package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppLanguageMode
import com.example.data.preferences.TutorPreferencesState
import com.example.data.repository.TutorRepository
import com.example.domain.model.LearningProgressSummary
import com.example.domain.model.LessonWithProgress
import com.example.domain.model.VocabularyItem
import com.example.speech.SpeechServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val preferences: TutorPreferencesState = TutorPreferencesState(),
    val progress: LearningProgressSummary? = null,
    val recommendedLesson: LessonWithProgress? = null,
    val continueLesson: LessonWithProgress? = null
)

class HomeViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.preferencesFlow,
        repository.observeProgressSummary(),
        repository.observeLessonsWithProgress()
    ) { prefs, progress, lessons ->
        val nextLesson = lessons.firstOrNull { !it.isCompleted } ?: lessons.firstOrNull()
        val lastPracticed = lessons
            .filter { it.practiceCount > 0 && !it.isCompleted }
            .firstOrNull() ?: nextLesson
        HomeUiState(
            isLoading = false,
            preferences = prefs,
            progress = progress,
            recommendedLesson = nextLesson,
            continueLesson = lastPracticed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

class OnboardingViewModel(
    private val repository: TutorRepository
) : ViewModel() {

    val preferences: StateFlow<TutorPreferencesState> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TutorPreferencesState()
    )

    fun selectLanguage(mode: AppLanguageMode) {
        viewModelScope.launch {
            repository.setLanguageMode(mode)
        }
    }

    fun completeGoalSetup(
        learnerName: String,
        speakingMinutes: Int,
        vocabGoal: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(learnerName, speakingMinutes, vocabGoal)
            onComplete()
        }
    }
}

class LessonsViewModel(
    private val repository: TutorRepository,
    private val speechManager: SpeechServiceManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val lessons: StateFlow<List<LessonWithProgress>> = combine(
        repository.observeLessonsWithProgress(),
        _selectedCategory
    ) { all, category ->
        if (category == "All") all else all.filter { it.category == category }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLessons: StateFlow<List<LessonWithProgress>> = repository.observeLessonsWithProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val ttsErrorBn: StateFlow<String?> = speechManager.ttsErrorBn

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun observeLesson(lessonId: Int) = repository.observeLessonDetail(lessonId)

    fun speakPhrase(text: String) {
        viewModelScope.launch {
            val slow = repository.preferencesFlow.first().slowTtsForBeginners
            speechManager.speakEnglish(text, slowForBeginners = slow)
        }
    }

    fun completeLesson(lessonId: Int, quizScore: Int) {
        viewModelScope.launch {
            repository.markLessonPracticed(lessonId, completed = true, quizScore = quizScore)
        }
    }

    fun clearTtsError() = speechManager.clearErrors()
}

class VocabularyViewModel(
    private val repository: TutorRepository,
    private val speechManager: SpeechServiceManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    val words: StateFlow<List<VocabularyItem>> = combine(
        repository.observeVocabularyItems(),
        _searchQuery,
        _favoritesOnly
    ) { all, query, favOnly ->
        val trimmed = query.trim().lowercase()
        all.filter { item ->
            val matchesFav = !favOnly || item.isFavorite
            val matchesQuery = trimmed.isEmpty() ||
                item.englishWord.lowercase().contains(trimmed) ||
                item.bengaliMeaning.contains(trimmed) ||
                item.category.lowercase().contains(trimmed)
            matchesFav && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val progressSummary = repository.observeProgressSummary().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFavoritesOnly(enabled: Boolean) {
        _favoritesOnly.value = enabled
    }

    fun toggleFavorite(item: VocabularyItem) {
        viewModelScope.launch {
            repository.toggleFavoriteWord(item.id, item.isFavorite)
        }
    }

    fun markMastered(item: VocabularyItem, mastered: Boolean) {
        viewModelScope.launch {
            repository.markWordMastered(item.id, mastered)
        }
    }

    fun speakWord(text: String) {
        viewModelScope.launch {
            val slow = repository.preferencesFlow.first().slowTtsForBeginners
            speechManager.speakEnglish(text, slowForBeginners = slow)
        }
    }
}
