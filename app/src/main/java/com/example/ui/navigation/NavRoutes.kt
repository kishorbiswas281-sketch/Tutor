package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Splash : AppRoute

    @Serializable
    data object Welcome : AppRoute

    @Serializable
    data object LanguageSelection : AppRoute

    @Serializable
    data object GoalSetup : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data class VoiceCall(val topicTitle: String = "Everyday Conversation") : AppRoute

    @Serializable
    data class Transcript(val conversationId: Long) : AppRoute

    @Serializable
    data class GrammarCorrection(val initialSentence: String = "") : AppRoute

    @Serializable
    data object Lessons : AppRoute

    @Serializable
    data class LessonDetail(val lessonId: Int) : AppRoute

    @Serializable
    data object Vocabulary : AppRoute

    @Serializable
    data class Pronunciation(val targetSentence: String = "") : AppRoute

    @Serializable
    data object Progress : AppRoute

    @Serializable
    data object History : AppRoute

    @Serializable
    data object Settings : AppRoute
}
