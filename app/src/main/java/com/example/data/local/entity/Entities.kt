package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val learnerName: String = "শিক্ষার্থী",
    val languageMode: String = "BENGALI_PRIMARY",
    val dailySpeakingGoalMinutes: Int = 10,
    val dailyVocabGoal: Int = 5,
    val learningLevel: String = "Absolute Beginner",
    val onboardingCompleted: Boolean = false,
    val slowTtsEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val orderIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val category: String,
    val levelTag: String,
    val estimatedMinutes: Int,
    val summaryBn: String,
    val explanationBn: String,
    val examplesJson: String,
    val dialogueJson: String,
    val quizJson: String
)

@Entity(
    tableName = "lesson_progress",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class LessonProgressEntity(
    @PrimaryKey val lessonId: Int,
    val isCompleted: Boolean = false,
    val completionPercent: Int = 0,
    val quizScore: Int = 0,
    val practiceCount: Int = 0,
    val lastPracticedAt: Long = 0L
)

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lessonId: Int,
    val englishWord: String,
    val bengaliMeaning: String,
    val pronunciationBn: String,
    val ipaPronunciation: String,
    val exampleSentenceEn: String,
    val exampleSentenceBn: String,
    val category: String,
    val mastered: Boolean = false,
    val reviewCount: Int = 0
)

@Entity(
    tableName = "favorite_words",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vocabularyId", unique = true)]
)
data class FavoriteWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vocabularyId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val topicTitle: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationSeconds: Int,
    val correctedCount: Int,
    val newVocabularyList: String,
    val learningFeedbackBn: String,
    val mode: String // "LIVE_WEBRTC" or "GUIDED_PRACTICE"
)

@Entity(
    tableName = "conversation_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: Long,
    val speaker: String, // "TEACHER" or "STUDENT"
    val textEn: String,
    val bengaliNote: String = "",
    val originalSentence: String? = null,
    val correctedSentence: String? = null,
    val grammarExplanationBn: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_learning_stats")
data class DailyLearningStatsEntity(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val speakingSeconds: Int = 0,
    val lessonsCompletedCount: Int = 0,
    val vocabularyLearnedCount: Int = 0,
    val grammarCorrectionsReviewed: Int = 0,
    val pronunciationAttempts: Int = 0
)
