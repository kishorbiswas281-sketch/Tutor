package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LessonExample(
    val english: String,
    val bengaliMeaning: String,
    val pronunciationBn: String
)

@Serializable
data class DialogueLine(
    val speaker: String,
    val english: String,
    val bengali: String
)

@Serializable
data class LessonQuizQuestion(
    val questionBn: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanationBn: String
)

data class LessonWithProgress(
    val id: Int,
    val orderIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val category: String,
    val levelTag: String,
    val estimatedMinutes: Int,
    val summaryBn: String,
    val explanationBn: String,
    val examples: List<LessonExample>,
    val dialogue: List<DialogueLine>,
    val quiz: List<LessonQuizQuestion>,
    val isCompleted: Boolean,
    val completionPercent: Int,
    val quizScore: Int,
    val practiceCount: Int
)

data class VocabularyItem(
    val id: Int,
    val lessonId: Int,
    val englishWord: String,
    val bengaliMeaning: String,
    val pronunciationBn: String,
    val ipaPronunciation: String,
    val exampleSentenceEn: String,
    val exampleSentenceBn: String,
    val category: String,
    val mastered: Boolean,
    val reviewCount: Int,
    val isFavorite: Boolean
)

data class GrammarFeedbackItem(
    val id: String,
    val originalSentence: String,
    val correctedSentence: String,
    val bengaliExplanation: String,
    val additionalExamples: List<String>,
    val categoryLabel: String,
    val sourceTimestamp: Long = 0L
)

data class PronunciationResult(
    val targetSentence: String,
    val recognizedSpeech: String,
    val matchPercent: Int,
    val feedbackBn: String,
    val difficultWords: List<String>,
    val phoneticTipsBn: List<String>,
    val assessmentMethodNoteBn: String
)

data class LearningProgressSummary(
    val completedLessons: Int,
    val totalLessons: Int,
    val learnedVocabulary: Int,
    val totalVocabulary: Int,
    val totalSpeakingMinutes: Int,
    val todaySpeakingMinutes: Int,
    val dailySpeakingGoalMinutes: Int,
    val dailyVocabGoal: Int,
    val todayVocabCount: Int,
    val currentStreakDays: Int,
    val grammarCorrectionsCount: Int,
    val learningLevelTitleBn: String,
    val learningLevelTitleEn: String,
    val weeklyStats: List<DailyBarStat>
)

data class DailyBarStat(
    val dateIso: String,
    val dayLabelBn: String,
    val speakingMinutes: Int,
    val vocabularyCount: Int
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val messageBn: String) : UiState<Nothing>
}
