package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.AppContainer

class TutorViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.repository) as T
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(container.repository) as T
            modelClass.isAssignableFrom(LessonsViewModel::class.java) ->
                LessonsViewModel(container.repository, container.speechManager) as T
            modelClass.isAssignableFrom(VocabularyViewModel::class.java) ->
                VocabularyViewModel(container.repository, container.speechManager) as T
            modelClass.isAssignableFrom(GrammarViewModel::class.java) ->
                GrammarViewModel(container.repository, container.speechManager) as T
            modelClass.isAssignableFrom(PronunciationViewModel::class.java) ->
                PronunciationViewModel(container.repository, container.speechManager) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(container.repository, container.speechManager) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.repository, container.backendApi) as T
            modelClass.isAssignableFrom(VoiceCallViewModel::class.java) ->
                VoiceCallViewModel(container.repository, container.webRtcManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
