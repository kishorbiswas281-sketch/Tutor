package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.preferences.AppLanguageMode
import com.example.data.preferences.TutorPreferencesState
import com.example.data.remote.BackendCallResult
import com.example.data.remote.RealtimeBackendApi
import com.example.data.repository.TutorRepository
import com.example.domain.engine.GrammarCorrectionEngine
import com.example.domain.engine.PronunciationEvaluator
import com.example.domain.model.GrammarFeedbackItem
import com.example.domain.model.PronunciationResult
import com.example.speech.SpeechServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GrammarViewModel(
    private val repository: TutorRepository,
    private val speechManager: SpeechServiceManager
) : ViewModel() {

    private val _inputSentence = MutableStateFlow("")
    val inputSentence: StateFlow<String> = _inputSentence.asStateFlow()

    private val _analyzedFeedback = MutableStateFlow<GrammarFeedbackItem?>(null)
    val analyzedFeedback: StateFlow<GrammarFeedbackItem?> = _analyzedFeedback.asStateFlow()

    val feedbackList: StateFlow<List<GrammarFeedbackItem>> = combine(
        repository.observeRecordedGrammarCorrections(),
        _analyzedFeedback
    ) { recorded, current ->
        val list = mutableListOf<GrammarFeedbackItem>()
        if (current != null && current.correctedSentence.isNotBlank()) {
            list.add(current)
        }
        list.addAll(recorded)
        list.addAll(GrammarCorrectionEngine.commonBeginnerMistakes())
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GrammarCorrectionEngine.commonBeginnerMistakes()
    )

    fun updateInput(text: String) {
        _inputSentence.value = text
    }

    fun analyzeInput(sentenceOverride: String? = null) {
        val target = sentenceOverride ?: _inputSentence.value
        if (sentenceOverride != null) {
            _inputSentence.value = sentenceOverride
        }
        val result = GrammarCorrectionEngine.analyzeSentence(target)
        _analyzedFeedback.value = result
        viewModelScope.launch {
            repository.recordGrammarReview()
        }
    }

    fun speakSentence(sentence: String) {
        viewModelScope.launch {
            val slow = repository.preferencesFlow.first().slowTtsForBeginners
            speechManager.speakEnglish(sentence, slowForBeginners = slow)
        }
    }
}

class PronunciationViewModel(
    private val repository: TutorRepository,
    private val speechManager: SpeechServiceManager
) : ViewModel() {

    val presets = listOf(
        "Hello! How are you?",
        "My name is Kishor.",
        "I am from Bangladesh.",
        "Water please, thank you.",
        "I am learning English every day.",
        "Could you please speak slowly?"
    )

    private val _targetSentence = MutableStateFlow(presets.first())
    val targetSentence: StateFlow<String> = _targetSentence.asStateFlow()

    private val _result = MutableStateFlow<PronunciationResult?>(null)
    val result: StateFlow<PronunciationResult?> = _result.asStateFlow()

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val partialSpeech: StateFlow<String> = speechManager.partialSpeech
    val speechErrorBn: StateFlow<String?> = speechManager.speechErrorBn
    val ttsErrorBn: StateFlow<String?> = speechManager.ttsErrorBn

    fun setTargetSentence(sentence: String) {
        if (sentence.isNotBlank()) {
            _targetSentence.value = sentence.trim()
            _result.value = null
        }
    }

    fun listenTargetAudio() {
        viewModelScope.launch {
            val slow = repository.preferencesFlow.first().slowTtsForBeginners
            speechManager.speakEnglish(_targetSentence.value, slowForBeginners = slow)
        }
    }

    fun startSpeechCapture() {
        speechManager.clearErrors()
        speechManager.startListening { recognized ->
            val evaluation = PronunciationEvaluator.evaluate(_targetSentence.value, recognized)
            _result.value = evaluation
            viewModelScope.launch {
                repository.recordPronunciationAttempt(20)
            }
        }
    }

    fun stopSpeechCapture() {
        speechManager.stopListening()
    }

    fun evaluateManualText(spokenSample: String) {
        val evaluation = PronunciationEvaluator.evaluate(_targetSentence.value, spokenSample)
        _result.value = evaluation
        viewModelScope.launch {
            repository.recordPronunciationAttempt(15)
        }
    }

    fun clearErrors() = speechManager.clearErrors()

    override fun onCleared() {
        speechManager.stopListening()
        super.onCleared()
    }
}

class HistoryViewModel(
    private val repository: TutorRepository,
    private val speechManager: SpeechServiceManager
) : ViewModel() {

    val conversations: StateFlow<List<ConversationEntity>> =
        repository.observeAllConversations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun observeConversation(id: Long): StateFlow<ConversationEntity?> =
        repository.observeConversation(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun observeMessages(id: Long): StateFlow<List<ConversationMessageEntity>> =
        repository.observeConversationMessages(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun speakLine(text: String) {
        viewModelScope.launch {
            val slow = repository.preferencesFlow.first().slowTtsForBeginners
            speechManager.speakEnglish(text, slowForBeginners = slow)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
        }
    }
}

data class BackendHealthUiState(
    val checking: Boolean = false,
    val statusMessageBn: String? = null,
    val isHealthy: Boolean = false
)

class SettingsViewModel(
    private val repository: TutorRepository,
    private val backendApi: RealtimeBackendApi
) : ViewModel() {

    val preferences: StateFlow<TutorPreferencesState> =
        repository.preferencesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TutorPreferencesState()
        )

    private val _healthState = MutableStateFlow(BackendHealthUiState())
    val healthState: StateFlow<BackendHealthUiState> = _healthState.asStateFlow()

    private val _savedFeedbackBn = MutableStateFlow<String?>(null)
    val savedFeedbackBn: StateFlow<String?> = _savedFeedbackBn.asStateFlow()

    fun saveProfile(
        learnerName: String,
        languageMode: AppLanguageMode,
        speakingMinutes: Int,
        vocabGoal: Int,
        slowTts: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSettings(learnerName, languageMode, speakingMinutes, vocabGoal, slowTts)
            _savedFeedbackBn.value = "সেটিংস সংরক্ষিত হয়েছে।"
        }
    }

    fun saveAndTestBackendUrl(url: String) {
        viewModelScope.launch {
            repository.setBackendUrl(url)
            _healthState.value = BackendHealthUiState(checking = true)
            when (val res = backendApi.checkHealth(url)) {
                is BackendCallResult.Success -> {
                    val aiReady = res.data.aiConfigured
                    _healthState.value = BackendHealthUiState(
                        checking = false,
                        isHealthy = aiReady,
                        statusMessageBn = if (aiReady) {
                            "সার্ভার সংযুক্ত (${res.data.model}, সেশন সীমা ${res.data.maxSessionSeconds / 60} মিনিট)।"
                        } else {
                            "সার্ভার চালু আছে, তবে OpenAI API কী এখনো সেট করা হয়নি।"
                        }
                    )
                }
                is BackendCallResult.Failure -> {
                    _healthState.value = BackendHealthUiState(
                        checking = false,
                        isHealthy = false,
                        statusMessageBn = res.messageBn
                    )
                }
            }
        }
    }

    fun setDarkTheme(followSystem: Boolean, darkEnabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkTheme(followSystem, darkEnabled)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllConversations()
            _savedFeedbackBn.value = "সব কথোপকথনের ইতিহাস মুছে ফেলা হয়েছে।"
        }
    }

    fun clearFeedback() {
        _savedFeedbackBn.value = null
    }
}
