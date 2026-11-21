package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.preferences.AppLanguageMode
import com.example.ui.components.BengaliText
import com.example.ui.components.FreeAppPill
import com.example.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

// Screen 1: Splash Screen
@Composable
fun SplashScreen(
    viewModel: OnboardingViewModel,
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val scale = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(950L)
        if (prefs.onboardingCompleted) {
            onNavigateToHome()
        } else {
            onNavigateToWelcome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.RecordVoiceOver,
                    contentDescription = "AI English Tutor Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                text = "AI English Tutor",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            BengaliText(
                text = "বাংলা থেকে ইংরেজি বলার ব্যক্তিগত শিক্ষক",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
            FreeAppPill()
        }
    }
}

// Screen 2: Welcome Screen
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                FreeAppPill()

                Image(
                    painter = painterResource(id = R.drawable.img_welcome_hero),
                    contentDescription = "English learning illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Speak English with Confidence",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                BengaliText(
                    text = "ভয় ছাড়াই শুরু করুন ইংরেজি বলা — একদম শূন্য থেকে ধাপে ধাপে।",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureHighlightRow(
                        icon = Icons.Rounded.GraphicEq,
                        titleBn = "সরাসরি ভয়েস কথোপকথন",
                        subtitleBn = "রিয়েল-টাইম AI শিক্ষকের সাথে কথা বলুন, ভুল হলে বাংলায় বুঝে নিন।"
                    )
                    FeatureHighlightRow(
                        icon = Icons.Rounded.MenuBook,
                        titleBn = "২০টি সম্পূর্ণ অফলাইন লেসন",
                        subtitleBn = "বর্ণমালা থেকে অফিস ও ইন্টারভিউ পর্যন্ত ইন্টারনেট ছাড়াই শিখুন।"
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    BengaliText(
                        text = "অফলাইন লেসন, শব্দভান্ডার, গ্রামার ও উচ্চারণ অনুশীলন সবসময় ফ্রি ও বিজ্ঞাপনমুক্ত। লাইভ AI ভয়েস কলের জন্য ইন্টারনেট ও সার্ভার সংযোগ প্রয়োজন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("welcome_start_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                BengaliText(
                    text = "শুরু করুন (Get Started)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleBn: String,
    subtitleBn: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
    }
}

// Screen 3: Language Selection Screen
@Composable
fun LanguageSelectionScreen(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
                BengaliText(
                    text = "শেখার ভাষা পছন্দ করুন",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose how Bengali & English appear during your lessons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                AppLanguageMode.entries.forEach { mode ->
                    val selected = prefs.languageMode == mode
                    Card(
                        onClick = { viewModel.selectLanguage(mode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("language_option_${mode.name}"),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BengaliText(
                                    text = mode.titleBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = mode.titleEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                BengaliText(
                                    text = mode.descriptionBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("language_continue_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                BengaliText(
                    text = "পরবর্তী ধাপ (Continue)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Screen 4: Learning Goal Setup Screen
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalSetupScreen(
    viewModel: OnboardingViewModel,
    onCompleted: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var speakingMinutes by remember { mutableIntStateOf(10) }
    var vocabGoal by remember { mutableIntStateOf(5) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                BengaliText(
                    text = "আপনার দৈনিক লক্ষ্য ঠিক করুন",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                BengaliText(
                    text = "প্রতিদিন অল্প সময় চর্চা করলেই দ্রুত উন্নতি হবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { BengaliText("আপনার নাম (Your Name)") },
                    placeholder = { Text("e.g. Kishor") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input"),
                    shape = MaterialTheme.shapes.small
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BengaliText(
                        text = "দৈনিক কথা বলার সময় (Speaking Goal)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            FilterChip(
                                selected = speakingMinutes == mins,
                                onClick = { speakingMinutes = mins },
                                label = { BengaliText("$mins মিনিট") },
                                modifier = Modifier.testTag("goal_minutes_$mins")
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BengaliText(
                        text = "দৈনিক নতুন শব্দ শেখা (Daily Vocabulary)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(3, 5, 10, 15).forEach { count ->
                            FilterChip(
                                selected = vocabGoal == count,
                                onClick = { vocabGoal = count },
                                label = { BengaliText("$count শব্দ") },
                                modifier = Modifier.testTag("goal_vocab_$count")
                            )
                        }
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BengaliText(
                            text = "শেখার ধাপ: একদম শুরু (Absolute Beginner)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        BengaliText(
                            text = "AI শিক্ষক খুব ধীরে, সহজ শব্দে ও বাংলা অর্থসহ আপনাকে শেখাবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.completeGoalSetup(
                        learnerName = name.ifBlank { "শিক্ষার্থী" },
                        speakingMinutes = speakingMinutes,
                        vocabGoal = vocabGoal,
                        onComplete = onCompleted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("goal_complete_button"),
                shape = MaterialTheme.shapes.medium
            ) {
                BengaliText(
                    text = "ড্যাশবোর্ডে যান (Start Learning)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
