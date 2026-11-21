package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tutor_preferences")

enum class AppLanguageMode(val titleBn: String, val titleEn: String, val descriptionBn: String) {
    BENGALI_PRIMARY(
        titleBn = "বাংলা প্রধান (নতুনদের জন্য)",
        titleEn = "Bengali Primary",
        descriptionBn = "সব নির্দেশনা ও ব্যাখ্যা বাংলায়, সাথে সহজ ইংরেজি"
    ),
    BILINGUAL(
        titleBn = "বাংলা + ইংরেজি (দ্বিভাষিক)",
        titleEn = "Bilingual Mode",
        descriptionBn = "ইংরেজি অনুশীলন ও বাংলা সহায়তা একসাথে"
    ),
    ENGLISH_FOCUS(
        titleBn = "ইংরেজি অনুশীলন বেশি",
        titleEn = "English Focused",
        descriptionBn = "বেশিরভাগ লেখা ইংরেজিতে, প্রয়োজনে বাংলা ব্যাখ্যা"
    )
}

data class TutorPreferencesState(
    val onboardingCompleted: Boolean = false,
    val learnerName: String = "শিক্ষার্থী",
    val languageMode: AppLanguageMode = AppLanguageMode.BENGALI_PRIMARY,
    val dailySpeakingGoalMinutes: Int = 10,
    val dailyVocabGoal: Int = 5,
    val backendBaseUrl: String = BuildConfig.DEFAULT_BACKEND_URL,
    val slowTtsForBeginners: Boolean = true,
    val maxSessionMinutes: Int = 10,
    val dailySessionLimit: Int = 5,
    val dailySessionsUsed: Int = 0,
    val lastSessionDateIso: String = "",
    val darkModeFollowSystem: Boolean = true,
    val darkModeEnabled: Boolean = false
)

class AppPreferencesManager(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LEARNER_NAME = stringPreferencesKey("learner_name")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val DAILY_SPEAKING_GOAL = intPreferencesKey("daily_speaking_goal")
        val DAILY_VOCAB_GOAL = intPreferencesKey("daily_vocab_goal")
        val BACKEND_BASE_URL = stringPreferencesKey("backend_base_url")
        val SLOW_TTS = booleanPreferencesKey("slow_tts")
        val MAX_SESSION_MINUTES = intPreferencesKey("max_session_minutes")
        val DAILY_SESSION_LIMIT = intPreferencesKey("daily_session_limit")
        val DAILY_SESSIONS_USED = intPreferencesKey("daily_sessions_used")
        val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
        val DEVICE_INSTALL_ID = stringPreferencesKey("device_install_id")
        val DARK_MODE_FOLLOW_SYSTEM = booleanPreferencesKey("dark_mode_follow_system")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val preferencesFlow: Flow<TutorPreferencesState> = context.dataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        val storedDate = prefs[Keys.LAST_SESSION_DATE].orEmpty()
        val sessionsToday = if (storedDate == today) prefs[Keys.DAILY_SESSIONS_USED] ?: 0 else 0
        val modeName = prefs[Keys.LANGUAGE_MODE] ?: AppLanguageMode.BENGALI_PRIMARY.name
        val mode = AppLanguageMode.entries.firstOrNull { it.name == modeName }
            ?: AppLanguageMode.BENGALI_PRIMARY

        TutorPreferencesState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            learnerName = prefs[Keys.LEARNER_NAME]?.takeIf { it.isNotBlank() } ?: "শিক্ষার্থী",
            languageMode = mode,
            dailySpeakingGoalMinutes = prefs[Keys.DAILY_SPEAKING_GOAL] ?: 10,
            dailyVocabGoal = prefs[Keys.DAILY_VOCAB_GOAL] ?: 5,
            backendBaseUrl = prefs[Keys.BACKEND_BASE_URL]?.takeIf { it.isNotBlank() }
                ?: BuildConfig.DEFAULT_BACKEND_URL,
            slowTtsForBeginners = prefs[Keys.SLOW_TTS] ?: true,
            maxSessionMinutes = prefs[Keys.MAX_SESSION_MINUTES] ?: 10,
            dailySessionLimit = prefs[Keys.DAILY_SESSION_LIMIT] ?: 5,
            dailySessionsUsed = sessionsToday,
            lastSessionDateIso = storedDate,
            darkModeFollowSystem = prefs[Keys.DARK_MODE_FOLLOW_SYSTEM] ?: true,
            darkModeEnabled = prefs[Keys.DARK_MODE_ENABLED] ?: false
        )
    }

    suspend fun getOrCreateDeviceId(): String {
        val current = context.dataStore.data.first()[Keys.DEVICE_INSTALL_ID]
        if (!current.isNullOrBlank()) return current
        val created = UUID.randomUUID().toString()
        context.dataStore.edit { it[Keys.DEVICE_INSTALL_ID] = created }
        return created
    }

    suspend fun setLanguageMode(mode: AppLanguageMode) {
        context.dataStore.edit { it[Keys.LANGUAGE_MODE] = mode.name }
    }

    suspend fun completeOnboarding(
        learnerName: String,
        speakingMinutes: Int,
        vocabGoal: Int
    ) {
        context.dataStore.edit {
            it[Keys.LEARNER_NAME] = learnerName.trim().ifBlank { "শিক্ষার্থী" }
            it[Keys.DAILY_SPEAKING_GOAL] = speakingMinutes.coerceIn(5, 60)
            it[Keys.DAILY_VOCAB_GOAL] = vocabGoal.coerceIn(3, 30)
            it[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun updateProfileAndGoals(
        learnerName: String,
        languageMode: AppLanguageMode,
        speakingMinutes: Int,
        vocabGoal: Int,
        slowTts: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.LEARNER_NAME] = learnerName.trim().ifBlank { "শিক্ষার্থী" }
            it[Keys.LANGUAGE_MODE] = languageMode.name
            it[Keys.DAILY_SPEAKING_GOAL] = speakingMinutes.coerceIn(5, 60)
            it[Keys.DAILY_VOCAB_GOAL] = vocabGoal.coerceIn(3, 30)
            it[Keys.SLOW_TTS] = slowTts
        }
    }

    suspend fun setBackendBaseUrl(url: String) {
        context.dataStore.edit {
            it[Keys.BACKEND_BASE_URL] = url.trim().trimEnd('/')
        }
    }

    suspend fun setSlowTts(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SLOW_TTS] = enabled }
    }

    suspend fun setDarkTheme(followSystem: Boolean, enabled: Boolean) {
        context.dataStore.edit {
            it[Keys.DARK_MODE_FOLLOW_SYSTEM] = followSystem
            it[Keys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun incrementDailyAiSessionCount() {
        val today = LocalDate.now().toString()
        context.dataStore.edit {
            val prevDate = it[Keys.LAST_SESSION_DATE].orEmpty()
            val current = if (prevDate == today) it[Keys.DAILY_SESSIONS_USED] ?: 0 else 0
            it[Keys.LAST_SESSION_DATE] = today
            it[Keys.DAILY_SESSIONS_USED] = current + 1
        }
    }
}
