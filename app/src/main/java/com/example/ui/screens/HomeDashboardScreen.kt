package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.BengaliText
import com.example.ui.components.FreeAppPill
import com.example.ui.components.GoalProgressRing
import com.example.ui.components.MetricStatCard
import com.example.ui.viewmodel.HomeViewModel

// Screen 5: Home Dashboard
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    viewModel: HomeViewModel,
    onStartAiCall: (String) -> Unit,
    onOpenLesson: (Int) -> Unit,
    onOpenLessonsList: () -> Unit,
    onOpenVocabulary: () -> Unit,
    onOpenGrammar: () -> Unit,
    onOpenPronunciation: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = state.progress
    val topics = listOf(
        "Greetings & Introduction",
        "Shopping Conversation",
        "Daily Routine",
        "Restaurant Order",
        "Office & Interview"
    )
    var selectedTopic by remember { mutableStateOf(topics.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BengaliText(
                            text = "স্বাগতম, ${state.preferences.learnerName}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        BengaliText(
                            text = "আজ একটু একটু করে ইংরেজি বলার চর্চা করি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            }

            item {
                FreeAppPill()
            }

            // Hero AI Voice Call Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_tutor_avatar),
                                    contentDescription = "AI English Teacher Avatar",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AI Live English Teacher",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    BengaliText(
                                        text = "ভয় ছাড়াই কথা বলুন • ভুল হলে বাংলায় বুঝিয়ে দেবেন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topics.forEach { topic ->
                                    FilterChip(
                                        selected = selectedTopic == topic,
                                        onClick = { selectedTopic = topic },
                                        label = {
                                            Text(
                                                text = topic,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                                            labelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        border = null
                                    )
                                }
                            }

                            Button(
                                onClick = { onStartAiCall(selectedTopic) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("start_ai_conversation_button"),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                BengaliText(
                                    text = "Start AI Conversation (কথা বলুন)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Daily Goal & Overall Progress Card
            item {
                val speakingToday = progress?.todaySpeakingMinutes ?: 0
                val speakingGoal = state.preferences.dailySpeakingGoalMinutes.coerceAtLeast(1)
                val goalRatio = speakingToday.toFloat() / speakingGoal.toFloat()
                val completedLessons = progress?.completedLessons ?: 0
                val totalLessons = progress?.totalLessons ?: 20

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GoalProgressRing(
                            progress = goalRatio,
                            centerLabel = "${speakingToday}/${speakingGoal}m",
                            captionBn = "আজকের লক্ষ্য"
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BengaliText(
                                text = "দৈনিক শেখার লক্ষ্য ও অগ্রগতি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            BengaliText(
                                text = "লেসন সম্পন্ন: $completedLessons / $totalLessons টি",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { (completedLessons.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            BengaliText(
                                text = "ধাপ: ${progress?.learningLevelTitleBn ?: "একদম নতুন শিক্ষার্থী"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 4 Metric Cards Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricStatCard(
                            icon = Icons.Rounded.LocalFireDepartment,
                            value = "${progress?.currentStreakDays ?: 0} দিন",
                            labelBn = "দৈনিক স্ট্রিক (Streak)",
                            tint = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            icon = Icons.Rounded.Book,
                            value = "${progress?.learnedVocabulary ?: 0}/${progress?.totalVocabulary ?: 64}",
                            labelBn = "শেখা শব্দ (Vocabulary)",
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
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
                            labelBn = "মোট কথা বলার সময়",
                            tint = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            icon = Icons.Rounded.AutoGraph,
                            value = "${((progress?.completedLessons ?: 0) * 100 / (progress?.totalLessons ?: 20))}%",
                            labelBn = "কোর্স অগ্রগতি",
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Continue Learning & Daily Recommended Lesson
            item {
                val recommended = state.recommendedLesson
                val continueLesson = state.continueLesson

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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                BengaliText(
                                    text = "আজকের সুপারিশকৃত লেসন",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (recommended != null) {
                                Text(
                                    text = "Lesson ${recommended.orderIndex}/20",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (recommended != null) {
                            Text(
                                text = recommended.titleEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            BengaliText(
                                text = "${recommended.titleBn} — ${recommended.summaryBn}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val targetId = continueLesson?.id ?: recommended?.id ?: 1
                                    onOpenLesson(targetId)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("continue_learning_button")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.size(6.dp))
                                BengaliText(
                                    text = "Continue Learning",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenLessonsList,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("all_lessons_button")
                            ) {
                                BengaliText("সব লেসন", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Quick Practice Tools
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BengaliText(
                        text = "দ্রুত অনুশীলন ও বিশ্লেষণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    QuickActionRow(
                        icon = Icons.Rounded.Spellcheck,
                        titleBn = "গ্রামার সংশোধন (Grammar Correction)",
                        subtitleBn = "বাক্যের ভুল ধরে বাংলায় সহজ ব্যাখ্যা পান",
                        onClick = onOpenGrammar,
                        testTag = "quick_grammar_card"
                    )
                    QuickActionRow(
                        icon = Icons.Rounded.Mic,
                        titleBn = "উচ্চারণ অনুশীলন (Pronunciation Practice)",
                        subtitleBn = "বাক্য শুনে নিজে বলুন ও স্পষ্টতা যাচাই করুন",
                        onClick = onOpenPronunciation,
                        testTag = "quick_pronunciation_card"
                    )
                    QuickActionRow(
                        icon = Icons.Rounded.Book,
                        titleBn = "শব্দভান্ডার (Vocabulary Builder)",
                        subtitleBn = "অডিও ও ফ্ল্যাশকার্ডসহ প্রয়োজনীয় ইংরেজি শব্দ",
                        onClick = onOpenVocabulary,
                        testTag = "quick_vocab_card"
                    )
                    QuickActionRow(
                        icon = Icons.Rounded.AutoGraph,
                        titleBn = "শেখার অগ্রগতি (Progress Dashboard)",
                        subtitleBn = "সাপ্তাহিক চর্চার চার্ট ও স্ট্রিক দেখুন",
                        onClick = onOpenProgress,
                        testTag = "quick_progress_card"
                    )
                    QuickActionRow(
                        icon = Icons.Rounded.History,
                        titleBn = "কথোপকথনের ইতিহাস (History)",
                        subtitleBn = "আগের ভয়েস চ্যাটের ট্রান্সক্রিপ্ট ও নোট দেখুন",
                        onClick = onOpenHistory,
                        testTag = "quick_history_card"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    titleBn: String,
    subtitleBn: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                BengaliText(
                    text = titleBn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                BengaliText(
                    text = subtitleBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
