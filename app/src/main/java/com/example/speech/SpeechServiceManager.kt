package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class SpeechServiceManager(context: Context) {
    private val appContext = context.applicationContext

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsErrorBn = MutableStateFlow<String?>(null)
    val ttsErrorBn: StateFlow<String?> = _ttsErrorBn.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialSpeech = MutableStateFlow("")
    val partialSpeech: StateFlow<String> = _partialSpeech.asStateFlow()

    private val _recognizedSpeech = MutableStateFlow("")
    val recognizedSpeech: StateFlow<String> = _recognizedSpeech.asStateFlow()

    private val _speechErrorBn = MutableStateFlow<String?>(null)
    val speechErrorBn: StateFlow<String?> = _speechErrorBn.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val usResult = tts?.setLanguage(Locale.US)
                if (usResult == TextToSpeech.LANG_MISSING_DATA || usResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val enResult = tts?.setLanguage(Locale.ENGLISH)
                    ttsReady = enResult != TextToSpeech.LANG_MISSING_DATA &&
                        enResult != TextToSpeech.LANG_NOT_SUPPORTED
                } else {
                    ttsReady = true
                }
                if (!ttsReady) {
                    _ttsErrorBn.value = "ইংরেজি অডিও ভয়েস প্যাকেজ এই ডিভাইসে প্রস্তুত নয়।"
                }
            } else {
                ttsReady = false
                _ttsErrorBn.value = "অডিও প্লেব্যাক ইঞ্জিন চালু করা যায়নি।"
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _ttsErrorBn.value = "অডিও চালু করতে সমস্যা হয়েছে।"
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _ttsErrorBn.value = "অডিও চালু করতে সমস্যা হয়েছে।"
            }
        })
    }

    fun speakEnglish(text: String, slowForBeginners: Boolean = true) {
        if (text.isBlank()) return
        _ttsErrorBn.value = null
        val engine = tts
        if (engine == null || !ttsReady) {
            _ttsErrorBn.value = "অডিও প্লেব্যাক এই মুহূর্তে উপলব্ধ নয়।"
            return
        }
        engine.setSpeechRate(if (slowForBeginners) 0.78f else 0.98f)
        engine.setPitch(1.0f)
        val utteranceId = UUID.randomUUID().toString()
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            _isSpeaking.value = false
            _ttsErrorBn.value = "অডিও চালু করতে সমস্যা হয়েছে।"
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun isSpeechRecognitionAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening(onResult: (String) -> Unit) {
        _speechErrorBn.value = null
        _partialSpeech.value = ""
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _speechErrorBn.value = "এই ডিভাইসে স্পিচ রিকগনিশন সেবা পাওয়া যায়নি।"
            return
        }
        stopListening()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _speechErrorBn.value = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "কোনো কথা শোনা যায়নি। আবার চেষ্টা করুন।"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "কথা বলার জন্য মাইক্রোফোন অনুমতি প্রয়োজন।"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "স্পিচ রিকগনিশনের জন্য নেটওয়ার্ক বা অফলাইন ভয়েস প্যাক প্রয়োজন।"
                    else -> "কথা শনাক্ত করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।"
                }
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull().orEmpty()
                _recognizedSpeech.value = best
                if (best.isNotBlank()) {
                    onResult(best)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partial.isNotBlank()) {
                    _partialSpeech.value = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechErrorBn.value = "মাইক্রোফোন চালু করা যায়নি।"
        }
    }

    fun stopListening() {
        _isListening.value = false
        speechRecognizer?.apply {
            runCatching { stopListening() }
            runCatching { destroy() }
        }
        speechRecognizer = null
    }

    fun clearErrors() {
        _ttsErrorBn.value = null
        _speechErrorBn.value = null
    }

    fun release() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
