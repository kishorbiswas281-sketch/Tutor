package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.GrammarFeedbackItem
import com.example.ui.components.BengaliErrorBanner
import com.example.ui.components.BengaliText
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.TutorTopBar
import com.example.ui.viewmodel.GrammarViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.PronunciationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Screen 7: Conversation Transcript Screen
@Composable
fun ConversationTranscriptScreen(
    conversationId: Long,
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onRepeatSentence: (String) -> Unit
) {
    val conversation by viewModel.observeConversation(conversationId).collectAsStateWithLifecycle()
    val messages by viewModel.observeMessages(conversationId).collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TutorTopBar(
                title = conversation?.topicTitle ?: "Transcript",
                subtitleBn = "কথোপকথনের পূর্ণ বিবরণ",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                conversation?.let { conv ->
                    item {
                        val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(Date(conv.startedAt))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                BengaliText(
                                    text = "সময়কাল: ${conv.durationSeconds} সেকেন্ড • সংশোধিত বাক্য: ${conv.correctedCount} টি",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                BengaliText(
                                    text = conv.learningFeedbackBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Forum,
                            titleBn = "কোনো ট্রান্সক্রিপ্ট পাওয়া যায়নি",
                            subtitleBn = "এই সেশনে কোনো সংলাপ সংরক্ষিত হয়নি।"
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        val isTeacher = msg.speaker == "TEACHER"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTeacher) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isTeacher) "AI Teacher" else "You",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.speakLine(msg.textEn) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.VolumeUp,
                                            contentDescription = "Play sentence"
                                        )
                                    }
                                }
                                Text(text = msg.textEn, style = MaterialTheme.typography.bodyLarge)
                                if (!msg.correctedSentence.isNullOrBlank()) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            BengaliText(
                                                text = "সঠিক বাক্য: ${msg.correctedSentence}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            if (!msg.grammarExplanationBn.isNullOrBlank()) {
                                                BengaliText(
                                                    text = msg.grammarExplanationBn,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { onRepeatSentence(msg.correctedSentence) },
                                                modifier = Modifier.height(40.dp)
                                            ) {
                                                BengaliText("পুনরায় বলার অনুশীলন", style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Screen 8: Grammar Correction Screen
@Composable
fun GrammarCorrectionScreen(
    initialSentence: String,
    viewModel: GrammarViewModel,
    onBack: () -> Unit,
    onRepeatPractice: (String) -> Unit
) {
    val input by viewModel.inputSentence.collectAsStateWithLifecycle()
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()

    LaunchedEffect(initialSentence) {
        if (initialSentence.isNotBlank()) {
            viewModel.analyzeInput(initialSentence)
        }
    }

    Scaffold(
        topBar = {
            TutorTopBar(
                title = "Grammar Correction",
                subtitleBn = "ভুল ধরুন ও সহজ বাংলায় বুঝুন",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                                text = "আপনার ইংরেজি বাক্য যাচাই করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = input,
                                onValueChange = viewModel::updateInput,
                                placeholder = { Text("Try: My name Kishor  or  I live Dhaka") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grammar_sentence_input"),
                                shape = MaterialTheme.shapes.small
                            )
                            Button(
                                onClick = { viewModel.analyzeInput() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("grammar_analyze_button")
                            ) {
                                Icon(Icons.Rounded.AutoFixHigh, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                BengaliText("সঠিক বাক্য দেখুন", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                items(feedbackList, key = { it.id }) { item ->
                    GrammarFeedbackCard(
                        item = item,
                        onListen = { viewModel.speakSentence(item.correctedSentence) },
                        onRepeatPractice = { onRepeatPractice(item.correctedSentence) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GrammarFeedbackCard(
    item: GrammarFeedbackItem,
    onListen: () -> Unit,
    onRepeatPractice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = item.categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (item.originalSentence.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        BengaliText(
                            text = "আসল বাক্য (Original):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(text = item.originalSentence, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    BengaliText(
                        text = "সঠিক বাক্য (Correct Sentence):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.correctedSentence,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            BengaliText(
                text = "বাংলা ব্যাখ্যা: ${item.bengaliExplanation}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (item.additionalExamples.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BengaliText(
                        text = "আরও সহজ উদাহরণ:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    item.additionalExamples.forEach { ex ->
                        Text(
                            text = "• $ex",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onListen,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    BengaliText("শুনুন")
                }
                Button(
                    onClick = onRepeatPractice,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    BengaliText("বলে চর্চা করুন")
                }
            }
        }
    }
}

// Screen 12: Pronunciation Practice Screen
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationPracticeScreen(
    initialTargetSentence: String,
    viewModel: PronunciationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val targetSentence by viewModel.targetSentence.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val partialSpeech by viewModel.partialSpeech.collectAsStateWithLifecycle()
    val speechError by viewModel.speechErrorBn.collectAsStateWithLifecycle()
    val ttsError by viewModel.ttsErrorBn.collectAsStateWithLifecycle()

    var customSentence by remember { mutableStateOf("") }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startSpeechCapture()
        }
    }

    LaunchedEffect(initialTargetSentence) {
        if (initialTargetSentence.isNotBlank()) {
            viewModel.setTargetSentence(initialTargetSentence)
        }
    }

    Scaffold(
        topBar = {
            TutorTopBar(
                title = "Pronunciation Practice",
                subtitleBn = "শুনে শুনে স্পষ্ট উচ্চারণ অভ্যাস",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.presets.forEach { sentence ->
                            FilterChip(
                                selected = targetSentence == sentence,
                                onClick = { viewModel.setTargetSentence(sentence) },
                                label = { Text(sentence) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customSentence,
                            onValueChange = { customSentence = it },
                            placeholder = { BengaliText("নিজের বাক্য লিখুন...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        )
                        Button(
                            onClick = {
                                viewModel.setTargetSentence(customSentence)
                                customSentence = ""
                            },
                            enabled = customSentence.isNotBlank()
                        ) {
                            BengaliText("সেট")
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BengaliText(
                                text = "অনুশীলনের বাক্য (Target Sentence):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = targetSentence,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = viewModel::listenTargetAudio,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pronunciation_listen_button")
                                ) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                                    Spacer(modifier = Modifier.size(6.dp))
                                    BengaliText("শুনুন")
                                }
                                Button(
                                    onClick = {
                                        if (isListening) {
                                            viewModel.stopSpeechCapture()
                                        } else if (hasMicPermission) {
                                            viewModel.startSpeechCapture()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pronunciation_speak_button"),
                                    colors = if (isListening) {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    BengaliText(if (isListening) "থামান" else "বলুন")
                                }
                            }
                            if (isListening) {
                                BengaliText(
                                    text = if (partialSpeech.isBlank()) "শোনা হচ্ছে... স্পষ্টভাবে বলুন" else "শোনা যাচ্ছে: $partialSpeech",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                val activeError = speechError ?: ttsError
                if (!activeError.isNullOrBlank()) {
                    item {
                        BengaliErrorBanner(
                            messageBn = activeError,
                            actionLabelBn = "ঠিক আছে",
                            onActionClick = viewModel::clearErrors
                        )
                    }
                }

                result?.let { eval ->
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BengaliText(
                                        text = "শনাক্তকৃত কথা (Recognized):",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "Word Match: ${eval.matchPercent}%",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = eval.recognizedSpeech.ifBlank { "(কোনো শব্দ শনাক্ত হয়নি)" },
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                BengaliText(
                                    text = eval.feedbackBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (eval.difficultWords.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BengaliText(
                                            text = "যে শব্দগুলো আবার চেষ্টা করলে ভালো হয়:",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            eval.difficultWords.forEach { w ->
                                                AssistChip(
                                                    onClick = { viewModel.setTargetSentence(w) },
                                                    label = { Text(w) }
                                                )
                                            }
                                        }
                                    }
                                }

                                eval.phoneticTipsBn.forEach { tip ->
                                    BengaliText(
                                        text = "• $tip",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    BengaliText(
                                        text = eval.assessmentMethodNoteBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (hasMicPermission) viewModel.startSpeechCapture()
                                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pronunciation_repeat_button")
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.size(6.dp))
                                    BengaliText("আবার বলুন (Repeat)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
