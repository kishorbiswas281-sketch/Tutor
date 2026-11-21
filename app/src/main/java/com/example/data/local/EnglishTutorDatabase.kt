package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.ConversationMessageDao
import com.example.data.local.dao.DailyLearningStatsDao
import com.example.data.local.dao.FavoriteWordDao
import com.example.data.local.dao.LessonDao
import com.example.data.local.dao.LessonProgressDao
import com.example.data.local.dao.UserPreferencesDao
import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.DailyLearningStatsEntity
import com.example.data.local.entity.FavoriteWordEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.LessonProgressEntity
import com.example.data.local.entity.UserPreferencesEntity
import com.example.data.local.entity.VocabularyEntity

@Database(
    entities = [
        UserPreferencesEntity::class,
        LessonEntity::class,
        LessonProgressEntity::class,
        VocabularyEntity::class,
        FavoriteWordEntity::class,
        ConversationEntity::class,
        ConversationMessageEntity::class,
        DailyLearningStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EnglishTutorDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun lessonDao(): LessonDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun favoriteWordDao(): FavoriteWordDao
    abstract fun conversationDao(): ConversationDao
    abstract fun conversationMessageDao(): ConversationMessageDao
    abstract fun dailyLearningStatsDao(): DailyLearningStatsDao

    companion object {
        @Volatile
        private var INSTANCE: EnglishTutorDatabase? = null

        fun getInstance(context: Context): EnglishTutorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EnglishTutorDatabase::class.java,
                    "english_tutor_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
