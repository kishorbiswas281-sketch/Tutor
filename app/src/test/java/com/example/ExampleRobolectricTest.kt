package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.EnglishTutorDatabase
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.preferences.AppPreferencesManager
import com.example.data.repository.TutorRepository
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.EnglishTutorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: EnglishTutorDatabase
    private lateinit var repository: TutorRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EnglishTutorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val prefs = AppPreferencesManager(context)
        repository = TutorRepository(db, prefs)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("English Tutor", appName)
    }

    @Test
    fun `repository seeds 20 lessons and persists lesson progress and conversation history`() = runBlocking {
        repository.ensureSeeded()
        val lessons = repository.observeLessonsWithProgress().first()
        assertEquals(20, lessons.size)

        repository.markLessonPracticed(lessonId = 1, completed = true, quizScore = 100)
        val updated = repository.observeLessonDetail(1).first()
        assertEquals(true, updated?.isCompleted)
        assertEquals(100, updated?.quizScore)

        val convId = repository.saveConversation(
            topicTitle = "Greetings",
            startedAt = 1000L,
            endedAt = 2000L,
            durationSeconds = 60,
            messages = listOf(
                ConversationMessageEntity(
                    conversationId = 0L,
                    speaker = "STUDENT",
                    textEn = "My name Kishor",
                    originalSentence = "My name Kishor",
                    correctedSentence = "My name is Kishor.",
                    grammarExplanationBn = "নিজের নাম বলার সময় 'is' ব্যবহার করতে হয়।"
                )
            ),
            newVocabulary = listOf("Greeting"),
            feedbackBn = "দারুণ চেষ্টা!",
            mode = "LIVE_WEBRTC"
        )
        assertTrue(convId > 0L)

        val conversations = repository.observeAllConversations().first()
        assertEquals(1, conversations.size)
        assertEquals(1, conversations.first().correctedCount)

        repository.deleteConversation(convId)
        val afterDelete = repository.observeAllConversations().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `welcome screen renders and triggers get started callback`() {
        var clicked = false
        composeTestRule.setContent {
            EnglishTutorTheme {
                WelcomeScreen(onGetStarted = { clicked = true })
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("welcome_start_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue(clicked)
    }
}
