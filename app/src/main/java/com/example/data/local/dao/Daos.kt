package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.DailyLearningStatsEntity
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.UserPreferencesEntity
import com.example.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun observePreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesOnce(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preferences: UserPreferencesEntity)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun observeAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun observeLessonById(lessonId: Int): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: Int): LessonEntity?

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)
}

@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun observeAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    fun observeProgressForLesson(lessonId: Int): Flow<LessonProgressEntity?>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE isCompleted = 1")
    fun observeCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LessonProgressEntity)
}

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY lessonId ASC, id ASC")
    fun observeAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE lessonId = :lessonId ORDER BY id ASC")
    fun observeVocabularyByLesson(lessonId: Int): Flow<List<VocabularyEntity>>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE mastered = 1")
    fun observeMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabularyEntity>)

    @Update
    suspend fun updateWord(word: VocabularyEntity)

    @Query("UPDATE vocabulary SET mastered = :mastered, reviewCount = reviewCount + 1 WHERE id = :wordId")
    suspend fun markReviewed(wordId: Int, mastered: Boolean)
}

@Dao
interface FavoriteWordDao {
    @Query("SELECT * FROM favorite_words ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteWordEntity>>

    @Query("SELECT vocabularyId FROM favorite_words")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteWordEntity)

    @Query("DELETE FROM favorite_words WHERE vocabularyId = :vocabularyId")
    suspend fun removeFavorite(vocabularyId: Int)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY startedAt DESC")
    fun observeAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun observeConversation(conversationId: Long): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface ConversationMessageDao {
    @Query("SELECT * FROM conversation_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    fun observeMessagesForConversation(conversationId: Long): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages WHERE correctedSentence IS NOT NULL AND correctedSentence != '' ORDER BY timestamp DESC")
    fun observeGrammarCorrections(): Flow<List<ConversationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ConversationMessageEntity>)
}

@Dao
interface DailyLearningStatsDao {
    @Query("SELECT * FROM daily_learning_stats ORDER BY dateIso DESC LIMIT 30")
    fun observeRecentStats(): Flow<List<DailyLearningStatsEntity>>

    @Query("SELECT * FROM daily_learning_stats WHERE dateIso = :dateIso")
    fun observeStatsForDate(dateIso: String): Flow<DailyLearningStatsEntity?>

    @Query("SELECT * FROM daily_learning_stats WHERE dateIso = :dateIso")
    suspend fun getStatsForDate(dateIso: String): DailyLearningStatsEntity?

    @Query("SELECT COALESCE(SUM(speakingSeconds), 0) FROM daily_learning_stats")
    fun observeTotalSpeakingSeconds(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: DailyLearningStatsEntity)
}
