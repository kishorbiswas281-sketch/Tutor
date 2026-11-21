package com.example.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.screens.ConversationHistoryScreen
import com.example.ui.screens.ConversationTranscriptScreen
import com.example.ui.screens.GoalSetupScreen
import com.example.ui.screens.GrammarCorrectionScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.LessonDetailScreen
import com.example.ui.screens.LessonsScreen
import com.example.ui.screens.ProgressDashboardScreen
import com.example.ui.screens.PronunciationPracticeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VocabularyScreen
import com.example.ui.screens.VoiceCallScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.viewmodel.GrammarViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.LessonsViewModel
import com.example.ui.viewmodel.OnboardingViewModel
import com.example.ui.viewmodel.PronunciationViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TutorViewModelFactory
import com.example.ui.viewmodel.VocabularyViewModel
import com.example.ui.viewmodel.VoiceCallViewModel

private data class TopLevelTab(
    val route: AppRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelTabs = listOf(
    TopLevelTab(AppRoute.Home, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    TopLevelTab(AppRoute.Lessons, "Lessons", Icons.Rounded.School, Icons.Outlined.School, "nav_lessons"),
    TopLevelTab(AppRoute.Vocabulary, "Words", Icons.Rounded.Book, Icons.Outlined.Book, "nav_vocab"),
    TopLevelTab(AppRoute.Progress, "Progress", Icons.Rounded.AutoGraph, Icons.Outlined.AutoGraph, "nav_progress"),
    TopLevelTab(AppRoute.History, "History", Icons.Rounded.History, Icons.Outlined.History, "nav_history")
)

@Composable
fun EnglishTutorNavHost(
    factory: TutorViewModelFactory,
    startDestination: AppRoute = AppRoute.Splash
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val isTopLevelRoute = topLevelTabs.any { tab ->
        currentDest?.hasRoute(tab.route::class) == true
    }

    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
    val lessonsViewModel: LessonsViewModel = viewModel(factory = factory)
    val vocabularyViewModel: VocabularyViewModel = viewModel(factory = factory)
    val grammarViewModel: GrammarViewModel = viewModel(factory = factory)
    val pronunciationViewModel: PronunciationViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (isTopLevelRoute && !useNavRail) {
                    NavigationBar {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDest?.hasRoute(tab.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelRoute && useNavRail) {
                    NavigationRail {
                        topLevelTabs.forEach { tab ->
                            val selected = currentDest?.hasRoute(tab.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<AppRoute.Splash> {
                        SplashScreen(
                            viewModel = onboardingViewModel,
                            onNavigateToWelcome = {
                                navController.navigate(AppRoute.Welcome) {
                                    popUpTo<AppRoute.Splash> { inclusive = true }
                                }
                            },
                            onNavigateToHome = {
                                navController.navigate(AppRoute.Home) {
                                    popUpTo<AppRoute.Splash> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppRoute.Welcome> {
                        WelcomeScreen(
                            onGetStarted = { navController.navigate(AppRoute.LanguageSelection) }
                        )
                    }

                    composable<AppRoute.LanguageSelection> {
                        LanguageSelectionScreen(
                            viewModel = onboardingViewModel,
                            onContinue = { navController.navigate(AppRoute.GoalSetup) }
                        )
                    }

                    composable<AppRoute.GoalSetup> {
                        GoalSetupScreen(
                            viewModel = onboardingViewModel,
                            onCompleted = {
                                navController.navigate(AppRoute.Home) {
                                    popUpTo<AppRoute.Welcome> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppRoute.Home> {
                        HomeDashboardScreen(
                            viewModel = homeViewModel,
                            onStartAiCall = { topic ->
                                navController.navigate(AppRoute.VoiceCall(topicTitle = topic))
                            },
                            onOpenLesson = { lessonId ->
                                navController.navigate(AppRoute.LessonDetail(lessonId = lessonId))
                            },
                            onOpenLessonsList = {
                                navController.navigate(AppRoute.Lessons)
                            },
                            onOpenVocabulary = {
                                navController.navigate(AppRoute.Vocabulary)
                            },
                            onOpenGrammar = {
                                navController.navigate(AppRoute.GrammarCorrection())
                            },
                            onOpenPronunciation = {
                                navController.navigate(AppRoute.Pronunciation())
                            },
                            onOpenProgress = {
                                navController.navigate(AppRoute.Progress)
                            },
                            onOpenHistory = {
                                navController.navigate(AppRoute.History)
                            },
                            onOpenSettings = {
                                navController.navigate(AppRoute.Settings)
                            }
                        )
                    }

                    composable<AppRoute.VoiceCall> { entry ->
                        val route = entry.toRoute<AppRoute.VoiceCall>()
                        val voiceCallViewModel: VoiceCallViewModel = viewModel(factory = factory)
                        VoiceCallScreen(
                            topicTitle = route.topicTitle,
                            viewModel = voiceCallViewModel,
                            onBack = { navController.popBackStack() },
                            onOpenTranscript = { conversationId ->
                                navController.navigate(AppRoute.Transcript(conversationId)) {
                                    popUpTo<AppRoute.VoiceCall> { inclusive = true }
                                }
                            },
                            onOpenOfflineLessons = {
                                navController.navigate(AppRoute.Lessons) {
                                    popUpTo<AppRoute.VoiceCall> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<AppRoute.Transcript> { entry ->
                        val route = entry.toRoute<AppRoute.Transcript>()
                        ConversationTranscriptScreen(
                            conversationId = route.conversationId,
                            viewModel = historyViewModel,
                            onBack = { navController.popBackStack() },
                            onRepeatSentence = { sentence ->
                                navController.navigate(AppRoute.Pronunciation(targetSentence = sentence))
                            }
                        )
                    }

                    composable<AppRoute.GrammarCorrection> { entry ->
                        val route = entry.toRoute<AppRoute.GrammarCorrection>()
                        GrammarCorrectionScreen(
                            initialSentence = route.initialSentence,
                            viewModel = grammarViewModel,
                            onBack = { navController.popBackStack() },
                            onRepeatPractice = { sentence ->
                                navController.navigate(AppRoute.Pronunciation(targetSentence = sentence))
                            }
                        )
                    }

                    composable<AppRoute.Lessons> {
                        LessonsScreen(
                            viewModel = lessonsViewModel,
                            onOpenLesson = { lessonId ->
                                navController.navigate(AppRoute.LessonDetail(lessonId))
                            }
                        )
                    }

                    composable<AppRoute.LessonDetail> { entry ->
                        val route = entry.toRoute<AppRoute.LessonDetail>()
                        LessonDetailScreen(
                            lessonId = route.lessonId,
                            viewModel = lessonsViewModel,
                            onBack = { navController.popBackStack() },
                            onPracticeSpeaking = { sentence ->
                                navController.navigate(AppRoute.Pronunciation(targetSentence = sentence))
                            },
                            onStartAiCallForLesson = { topic ->
                                navController.navigate(AppRoute.VoiceCall(topicTitle = topic))
                            }
                        )
                    }

                    composable<AppRoute.Vocabulary> {
                        VocabularyScreen(viewModel = vocabularyViewModel)
                    }

                    composable<AppRoute.Pronunciation> { entry ->
                        val route = entry.toRoute<AppRoute.Pronunciation>()
                        PronunciationPracticeScreen(
                            initialTargetSentence = route.targetSentence,
                            viewModel = pronunciationViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.Progress> {
                        ProgressDashboardScreen(homeViewModel = homeViewModel)
                    }

                    composable<AppRoute.History> {
                        ConversationHistoryScreen(
                            viewModel = historyViewModel,
                            onOpenTranscript = { id ->
                                navController.navigate(AppRoute.Transcript(id))
                            }
                        )
                    }

                    composable<AppRoute.Settings> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
