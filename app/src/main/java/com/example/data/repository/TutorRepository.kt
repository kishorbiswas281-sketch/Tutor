package com.example.data.repository

import com.example.data.local.EnglishTutorDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.DailyLearningStatsEntity
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.UserPreferencesEntity
import com.example.data.preferences.AppLanguageMode
import com.example.data.preferences.AppPreferencesManager
import com.example.data.preferences.TutorPreferencesState
import com.example.data.seed.BeginnerCurriculumSeed
import com.example.data.seed.VocabularySeed
import com.example.domain.model.DailyBarStat
import com.example.domain.model.DialogueLine
import com.example.domain.model.GrammarFeedbackItem
import com.example.domain.model.LearningProgressSummary
import com.example.domain.model.LessonExample
import com.example.domain.model.LessonQuizQuestion
import com.example.domain.model.LessonWithProgress
import com.example.domain.model.VocabularyItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TutorRepository(
    private val db: EnglishTutorDatabase,
    private val prefsManager: AppPreferencesManager
) {
    private val json = Json { ignoreUnknownKeys = true }

    val preferencesFlow: Flow<TutorPreferencesState> = prefsManager.preferencesFlow

    suspend fun ensureSeeded() {
        if (db.lessonDao().getLessonCount() == 0) {
            db.lessonDao().insertAll(BeginnerCurriculumSeed.allLessons())
        }
        if (db.vocabularyDao().getCount() == 0) {
            db.vocabularyDao().insertAll(VocabularySeed.allWords())
        }
        if (db.userPreferencesDao().getPreferencesOnce() == null) {
            db.userPreferencesDao().upsert(UserPreferencesEntity())
        }
    }

    fun observeLessonsWithProgress(): Flow<List<LessonWithProgress>> =
        combine(
            db.lessonDao().observeAllLessons(),
            db.lessonProgressDao().observeAllProgress()
        ) { lessons, progressList ->
            val progressMap = progressList.associateBy { it.lessonId }
            lessons.map { entity -> entity.toDomain(progressMap[entity.id]) }
        }

    fun observeLessonDetail(lessonId: Int): Flow<LessonWithProgress?> =
        combine(
            db.lessonDao().observeLessonById(lessonId),
            db.lessonProgressDao().observeProgressForLesson(lessonId)
        ) { lesson, progress ->
            lesson?.toDomain(progress)
        }

    fun observeVocabularyItems(): Flow<List<VocabularyItem>> =
        combine(
            db.vocabularyDao().observeAllVocabulary(),
            db.favoriteWordDao().observeFavoriteIds()
        ) { words, favoriteIds ->
            val favSet = favoriteIds.toSet()
            words.map { w ->
                VocabularyItem(
                    id = w.id,
                    lessonId = w.lessonId,
                    englishWord = w.englishWord,
                    bengaliMeaning = w.bengaliMeaning,
                    pronunciationBn = w.pronunciationBn,
                    ipaPronunciation = w.ipaPronunciation,
                    exampleSentenceEn = w.exampleSentenceEn,
                    exampleSentenceBn = w.exampleSentenceBn,
                    category = w.category,
                    mastered = w.mastered,
                    reviewCount = w.reviewCount,
                    isFavorite = w.id in favSet
                )
            }
        }

    fun observeAllConversations(): Flow<List<ConversationEntity>> =
        db.conversationDao().observeAllConversations()

    fun observeConversation(conversationId: Long): Flow<ConversationEntity?> =
        db.conversationDao().observeConversation(conversationId)

    fun observeConversationMessages(conversationId: Long): Flow<List<ConversationMessageEntity>> =
        db.conversationMessageDao().observeMessagesForConversation(conversationId)

    fun observeRecordedGrammarCorrections(): Flow<List<GrammarFeedbackItem>> =
        db.conversationMessageDao().observeGrammarCorrections().map { list ->
            list.mapNotNull { msg ->
                val corrected = msg.correctedSentence ?: return@mapNotNull null
                GrammarFeedbackItem(
                    id = "msg-${msg.id}",
                    originalSentence = msg.originalSentence.orEmpty(),
                    correctedSentence = corrected,
                    bengaliExplanation = msg.grammarExplanationBn
                        ?: "AI কথোপকথনে শনাক্ত করা সংশোধিত বাক্য।",
                    additionalExamples = listOf(corrected),
                    categoryLabel = "Live AI Correction",
                    sourceTimestamp = msg.timestamp
                )
            }
        }

    fun observeProgressSummary(): Flow<LearningProgressSummary> =
        combine(
            db.lessonProgressDao().observeCompletedCount(),
            db.lessonDao().observeAllLessons(),
            db.vocabularyDao().observeMasteredCount(),
            db.vocabularyDao().observeAllVocabulary(),
            db.dailyLearningStatsDao().observeRecentStats(),
            db.conversationMessageDao().observeGrammarCorrections(),
            prefsManager.preferencesFlow
        ) { args ->
            val completedLessons = args[0] as Int
            val totalLessons = (args[1] as List<*>).size.coerceAtLeast(20)
            val learnedVocab = args[2] as Int
            val totalVocab = (args[3] as List<*>).size
            @Suppress("UNCHECKED_CAST")
            val stats = args[4] as List<DailyLearningStatsEntity>
            val corrections = (args[5] as List<*>).size
            val prefs = args[6] as TutorPreferencesState

            val todayIso = LocalDate.now().toString()
            val statsByDate = stats.associateBy { it.dateIso }
            val todayStat = statsByDate[todayIso]
            val totalSeconds = stats.sumOf { it.speakingSeconds }
            val streak = calculateStreak(statsByDate)

            val weekly = (6 downTo 0).map { offset ->
                val day = LocalDate.now().minusDays(offset.toLong())
                val iso = day.toString()
                val s = statsByDate[iso]
                DailyBarStat(
                    dateIso = iso,
                    dayLabelBn = bengaliDayShort(day),
                    speakingMinutes = (s?.speakingSeconds ?: 0) / 60,
                    vocabularyCount = s?.vocabularyLearnedCount ?: 0
                )
            }

            val (levelBn, levelEn) = when {
                completedLessons >= 15 -> "আত্মবিশ্বাসী বক্তা" to "Confident Beginner"
                completedLessons >= 8 -> "উন্নতিশীল শিক্ষার্থী" to "Elementary Speaker"
                completedLessons >= 3 -> "প্রাথমিক বক্তা" to "Rising Beginner"
                else -> "একদম নতুন শিক্ষার্থী" to "Absolute Beginner"
            }

            LearningProgressSummary(
                completedLessons = completedLessons,
                totalLessons = totalLessons,
                learnedVocabulary = learnedVocab,
                totalVocabulary = totalVocab,
                totalSpeakingMinutes = totalSeconds / 60,
                todaySpeakingMinutes = (todayStat?.speakingSeconds ?: 0) / 60,
                dailySpeakingGoalMinutes = prefs.dailySpeakingGoalMinutes,
                dailyVocabGoal = prefs.dailyVocabGoal,
                todayVocabCount = todayStat?.vocabularyLearnedCount ?: 0,
                currentStreakDays = streak,
                grammarCorrectionsCount = corrections,
                learningLevelTitleBn = levelBn,
                learningLevelTitleEn = levelEn,
                weeklyStats = weekly
            )
        }

    suspend fun setLanguageMode(mode: AppLanguageMode) {
        prefsManager.setLanguageMode(mode)
        val current = db.userPreferencesDao().getPreferencesOnce() ?: UserPreferencesEntity()
        db.userPreferencesDao().upsert(current.copy(languageMode = mode.name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun completeOnboarding(learnerName: String, speakingMinutes: Int, vocabGoal: Int) {
        prefsManager.completeOnboarding(learnerName, speakingMinutes, vocabGoal)
        val current = db.userPreferencesDao().getPreferencesOnce() ?: UserPreferencesEntity()
        db.userPreferencesDao().upsert(
            current.copy(
                learnerName = learnerName.trim().ifBlank { "শিক্ষার্থী" },
                dailySpeakingGoalMinutes = speakingMinutes,
                dailyVocabGoal = vocabGoal,
                onboardingCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSettings(
        learnerName: String,
        languageMode: AppLanguageMode,
        speakingMinutes: Int,
        vocabGoal: Int,
        slowTts: Boolean
    ) {
        prefsManager.updateProfileAndGoals(learnerName, languageMode, speakingMinutes, vocabGoal, slowTts)
        val current = db.userPreferencesDao().getPreferencesOnce() ?: UserPreferencesEntity()
        db.userPreferencesDao().upsert(
            current.copy(
                learnerName = learnerName.trim().ifBlank { "শিক্ষার্থী" },
                languageMode = languageMode.name,
                dailySpeakingGoalMinutes = speakingMinutes,
                dailyVocabGoal = vocabGoal,
                slowTtsEnabled = slowTts,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setBackendUrl(url: String) = prefsManager.setBackendBaseUrl(url)
    suspend fun setDarkTheme(followSystem: Boolean, enabled: Boolean) = prefsManager.setDarkTheme(followSystem, enabled)
    suspend fun getOrCreateDeviceId(): String = prefsManager.getOrCreateDeviceId()
    suspend fun recordAiSessionStarted() = prefsManager.incrementDailyAiSessionCount()

    suspend fun markLessonPracticed(lessonId: Int, completed: Boolean, quizScore: Int) {
        val existing = db.lessonProgressDao().observeProgressForLesson(lessonId).first()
        val nextCount = (existing?.practiceCount ?: 0) + 1
        val isNowCompleted = (existing?.isCompleted == true) || completed
        val percent = if (isNowCompleted) 100 else maxOf(existing?.completionPercent ?: 0, 50)
        db.lessonProgressDao().upsertProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                isCompleted = isNowCompleted,
                completionPercent = percent,
                quizScore = maxOf(existing?.quizScore ?: 0, quizScore),
                practiceCount = nextCount,
                lastPracticedAt = System.currentTimeMillis()
            )
        )
        if (completed && existing?.isCompleted != true) {
            updateTodayStats { it.copy(lessonsCompletedCount = it.lessonsCompletedCount + 1) }
        }
    }

    suspend fun toggleFavoriteWord(vocabularyId: Int, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            db.favoriteWordDao().removeFavorite(vocabularyId)
        } else {
            db.favoriteWordDao().addFavorite(FavoriteWordEntity(vocabularyId = vocabularyId))
        }
    }

    suspend fun markWordMastered(vocabularyId: Int, mastered: Boolean) {
        db.vocabularyDao().markReviewed(vocabularyId, mastered)
        if (mastered) {
            updateTodayStats { it.copy(vocabularyLearnedCount = it.vocabularyLearnedCount + 1) }
        }
    }

    suspend fun recordPronunciationAttempt(secondsPracticed: Int = 20) {
        updateTodayStats {
            it.copy(
                pronunciationAttempts = it.pronunciationAttempts + 1,
                speakingSeconds = it.speakingSeconds + secondsPracticed
            )
        }
    }

    suspend fun recordGrammarReview() {
        updateTodayStats { it.copy(grammarCorrectionsReviewed = it.grammarCorrectionsReviewed + 1) }
    }

    suspend fun saveConversation(
        topicTitle: String,
        startedAt: Long,
        endedAt: Long,
        durationSeconds: Int,
        messages: List<ConversationMessageEntity>,
        newVocabulary: List<String>,
        feedbackBn: String,
        mode: String
    ): Long {
        val correctedCount = messages.count { !it.correctedSentence.isNullOrBlank() }
        val conversationId = db.conversationDao().insertConversation(
            ConversationEntity(
                topicTitle = topicTitle,
                startedAt = startedAt,
                endedAt = endedAt,
                durationSeconds = durationSeconds,
                correctedCount = correctedCount,
                newVocabularyList = newVocabulary.joinToString(", "),
                learningFeedbackBn = feedbackBn,
                mode = mode
            )
        )
        if (messages.isNotEmpty()) {
            db.conversationMessageDao().insertMessages(
                messages.map { it.copy(conversationId = conversationId) }
            )
        }
        updateTodayStats {
            it.copy(speakingSeconds = it.speakingSeconds + durationSeconds)
        }
        return conversationId
    }

    suspend fun deleteConversation(conversationId: Long) {
        db.conversationDao().deleteConversation(conversationId)
    }

    suspend fun clearAllConversations() {
        db.conversationDao().deleteAllConversations()
    }

    private suspend fun updateTodayStats(transform: (DailyLearningStatsEntity) -> DailyLearningStatsEntity) {
        val today = LocalDate.now().toString()
        val current = db.dailyLearningStatsDao().getStatsForDate(today)
            ?: DailyLearningStatsEntity(dateIso = today)
        db.dailyLearningStatsDao().upsertStats(transform(current))
    }

    private fun calculateStreak(statsByDate: Map<String, DailyLearningStatsEntity>): Int {
        var streak = 0
        var cursor = LocalDate.now()
        while (true) {
            val s = statsByDate[cursor.toString()]
            val activeToday = s != null && (
                s.speakingSeconds > 0 ||
                    s.lessonsCompletedCount > 0 ||
                    s.vocabularyLearnedCount > 0 ||
                    s.pronunciationAttempts > 0
                )
            if (activeToday) {
                streak++
                cursor = cursor.minusDays(1)
            } else {
                if (streak == 0 && cursor == LocalDate.now()) {
                    cursor = cursor.minusDays(1)
                    val yesterday = statsByDate[cursor.toString()]
                    val activeYesterday = yesterday != null && (
                        yesterday.speakingSeconds > 0 ||
                            yesterday.lessonsCompletedCount > 0 ||
                            yesterday.vocabularyLearnedCount > 0 ||
                            yesterday.pronunciationAttempts > 0
                        )
                    if (!activeYesterday) break
                } else {
                    break
                }
            }
        }
        return streak
    }

    private fun bengaliDayShort(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "সোম"
        2 -> "মঙ্গল"
        3 -> "বুধ"
        4 -> "বৃহঃ"
        5 -> "শুক্র"
        6 -> "শনি"
        7 -> "রবি"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

    private fun LessonEntity.toDomain(progress: LessonProgressEntity?): LessonWithProgress {
        val examples = runCatching {
            json.decodeFromString<List<LessonExample>>(examplesJson)
        }.getOrDefault(emptyList())
        val dialogue = runCatching {
            json.decodeFromString<List<DialogueLine>>(dialogueJson)
        }.getOrDefault(emptyList())
        val quiz = runCatching {
            json.decodeFromString<List<LessonQuizQuestion>>(quizJson)
        }.getOrDefault(emptyList())

        return LessonWithProgress(
            id = id,
            orderIndex = orderIndex,
            titleEn = titleEn,
            titleBn = titleBn,
            category = category,
            levelTag = levelTag,
            estimatedMinutes = estimatedMinutes,
            summaryBn = summaryBn,
            explanationBn = explanationBn,
            examples = examples,
            dialogue = dialogue,
            quiz = quiz,
            isCompleted = progress?.isCompleted ?: false,
            completionPercent = progress?.completionPercent ?: 0,
            quizScore = progress?.quizScore ?: 0,
            practiceCount = progress?.practiceCount ?: 0
        )
    }
}
