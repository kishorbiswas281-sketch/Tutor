package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.LessonWithProgress
import com.example.ui.components.BengaliErrorBanner
import com.example.ui.components.BengaliText
import com.example.ui.components.TutorTopBar
import com.example.ui.viewmodel.LessonsViewModel

// Screen 9: Beginner Lessons Screen (All 20 structured offline lessons)
@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    onOpenLesson: (Int) -> Unit
) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val allLessons by viewModel.allLessons.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf("All", "Foundations", "Daily Basics", "Grammar", "Conversation", "Real Life", "Workplace", "Vocabulary")
    val completedCount = allLessons.count { it.isCompleted }
    val totalCount = allLessons.size.coerceAtLeast(20)

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BengaliText(
                        text = "শুরু থেকে ইংরেজি কোর্স (২০টি লেসন)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    BengaliText(
                        text = "সবগুলো লেসন সম্পূর্ণ অফলাইনে ব্যবহার করা যায় • সম্পন্ন: $completedCount/$totalCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category) },
                            modifier = Modifier.testTag("lesson_filter_$category")
                        )
                    }
                }
            }

            items(lessons, key = { it.id }) { lesson ->
                LessonRowCard(
                    lesson = lesson,
                    onClick = { onOpenLesson(lesson.id) }
                )
            }
        }
    }
}

@Composable
private fun LessonRowCard(
    lesson: LessonWithProgress,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (lesson.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "${lesson.orderIndex}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = lesson.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    BengaliText(
                        text = "${lesson.estimatedMinutes} মিনিট",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = lesson.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BengaliText(
                    text = lesson.titleBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                BengaliText(
                    text = lesson.summaryBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open lesson",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Screen 10: Lesson Details Screen
@Composable
fun LessonDetailScreen(
    lessonId: Int,
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
    onPracticeSpeaking: (String) -> Unit,
    onStartAiCallForLesson: (String) -> Unit
) {
    val lesson by viewModel.observeLesson(lessonId).collectAsStateWithLifecycle(initialValue = null)
    val ttsError by viewModel.ttsErrorBn.collectAsStateWithLifecycle()
    val selectedAnswers = remember(lessonId) { mutableStateMapOf<Int, Int>() }
    var completionSaved by remember(lessonId) { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TutorTopBar(
                title = lesson?.titleEn ?: "Lesson $lessonId",
                subtitleBn = lesson?.titleBn,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        val current = lesson
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                BengaliText("লেসন লোড হচ্ছে...")
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!ttsError.isNullOrBlank()) {
                    item {
                        BengaliErrorBanner(
                            messageBn = ttsError!!,
                            actionLabelBn = "বন্ধ",
                            onActionClick = { viewModel.clearTtsError() }
                        )
                    }
                }

                // Bengali Explanation Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BengaliText(
                                text = "সহজ বাংলা ব্যাখ্যা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            BengaliText(
                                text = current.explanationBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Examples with Audio Playback
                item {
                    BengaliText(
                        text = "উদাহরণ ও উচ্চারণ (শুনতে স্পিকারে চাপুন)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(current.examples) { example ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = example.english,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                BengaliText(
                                    text = example.bengaliMeaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                BengaliText(
                                    text = "উচ্চারণ: ${example.pronunciationBn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakPhrase(example.english) },
                                modifier = Modifier.testTag("speak_example_${example.english.take(10)}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Listen pronunciation",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onPracticeSpeaking(example.english) }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Practice speaking",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Dialogue Practice
                item {
                    BengaliText(
                        text = "কথোপকথন অনুশীলন (Dialogue)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
                            current.dialogue.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = line.speaker,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = line.english,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        BengaliText(
                                            text = line.bengali,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.speakPhrase(line.english) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.VolumeUp,
                                            contentDescription = "Play dialogue line",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Quiz Practice
                item {
                    BengaliText(
                        text = "ছোট অনুশীলন কুইজ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                current.quiz.forEachIndexed { qIndex, question ->
                    item(key = "quiz_$qIndex") {
                        val chosen = selectedAnswers[qIndex]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BengaliText(
                                    text = "${qIndex + 1}. ${question.questionBn}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                question.options.forEachIndexed { optIndex, option ->
                                    val isChosen = chosen == optIndex
                                    val isCorrect = optIndex == question.correctIndex
                                    val containerColor = when {
                                        chosen == null -> MaterialTheme.colorScheme.surfaceVariant
                                        isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                        isChosen && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                    Surface(
                                        onClick = { selectedAnswers[qIndex] = optIndex },
                                        shape = MaterialTheme.shapes.small,
                                        color = containerColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quiz_${qIndex}_opt_$optIndex")
                                    ) {
                                        BengaliText(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                                if (chosen != null) {
                                    BengaliText(
                                        text = if (chosen == question.correctIndex) {
                                            "সঠিক! ${question.explanationBn}"
                                        } else {
                                            "আরেকটু দেখুন: ${question.explanationBn}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (chosen == question.correctIndex) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Completion & AI Voice CTA
                item {
                    val correctTotal = current.quiz.indices.count { idx ->
                        selectedAnswers[idx] == current.quiz[idx].correctIndex
                    }
                    val scorePercent = if (current.quiz.isEmpty()) 100 else (correctTotal * 100 / current.quiz.size)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                viewModel.completeLesson(current.id, scorePercent)
                                completionSaved++
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("mark_lesson_complete_button")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            BengaliText(
                                text = if (current.isCompleted || completionSaved > 0) {
                                    "লেসন সম্পন্ন হয়েছে (আবার আপডেট করুন)"
                                } else {
                                    "লেসন সম্পন্ন হিসেবে চিহ্নিত করুন"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { onStartAiCallForLesson(current.titleEn) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("lesson_ai_practice_button")
                        ) {
                            BengaliText("এই বিষয়ে AI শিক্ষকের সাথে কথা বলুন")
                        }
                    }
                }
            }
        }
    }
}
