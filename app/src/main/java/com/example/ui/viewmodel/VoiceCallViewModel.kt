package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.repository.TutorRepository
import com.example.domain.engine.GrammarCorrectionEngine
import com.example.webrtc.RealtimeTranscriptEvent
import com.example.webrtc.RealtimeWebRtcManager
import com.example.webrtc.VoiceConnectionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveTranscriptItem(
    val id: String,
    val speaker: String, // "TEACHER" or "STUDENT"
    val textEn: String,
    val bengaliNote: String = "",
    val correctedSentence: String? = null,
    val originalSentence: String? = null,
    val grammarExplanationBn: String? = null,
    val isStreaming: Boolean = false
)

data class VoiceCallUiState(
    val topicTitle: String = "Everyday Conversation",
    val connectionState: VoiceConnectionState = VoiceConnectionState.IDLE,
    val elapsedSeconds: Int = 0,
    val maxSessionSeconds: Int = 600,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isStudentSpeaking: Boolean = false,
    val isAiSpeaking: Boolean = false,
    val errorMessageBn: String? = null,
    val transcript: List<LiveTranscriptItem> = emptyList(),
    val savedConversationId: Long? = null,
    val sessionLimitReached: Boolean = false
)

class VoiceCallViewModel(
    private val repository: TutorRepository,
    private val webRtcManager: RealtimeWebRtcManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceCallUiState())
    val uiState: StateFlow<VoiceCallUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var eventsJob: Job? = null
    private var sessionStartMillis: Long = 0L
    private var lastStudentUtterance: String = ""
    private var hasRecordedSession = false

    init {
        viewModelScope.launch {
            webRtcManager.connectionState.collect { connState ->
                _uiState.update { it.copy(connectionState = connState) }
                if (connState == VoiceConnectionState.CONNECTED && timerJob == null) {
                    startTimer()
                }
            }
        }
        viewModelScope.launch {
            webRtcManager.errorMessageBn.collect { err ->
                if (err != null) {
                    _uiState.update { it.copy(errorMessageBn = err) }
                }
            }
        }
        viewModelScope.launch {
            webRtcManager.isMuted.collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }
        viewModelScope.launch {
            webRtcManager.isSpeakerOn.collect { speaker ->
                _uiState.update { it.copy(isSpeakerOn = speaker) }
            }
        }
        observeRealtimeEvents()
    }

    private fun observeRealtimeEvents() {
        eventsJob?.cancel()
        eventsJob = viewModelScope.launch {
            webRtcManager.events.collect { event ->
                when (event) {
                    is RealtimeTranscriptEvent.UserSpeechStarted -> {
                        _uiState.update { it.copy(isStudentSpeaking = true, isAiSpeaking = false) }
                    }
                    is RealtimeTranscriptEvent.UserSpeechStopped -> {
                        _uiState.update { it.copy(isStudentSpeaking = false) }
                    }
                    is RealtimeTranscriptEvent.UserSpeechFinal -> {
                        lastStudentUtterance = event.text
                        val correction = GrammarCorrectionEngine.analyzeSentence(event.text)
                        val hasCorrection = correction.correctedSentence.isNotBlank() &&
                            !correction.correctedSentence.equals(event.text.trim(), ignoreCase = true) &&
                            correction.categoryLabel != "Correct Sentence"

                        val item = LiveTranscriptItem(
                            id = "student-${System.currentTimeMillis()}",
                            speaker = "STUDENT",
                            textEn = event.text,
                            originalSentence = if (hasCorrection) event.text else null,
                            correctedSentence = if (hasCorrection) correction.correctedSentence else null,
                            grammarExplanationBn = if (hasCorrection) correction.bengaliExplanation else null
                        )
                        _uiState.update { state ->
                            state.copy(transcript = state.transcript + item)
                        }
                    }
                    is RealtimeTranscriptEvent.TeacherDelta -> {
                        _uiState.update { state ->
                            val existingIndex = state.transcript.indexOfFirst { it.id == "ai-${event.responseId}" }
                            val updated = if (existingIndex >= 0) {
                                state.transcript.toMutableList().also { list ->
                                    val old = list[existingIndex]
                                    list[existingIndex] = old.copy(
                                        textEn = old.textEn + event.delta,
                                        isStreaming = true
                                    )
                                }
                            } else {
                                state.transcript + LiveTranscriptItem(
                                    id = "ai-${event.responseId}",
                                    speaker = "TEACHER",
                                    textEn = event.delta,
                                    isStreaming = true
                                )
                            }
                            state.copy(transcript = updated, isAiSpeaking = true)
                        }
                    }
                    is RealtimeTranscriptEvent.TeacherDone -> {
                        val parsedCorrection = GrammarCorrectionEngine.parseTeacherCorrectionFromText(
                            studentUtterance = lastStudentUtterance,
                            teacherReply = event.fullText
                        )
                        val bengaliLines = event.fullText.lines()
                            .filter { line -> line.any { ch -> ch in '\u0980'..'\u09FF' } }
                            .joinToString("\n")

                        _uiState.update { state ->
                            val existingIndex = state.transcript.indexOfFirst { it.id == "ai-${event.responseId}" }
                            val updatedItem = LiveTranscriptItem(
                                id = "ai-${event.responseId}",
                                speaker = "TEACHER",
                                textEn = event.fullText,
                                bengaliNote = bengaliLines,
                                originalSentence = parsedCorrection?.first,
                                correctedSentence = parsedCorrection?.second,
                                grammarExplanationBn = parsedCorrection?.third,
                                isStreaming = false
                            )
                            val updatedList = if (existingIndex >= 0) {
                                state.transcript.toMutableList().also { it[existingIndex] = updatedItem }
                            } else {
                                state.transcript + updatedItem
                            }
                            state.copy(transcript = updatedList, isAiSpeaking = false)
                        }
                    }
                    is RealtimeTranscriptEvent.ServerError -> {
                        _uiState.update { it.copy(errorMessageBn = event.messageBn, isAiSpeaking = false) }
                    }
                }
            }
        }
    }

    fun startVoiceSession(topicTitle: String) {
        viewModelScope.launch {
            val prefs = repository.preferencesFlow.first()
            if (prefs.dailySessionsUsed >= prefs.dailySessionLimit) {
                _uiState.update {
                    it.copy(
                        topicTitle = topicTitle,
                        connectionState = VoiceConnectionState.ERROR,
                        sessionLimitReached = true,
                        errorMessageBn = "আজকের দৈনিক AI ভয়েস সেশনের সীমা (${prefs.dailySessionLimit} বার) শেষ হয়েছে। অফলাইন লেসন ও উচ্চারণ অনুশীলন ব্যবহার করুন।"
                    )
                }
                return@launch
            }

            hasRecordedSession = false
            sessionStartMillis = System.currentTimeMillis()
            _uiState.value = VoiceCallUiState(
                topicTitle = topicTitle,
                connectionState = VoiceConnectionState.FETCHING_TOKEN,
                maxSessionSeconds = prefs.maxSessionMinutes * 60
            )

            val deviceId = repository.getOrCreateDeviceId()
            val lessons = repository.observeLessonsWithProgress().first()
            val completedCount = lessons.count { it.isCompleted }

            val serverMaxSeconds = webRtcManager.connect(
                backendBaseUrl = prefs.backendBaseUrl,
                deviceId = deviceId,
                topic = topicTitle,
                learnerName = prefs.learnerName,
                completedLessons = completedCount,
                languageMode = prefs.languageMode.name
            )

            if (serverMaxSeconds != null) {
                repository.recordAiSessionStarted()
                _uiState.update { it.copy(maxSessionSeconds = serverMaxSeconds) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val nextSeconds = _uiState.value.elapsedSeconds + 1
                _uiState.update { it.copy(elapsedSeconds = nextSeconds) }
                if (nextSeconds >= _uiState.value.maxSessionSeconds) {
                    _uiState.update {
                        it.copy(errorMessageBn = "এই সেশনের নির্ধারিত সময় শেষ হয়েছে। নতুন সেশন শুরু করুন।")
                    }
                    endAndSaveSession()
                    break
                }
            }
        }
    }

    fun toggleMute() {
        webRtcManager.setMuted(!_uiState.value.isMuted)
    }

    fun toggleSpeaker() {
        webRtcManager.setSpeakerEnabled(!_uiState.value.isSpeakerOn)
    }

    fun bargeInInterrupt() {
        webRtcManager.interruptAiResponse()
        _uiState.update { it.copy(isAiSpeaking = false) }
    }

    fun onAppBackgrounded() {
        if (_uiState.value.connectionState == VoiceConnectionState.CONNECTED && !_uiState.value.isMuted) {
            webRtcManager.setMuted(true)
        }
    }

    fun endAndSaveSession() {
        if (hasRecordedSession) return
        hasRecordedSession = true
        timerJob?.cancel()
        timerJob = null

        val state = _uiState.value
        val duration = state.elapsedSeconds.coerceAtLeast(1)
        webRtcManager.disconnect(reportDurationSeconds = duration)

        if (state.transcript.isNotEmpty()) {
            viewModelScope.launch {
                val messages = state.transcript.map { item ->
                    ConversationMessageEntity(
                        conversationId = 0L,
                        speaker = item.speaker,
                        textEn = item.textEn,
                        bengaliNote = item.bengaliNote,
                        originalSentence = item.originalSentence,
                        correctedSentence = item.correctedSentence,
                        grammarExplanationBn = item.grammarExplanationBn
                    )
                }
                val corrections = state.transcript.count { !it.correctedSentence.isNullOrBlank() }
                val feedbackBn = if (corrections > 0) {
                    "দারুণ চেষ্টা! আপনি ${duration} সেকেন্ড কথা বলেছেন এবং $corrections টি বাক্যের সঠিক রূপ দেখেছেন।"
                } else {
                    "চমৎকার! আপনি আজ ${duration} সেকেন্ড ইংরেজিতে কথা বলার অনুশীলন করেছেন।"
                }
                val savedId = repository.saveConversation(
                    topicTitle = state.topicTitle,
                    startedAt = sessionStartMillis.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    endedAt = System.currentTimeMillis(),
                    durationSeconds = duration,
                    messages = messages,
                    newVocabulary = emptyList(),
                    feedbackBn = feedbackBn,
                    mode = "LIVE_WEBRTC"
                )
                _uiState.update { it.copy(savedConversationId = savedId, connectionState = VoiceConnectionState.ENDED) }
            }
        } else {
            _uiState.update { it.copy(connectionState = VoiceConnectionState.ENDED) }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        eventsJob?.cancel()
        webRtcManager.disconnect(0)
        super.onCleared()
    }
}
