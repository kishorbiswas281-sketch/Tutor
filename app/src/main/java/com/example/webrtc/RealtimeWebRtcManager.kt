package com.example.webrtc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.example.data.remote.BackendCallResult
import com.example.data.remote.RealtimeBackendApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

enum class VoiceConnectionState {
    IDLE,
    FETCHING_TOKEN,
    CONNECTING_WEBRTC,
    CONNECTED,
    RECONNECTING,
    ERROR,
    ENDED
}

sealed interface RealtimeTranscriptEvent {
    data class UserSpeechFinal(val text: String) : RealtimeTranscriptEvent
    data class TeacherDelta(val responseId: String, val delta: String) : RealtimeTranscriptEvent
    data class TeacherDone(val responseId: String, val fullText: String) : RealtimeTranscriptEvent
    data class UserSpeechStarted(val timestampMs: Long) : RealtimeTranscriptEvent
    data class UserSpeechStopped(val timestampMs: Long) : RealtimeTranscriptEvent
    data class ServerError(val messageBn: String) : RealtimeTranscriptEvent
}

class RealtimeWebRtcManager(
    context: Context,
    private val backendApi: RealtimeBackendApi
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _connectionState = MutableStateFlow(VoiceConnectionState.IDLE)
    val connectionState: StateFlow<VoiceConnectionState> = _connectionState.asStateFlow()

    private val _errorMessageBn = MutableStateFlow<String?>(null)
    val errorMessageBn: StateFlow<String?> = _errorMessageBn.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _events = MutableSharedFlow<RealtimeTranscriptEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RealtimeTranscriptEvent> = _events.asSharedFlow()

    private val initialized = AtomicBoolean(false)
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var audioDeviceModule: JavaAudioDeviceModule? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var dataChannel: DataChannel? = null

    private var audioManager: AudioManager? =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private var activeSessionToken: String = ""
    private var activeBackendUrl: String = ""

    private fun ensureFactoryInitialized() {
        if (initialized.compareAndSet(false, true)) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
            )
        }
        if (peerConnectionFactory == null) {
            val adm = JavaAudioDeviceModule.builder(appContext)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule()
            audioDeviceModule = adm
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setAudioDeviceModule(adm)
                .createPeerConnectionFactory()
        }
    }

    suspend fun connect(
        backendBaseUrl: String,
        deviceId: String,
        topic: String,
        learnerName: String,
        completedLessons: Int,
        languageMode: String
    ): Int? {
        disconnect(reportDurationSeconds = 0)
        _errorMessageBn.value = null
        _connectionState.value = VoiceConnectionState.FETCHING_TOKEN
        activeBackendUrl = backendBaseUrl

        val tokenResult = backendApi.fetchRealtimeClientSecret(
            baseUrl = backendBaseUrl,
            deviceId = deviceId,
            topic = topic,
            learnerName = learnerName,
            completedLessons = completedLessons,
            languageMode = languageMode
        )

        val (sessionToken, tokenPayload) = when (tokenResult) {
            is BackendCallResult.Success -> tokenResult.data
            is BackendCallResult.Failure -> {
                _connectionState.value = VoiceConnectionState.ERROR
                _errorMessageBn.value = tokenResult.messageBn
                return null
            }
        }

        activeSessionToken = sessionToken
        _connectionState.value = VoiceConnectionState.CONNECTING_WEBRTC

        return try {
            setupAudioMode()
            ensureFactoryInitialized()
            val factory = peerConnectionFactory ?: error("PeerConnectionFactory unavailable")

            val iceServers = listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            )
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            }

            val iceGatheringDone = CompletableDeferred<Unit>()
            val pc = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
                override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED,
                        PeerConnection.IceConnectionState.COMPLETED -> {
                            _connectionState.value = VoiceConnectionState.CONNECTED
                            _errorMessageBn.value = null
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            if (_connectionState.value == VoiceConnectionState.CONNECTED) {
                                _connectionState.value = VoiceConnectionState.RECONNECTING
                            }
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = VoiceConnectionState.ERROR
                            _errorMessageBn.value = "ভয়েস কল সংযোগ বিচ্ছিন্ন হয়েছে। আবার চেষ্টা করুন।"
                        }
                        else -> Unit
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                    if (state == PeerConnection.IceGatheringState.COMPLETE) {
                        iceGatheringDone.complete(Unit)
                    }
                }

                override fun onIceCandidate(candidate: IceCandidate?) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(dc: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }) ?: error("Failed to create PeerConnection")

            peerConnection = pc

            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            }
            val src = factory.createAudioSource(constraints)
            audioSource = src
            val track = factory.createAudioTrack("tutor_mic_track", src).apply {
                setEnabled(!_isMuted.value)
            }
            localAudioTrack = track
            pc.addTrack(track, listOf("tutor_stream"))

            val dc = pc.createDataChannel("oai-events", DataChannel.Init())
            dataChannel = dc
            dc.registerObserver(object : DataChannel.Observer {
                override fun onBufferedAmountChange(previousAmount: Long) {}
                override fun onStateChange() {
                    if (dc.state() == DataChannel.State.OPEN) {
                        sendSessionGreetingPrompt()
                    }
                }

                override fun onMessage(buffer: DataChannel.Buffer?) {
                    buffer ?: return
                    val bytes = ByteArray(buffer.data.remaining())
                    buffer.data.get(bytes)
                    val raw = String(bytes, Charsets.UTF_8)
                    handleRealtimeEvent(raw)
                }
            })

            val offerSdp = createAndSetLocalOffer(pc)
            withTimeoutOrNull(1500L) { iceGatheringDone.await() }
            val finalOfferSdp = pc.localDescription?.description ?: offerSdp

            val sdpExchange = backendApi.exchangeSdpOffer(
                rtcUrl = tokenPayload.rtcUrl,
                clientSecret = tokenPayload.clientSecret,
                offerSdp = finalOfferSdp
            )

            when (sdpExchange) {
                is BackendCallResult.Success -> {
                    setRemoteAnswer(pc, sdpExchange.data)
                    _connectionState.value = VoiceConnectionState.CONNECTED
                    tokenPayload.maxSessionSeconds
                }
                is BackendCallResult.Failure -> {
                    _connectionState.value = VoiceConnectionState.ERROR
                    _errorMessageBn.value = sdpExchange.messageBn
                    cleanupPeerConnection()
                    null
                }
            }
        } catch (e: Exception) {
            _connectionState.value = VoiceConnectionState.ERROR
            _errorMessageBn.value = "ভয়েস কল চালু করতে সমস্যা হয়েছে: ${e.localizedMessage ?: "Unknown"}"
            cleanupPeerConnection()
            null
        }
    }

    private fun sendSessionGreetingPrompt() {
        val greetEvent = """
            {
              "type": "response.create",
              "response": {
                "instructions": "Greet the student warmly in very simple English, ask how they are today, and keep it to one short friendly sentence."
              }
            }
        """.trimIndent()
        sendDataChannelJson(greetEvent)
    }

    fun interruptAiResponse() {
        sendDataChannelJson("""{"type":"response.cancel"}""")
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        localAudioTrack?.setEnabled(!muted)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        _isSpeakerOn.value = enabled
        audioManager?.isSpeakerphoneOn = enabled
    }

    fun disconnect(reportDurationSeconds: Int) {
        val tokenToReport = activeSessionToken
        val urlToReport = activeBackendUrl
        activeSessionToken = ""
        if (reportDurationSeconds > 0 && tokenToReport.isNotBlank() && urlToReport.isNotBlank()) {
            scope.launch {
                backendApi.reportUsage(urlToReport, tokenToReport, reportDurationSeconds)
            }
        }
        cleanupPeerConnection()
        restoreAudioMode()
        if (_connectionState.value != VoiceConnectionState.ERROR) {
            _connectionState.value = VoiceConnectionState.ENDED
        }
    }

    private fun sendDataChannelJson(payload: String) {
        val dc = dataChannel ?: return
        if (dc.state() != DataChannel.State.OPEN) return
        val buf = ByteBuffer.wrap(payload.toByteArray(Charsets.UTF_8))
        dc.send(DataChannel.Buffer(buf, false))
    }

    private fun handleRealtimeEvent(rawJson: String) {
        val obj = runCatching { json.parseToJsonElement(rawJson).jsonObject }.getOrNull() ?: return
        val type = obj["type"]?.jsonPrimitive?.content ?: return
        when (type) {
            "input_audio_buffer.speech_started" -> {
                _events.tryEmit(RealtimeTranscriptEvent.UserSpeechStarted(System.currentTimeMillis()))
            }
            "input_audio_buffer.speech_stopped" -> {
                _events.tryEmit(RealtimeTranscriptEvent.UserSpeechStopped(System.currentTimeMillis()))
            }
            "conversation.item.input_audio_transcription.completed" -> {
                val transcript = obj["transcript"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (transcript.isNotBlank()) {
                    _events.tryEmit(RealtimeTranscriptEvent.UserSpeechFinal(transcript))
                }
            }
            "response.output_audio_transcript.delta" -> {
                val respId = obj["response_id"]?.jsonPrimitive?.content ?: "default"
                val delta = obj["delta"]?.jsonPrimitive?.content.orEmpty()
                if (delta.isNotEmpty()) {
                    _events.tryEmit(RealtimeTranscriptEvent.TeacherDelta(respId, delta))
                }
            }
            "response.output_audio_transcript.done" -> {
                val respId = obj["response_id"]?.jsonPrimitive?.content ?: "default"
                val text = obj["transcript"]?.jsonPrimitive?.content?.trim().orEmpty()
                if (text.isNotBlank()) {
                    _events.tryEmit(RealtimeTranscriptEvent.TeacherDone(respId, text))
                }
            }
            "error" -> {
                val errObj: JsonObject? = obj["error"] as? JsonObject
                val code = errObj?.get("code")?.jsonPrimitive?.content.orEmpty()
                val msgBn = if (code.contains("token", ignoreCase = true) || code.contains("auth", ignoreCase = true)) {
                    "সেশন টোকেন বাতিল হয়েছে। অনুগ্রহ করে আবার সংযোগ করুন।"
                } else {
                    "AI শিক্ষকের উত্তর পেতে সাময়িক সমস্যা হয়েছে।"
                }
                _events.tryEmit(RealtimeTranscriptEvent.ServerError(msgBn))
            }
        }
    }

    private suspend fun createAndSetLocalOffer(pc: PeerConnection): String {
        val offerDeferred = CompletableDeferred<SessionDescription>()
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp != null) offerDeferred.complete(sdp)
                else offerDeferred.completeExceptionally(IllegalStateException("Null SDP offer"))
            }

            override fun onCreateFailure(err: String?) {
                offerDeferred.completeExceptionally(IllegalStateException(err ?: "Offer creation failed"))
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(err: String?) {}
        }, constraints)

        val offer = offerDeferred.await()
        val setDeferred = CompletableDeferred<Unit>()
        pc.setLocalDescription(object : SdpObserver {
            override fun onSetSuccess() { setDeferred.complete(Unit) }
            override fun onSetFailure(err: String?) {
                setDeferred.completeExceptionally(IllegalStateException(err ?: "Set local SDP failed"))
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(err: String?) {}
        }, offer)
        setDeferred.await()
        return offer.description
    }

    private suspend fun setRemoteAnswer(pc: PeerConnection, answerSdp: String) {
        val setDeferred = CompletableDeferred<Unit>()
        val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() { setDeferred.complete(Unit) }
            override fun onSetFailure(err: String?) {
                setDeferred.completeExceptionally(IllegalStateException(err ?: "Set remote SDP failed"))
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(err: String?) {}
        }, remoteSdp)
        setDeferred.await()
    }

    private fun setupAudioMode() {
        val am = audioManager ?: return
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        am.isSpeakerphoneOn = _isSpeakerOn.value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun restoreAudioMode() {
        val am = audioManager ?: return
        am.isSpeakerphoneOn = false
        am.mode = AudioManager.MODE_NORMAL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }

    private fun cleanupPeerConnection() {
        runCatching { dataChannel?.unregisterObserver() }
        runCatching { dataChannel?.close() }
        dataChannel = null

        runCatching { localAudioTrack?.dispose() }
        localAudioTrack = null

        runCatching { audioSource?.dispose() }
        audioSource = null

        runCatching { peerConnection?.close() }
        runCatching { peerConnection?.dispose() }
        peerConnection = null
    }

    fun release() {
        disconnect(0)
        runCatching { audioDeviceModule?.release() }
        audioDeviceModule = null
        runCatching { peerConnectionFactory?.dispose() }
        peerConnectionFactory = null
    }
}
