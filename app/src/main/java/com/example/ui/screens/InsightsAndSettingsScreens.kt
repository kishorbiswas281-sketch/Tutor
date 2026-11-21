package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AppLanguageMode
import com.example.ui.components.BengaliText
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FreeAppPill
import com.example.ui.components.MetricStatCard
import com.example.ui.components.TutorTopBar
import com.example.ui.components.WeeklySpeakingChart
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Screen 13: Progress Dashboard Screen
@Composable
fun ProgressDashboardScreen(
    homeViewModel: HomeViewModel,
    onBack: (() -> Unit)? = null
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()
    val progress = state.progress

    Scaffold(
        topBar = {
            if (onBack != null) {
                TutorTopBar(
                    title = "Learning Progress",
                    subtitleBn = "আপনার শেখার সাপ্তাহিক ও মোট অগ্রগতি",
                    onBackClick = onBack
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (onBack == null) {
                    item {
                        BengaliText(
                            text = "শেখার অগ্রগতি (Progress)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Column {
                                Text(
                                    text = progress?.learningLevelTitleEn ?: "Absolute Beginner",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                BengaliText(
                                    text = "বর্তমান ধাপ: ${progress?.learningLevelTitleBn ?: "একদম নতুন শিক্ষার্থী"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                icon = Icons.Rounded.CheckCircle,
                                value = "${progress?.completedLessons ?: 0}/${progress?.totalLessons ?: 20}",
                                labelBn = "সম্পন্ন লেসন",
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                icon = Icons.Rounded.Book,
                                value = "${progress?.learnedVocabulary ?: 0}",
                                labelBn = "শেখা শব্দ",
                                tint = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                icon = Icons.Rounded.Schedule,
                                value = "${progress?.totalSpeakingMinutes ?: 0} মি.",
                                labelBn = "কথা বলার সময়",
                                tint = MaterialTheme.colorScheme.tertiary,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                icon = Icons.Rounded.LocalFireDepartment,
                                value = "${progress?.currentStreakDays ?: 0} দিন",
                                labelBn = "ধারাবাহিক স্ট্রিক",
                                tint = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spellcheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                BengaliText(
                                    text = "গ্রামার উন্নতি ও সংশোধন: ${progress?.grammarCorrectionsCount ?: 0} টি",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                BengaliText(
                                    text = "সব অগ্রগতি আপনার ফোনের লোকাল ডাটাবেসে নিরাপদে সংরক্ষিত থাকে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoGraph,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                BengaliText(
                                    text = "গত ৭ দিনের কথা বলার অনুশীলন (মিনিট)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            WeeklySpeakingChart(stats = progress?.weeklyStats ?: emptyList())
                        }
                    }
                }
            }
        }
    }
}

// Screen 14: Conversation History Screen
@Composable
fun ConversationHistoryScreen(
    viewModel: HistoryViewModel,
    onBack: (() -> Unit)? = null,
    onOpenTranscript: (Long) -> Unit
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (onBack != null) {
                TutorTopBar(
                    title = "Conversation History",
                    subtitleBn = "আগের ভয়েস চ্যাট ও শিক্ষকের নোট",
                    onBackClick = onBack
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onBack == null) {
                    item {
                        BengaliText(
                            text = "কথোপকথনের ইতিহাস (History)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (conversations.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.History,
                            titleBn = "এখনো কোনো কথোপকথন সংরক্ষিত নেই",
                            subtitleBn = "AI শিক্ষকের সাথে ভয়েস কল শেষ করলে ট্রান্সক্রিপ্ট ও ফিডব্যাক এখানে জমা হবে।"
                        )
                    }
                } else {
                    items(conversations, key = { it.id }) { conv ->
                        val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(Date(conv.startedAt))
                        Card(
                            onClick = { onOpenTranscript(conv.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_item_${conv.id}"),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = conv.topicTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteConversation(conv.id) },
                                        modifier = Modifier.testTag("delete_history_${conv.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Delete conversation",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        BengaliText(
                                            text = "${conv.durationSeconds} সেকেন্ড",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        BengaliText(
                                            text = "সংশোধন: ${conv.correctedCount} টি",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                BengaliText(
                                    text = conv.learningFeedbackBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Screen 15: Settings Screen
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val health by viewModel.healthState.collectAsStateWithLifecycle()
    val savedFeedback by viewModel.savedFeedbackBn.collectAsStateWithLifecycle()

    var nameInput by remember(prefs.learnerName) { mutableStateOf(prefs.learnerName) }
    var selectedMode by remember(prefs.languageMode) { mutableStateOf(prefs.languageMode) }
    var speakingGoal by remember(prefs.dailySpeakingGoalMinutes) { mutableIntStateOf(prefs.dailySpeakingGoalMinutes) }
    var vocabGoal by remember(prefs.dailyVocabGoal) { mutableIntStateOf(prefs.dailyVocabGoal) }
    var slowTts by remember(prefs.slowTtsForBeginners) { mutableStateOf(prefs.slowTtsForBeginners) }
    var backendUrlInput by remember(prefs.backendBaseUrl) { mutableStateOf(prefs.backendBaseUrl) }

    LaunchedEffect(savedFeedback) {
        if (savedFeedback != null) {
            kotlinx.coroutines.delay(2500L)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TutorTopBar(
                title = "Settings",
                subtitleBn = "ভাষা, লক্ষ্য, সার্ভার ও অডিও সেটিংস",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!savedFeedback.isNullOrBlank()) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BengaliText(
                                text = savedFeedback!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Profile & Goals
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BengaliText(
                                text = "প্রোফাইল ও শেখার লক্ষ্য",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { BengaliText("শিক্ষার্থীর নাম") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            BengaliText("ভাষার ধরন:", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppLanguageMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = selectedMode == mode,
                                        onClick = { selectedMode = mode },
                                        label = { BengaliText(mode.titleBn) }
                                    )
                                }
                            }

                            BengaliText("দৈনিক কথা বলার লক্ষ্য:", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 20).forEach { m ->
                                    FilterChip(
                                        selected = speakingGoal == m,
                                        onClick = { speakingGoal = m },
                                        label = { BengaliText("$m মিনিট") }
                                    )
                                }
                            }

                            BengaliText("দৈনিক শব্দ লক্ষ্য:", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(3, 5, 10, 15).forEach { v ->
                                    FilterChip(
                                        selected = vocabGoal == v,
                                        onClick = { vocabGoal = v },
                                        label = { BengaliText("$v শব্দ") }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    BengaliText(
                                        text = "নতুনদের জন্য ধীরে অডিও উচ্চারণ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    BengaliText(
                                        text = "লেসন ও শব্দভান্ডারে ধীরে স্পষ্ট শোনানো হবে",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = slowTts,
                                    onCheckedChange = { slowTts = it }
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.saveProfile(
                                        learnerName = nameInput,
                                        languageMode = selectedMode,
                                        speakingMinutes = speakingGoal,
                                        vocabGoal = vocabGoal,
                                        slowTts = slowTts
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("settings_save_button")
                            ) {
                                BengaliText("প্রোফাইল সংরক্ষণ করুন")
                            }
                        }
                    }
                }

                // Backend Server Configuration
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BengaliText(
                                text = "AI ভয়েস ব্যাকএন্ড সার্ভার (OpenAI Realtime)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            BengaliText(
                                text = "নিরাপত্তার জন্য OpenAI API কী শুধু আপনার ব্যাকএন্ড সার্ভারে থাকে, অ্যাপে নয়।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = backendUrlInput,
                                onValueChange = { backendUrlInput = it },
                                label = { Text("Backend Base URL") },
                                placeholder = { Text("https://your-backend.example.com") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_backend_url_input")
                            )
                            OutlinedButton(
                                onClick = { viewModel.saveAndTestBackendUrl(backendUrlInput) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_test_backend_button")
                            ) {
                                BengaliText(
                                    if (health.checking) "সংযোগ পরীক্ষা চলছে..." else "সার্ভার সংযোগ যাচাই ও সেভ করুন"
                                )
                            }
                            health.statusMessageBn?.let { msg ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (health.isHealthy) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    }
                                ) {
                                    BengaliText(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                            BengaliText(
                                text = "আজকের লাইভ সেশন ব্যবহার: ${prefs.dailySessionsUsed}/${prefs.dailySessionLimit} • সেশন সীমা: ${prefs.maxSessionMinutes} মিনিট",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Theme & Data Management
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BengaliText("ডার্ক মোড (Dark Theme)", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = !prefs.darkModeFollowSystem && prefs.darkModeEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.setDarkTheme(followSystem = false, darkEnabled = enabled)
                                    }
                                )
                            }
                            OutlinedButton(
                                onClick = viewModel::clearHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_clear_history_button")
                            ) {
                                BengaliText(
                                    text = "সব কথোপকথনের ইতিহাস মুছুন",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Free & Privacy Transparency
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FreeAppPill()
                            BengaliText(
                                text = "AI English Tutor সম্পূর্ণ ফ্রি। কোনো বিজ্ঞাপন, সাবস্ক্রিপশন বা পেমেন্ট গেটওয়ে নেই। আপনার শেখার অগ্রগতি ও ইতিহাস শুধু আপনার ফোনেই সংরক্ষিত থাকে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
