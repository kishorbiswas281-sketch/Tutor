package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.BengaliErrorBanner
import com.example.ui.components.BengaliText
import com.example.ui.components.TutorTopBar
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.viewmodel.LiveTranscriptItem
import com.example.ui.viewmodel.VoiceCallViewModel
import com.example.webrtc.VoiceConnectionState

// Screen 6: AI Voice Call Screen (WebRTC + OpenAI Realtime GA)
@Composable
fun VoiceCallScreen(
    topicTitle: String,
    viewModel: VoiceCallViewModel,
    onBack: () -> Unit,
    onOpenTranscript: (Long) -> Unit,
    onOpenOfflineLessons: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedExplicitly by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        permissionDeniedExplicitly = !granted
        if (granted) {
            viewModel.startVoiceSession(topicTitle)
        }
    }

    LaunchedEffect(topicTitle, hasMicPermission) {
        if (hasMicPermission && state.connectionState == VoiceConnectionState.IDLE) {
            viewModel.startVoiceSession(topicTitle)
        }
    }

    LaunchedEffect(state.transcript.size) {
        if (state.transcript.isNotEmpty()) {
            listState.animateScrollToItem(state.transcript.lastIndex)
        }
    }

    LaunchedEffect(state.savedConversationId) {
        val id = state.savedConversationId
        if (id != null && id > 0L) {
            onOpenTranscript(id)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onAppBackgrounded()
    }

    BackHandler {
        viewModel.endAndSaveSession()
        onBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TutorTopBar(
                title = topicTitle,
                subtitleBn = connectionLabelBn(state.connectionState),
                onBackClick = {
                    viewModel.endAndSaveSession()
                    onBack()
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasMicPermission) {
                    PermissionPromptCard(
                        denied = permissionDeniedExplicitly,
                        onRequestPermission = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onFallbackOffline = onOpenOfflineLessons
                    )
                } else {
                    // Status + Timer Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(state.connectionState)
                                Text(
                                    text = "${formatTimer(state.elapsedSeconds)} / ${formatTimer(state.maxSessionSeconds)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.img_tutor_avatar),
                                contentDescription = "AI English Teacher",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            VoiceWaveformVisualizer(
                                isActive = state.isAiSpeaking || state.isStudentSpeaking,
                                barColor = if (state.isStudentSpeaking) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )

                            BengaliText(
                                text = when {
                                    state.isStudentSpeaking -> "আপনার কথা শুনছি..."
                                    state.isAiSpeaking -> "শিক্ষক কথা বলছেন (কথা বললে থামবেন)..."
                                    state.connectionState == VoiceConnectionState.CONNECTED ->
                                        "ইংরেজিতে ধীরে কথা বলুন, আমি শুনছি"
                                    else -> connectionLabelBn(state.connectionState)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (!state.errorMessageBn.isNullOrBlank()) {
                        BengaliErrorBanner(
                            messageBn = state.errorMessageBn!!,
                            actionLabelBn = if (!state.sessionLimitReached) "আবার চেষ্টা" else null,
                            onActionClick = {
                                if (!state.sessionLimitReached) {
                                    viewModel.startVoiceSession(topicTitle)
                                }
                            }
                        )
                        if (state.connectionState == VoiceConnectionState.ERROR) {
                            OutlinedButton(
                                onClick = onOpenOfflineLessons,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BengaliText("অফলাইন লেসন ও শব্দভান্ডার ব্যবহার করুন")
                            }
                        }
                    }

                    // Live Transcript Feed
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.transcript.isEmpty()) {
                            item {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    BengaliText(
                                        text = "সংযোগ সম্পন্ন হলে শিক্ষক আপনাকে সহজ ইংরেজিতে অভিবাদন জানাবেন। সরাসরি মাইক্রোফোনে উত্তর দিন।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(14.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        items(state.transcript, key = { it.id }) { item ->
                            LiveTranscriptBubble(item)
                        }
                    }

                    // Bottom Call Controls
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FilledIconToggleButton(
                                    checked = !state.isMuted,
                                    onCheckedChange = { viewModel.toggleMute() },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .testTag("voice_mute_button"),
                                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (state.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                        contentDescription = if (state.isMuted) "Unmute microphone" else "Mute microphone"
                                    )
                                }
                                BengaliText(
                                    text = if (state.isMuted) "মিউট" else "মাইক চালু",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FilledIconToggleButton(
                                    checked = state.isSpeakerOn,
                                    onCheckedChange = { viewModel.toggleSpeaker() },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .testTag("voice_speaker_button")
                                ) {
                                    Icon(
                                        imageVector = if (state.isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                                        contentDescription = "Toggle speaker"
                                    )
                                }
                                BengaliText("স্পিকার", style = MaterialTheme.typography.labelSmall)
                            }

                            if (state.isAiSpeaking) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FilledIconToggleButton(
                                        checked = true,
                                        onCheckedChange = { viewModel.bargeInInterrupt() },
                                        modifier = Modifier
                                            .size(52.dp)
                                            .testTag("voice_barge_in_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PanTool,
                                            contentDescription = "Interrupt AI teacher"
                                        )
                                    }
                                    BengaliText("থামান", style = MaterialTheme.typography.labelSmall)
                                }
                            } else if (state.connectionState == VoiceConnectionState.ERROR) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    FilledIconToggleButton(
                                        checked = false,
                                        onCheckedChange = { viewModel.startVoiceSession(topicTitle) },
                                        modifier = Modifier
                                            .size(52.dp)
                                            .testTag("voice_retry_button")
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = "Retry call")
                                    }
                                    BengaliText("পুনরায়", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.endAndSaveSession()
                                    if (state.transcript.isEmpty()) {
                                        onBack()
                                    }
                                },
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("voice_end_call_button"),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Icon(Icons.Rounded.CallEnd, contentDescription = "End conversation")
                                Spacer(modifier = Modifier.size(6.dp))
                                BengaliText("শেষ করুন", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionPromptCard(
    denied: Boolean,
    onRequestPermission: () -> Unit,
    onFallbackOffline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Hearing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            BengaliText(
                text = if (denied) {
                    "কথা বলার জন্য মাইক্রোফোন অনুমতি প্রয়োজন।"
                } else {
                    "AI শিক্ষকের সাথে কথা বলতে মাইক্রোফোনের অনুমতি দিন"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            BengaliText(
                text = "আমরা শুধুমাত্র সরাসরি কথা বলার সময় অডিও পাঠাই এবং কোনো কাঁচা অডিও ফাইল সংরক্ষণ করি না।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("grant_mic_permission_button")
            ) {
                BengaliText("মাইক্রোফোন অনুমতি দিন")
            }
            OutlinedButton(
                onClick = onFallbackOffline,
                modifier = Modifier.fillMaxWidth()
            ) {
                BengaliText("অফলাইন লেসন দেখুন")
            }
        }
    }
}

@Composable
private fun StatusBadge(state: VoiceConnectionState) {
    val (bg, fg) = when (state) {
        VoiceConnectionState.CONNECTED ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        VoiceConnectionState.ERROR ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        else ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        BengaliText(
            text = connectionLabelBn(state),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LiveTranscriptBubble(item: LiveTranscriptItem) {
    val isTeacher = item.speaker == "TEACHER"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isTeacher) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isTeacher) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isTeacher) "AI Teacher" else "You",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.textEn,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!item.correctedSentence.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            BengaliText(
                                text = "সঠিক বাক্য: ${item.correctedSentence}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!item.grammarExplanationBn.isNullOrBlank()) {
                                BengaliText(
                                    text = item.grammarExplanationBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun connectionLabelBn(state: VoiceConnectionState): String = when (state) {
    VoiceConnectionState.IDLE -> "প্রস্তুত হচ্ছে"
    VoiceConnectionState.FETCHING_TOKEN -> "নিরাপদ সেশন তৈরি হচ্ছে..."
    VoiceConnectionState.CONNECTING_WEBRTC -> "ভয়েস সংযোগ হচ্ছে..."
    VoiceConnectionState.CONNECTED -> "লাইভ সংযুক্ত"
    VoiceConnectionState.RECONNECTING -> "পুনঃসংযোগ হচ্ছে..."
    VoiceConnectionState.ERROR -> "সংযোগ সমস্যা"
    VoiceConnectionState.ENDED -> "কল সমাপ্ত"
}

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
