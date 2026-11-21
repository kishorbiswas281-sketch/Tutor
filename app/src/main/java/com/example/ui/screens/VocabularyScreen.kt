package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Style
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.VocabularyItem
import com.example.ui.components.BengaliText
import com.example.ui.components.EmptyStateCard
import com.example.ui.viewmodel.VocabularyViewModel

// Screen 11: Vocabulary Builder Screen
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel
) {
    val words by viewModel.words.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()
    val summary by viewModel.progressSummary.collectAsStateWithLifecycle()

    var revisionMode by remember { mutableStateOf(false) }
    var flashcardIndex by remember { mutableIntStateOf(0) }
    var showFlashcardAnswer by remember { mutableStateOf(false) }

    val todayCount = summary?.todayVocabCount ?: 0
    val dailyGoal = (summary?.dailyVocabGoal ?: 5).coerceAtLeast(1)

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BengaliText(
                        text = "শব্দভান্ডার তৈরি করুন (Vocabulary)",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BengaliText(
                                text = "আজকের শব্দ লক্ষ্য: $todayCount / $dailyGoal টি শব্দ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            LinearProgressIndicator(
                                progress = { (todayCount.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { BengaliText("ইংরেজি বা বাংলা শব্দ খুঁজুন...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_search_input"),
                    shape = MaterialTheme.shapes.small
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !favoritesOnly && !revisionMode,
                        onClick = {
                            viewModel.setFavoritesOnly(false)
                            revisionMode = false
                        },
                        label = { BengaliText("সব শব্দ (${words.size})") }
                    )
                    FilterChip(
                        selected = favoritesOnly && !revisionMode,
                        onClick = {
                            viewModel.setFavoritesOnly(true)
                            revisionMode = false
                        },
                        label = { BengaliText("পছন্দের শব্দ") }
                    )
                    FilterChip(
                        selected = revisionMode,
                        onClick = {
                            revisionMode = !revisionMode
                            flashcardIndex = 0
                            showFlashcardAnswer = false
                        },
                        leadingIcon = { Icon(Icons.Rounded.Style, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { BengaliText("রিভিশন") },
                        modifier = Modifier.testTag("vocab_revision_chip")
                    )
                }
            }

            if (words.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.Bookmark,
                        titleBn = "কোনো শব্দ পাওয়া যায়নি",
                        subtitleBn = "খোঁজার শব্দ পরিবর্তন করুন অথবা পছন্দের ফিল্টার বন্ধ করুন।"
                    )
                }
            } else if (revisionMode) {
                item {
                    val safeIndex = flashcardIndex.coerceIn(0, words.lastIndex)
                    val cardWord = words[safeIndex]
                    FlashcardRevisionCard(
                        item = cardWord,
                        index = safeIndex + 1,
                        total = words.size,
                        showAnswer = showFlashcardAnswer,
                        onToggleAnswer = { showFlashcardAnswer = !showFlashcardAnswer },
                        onSpeak = { viewModel.speakWord(cardWord.englishWord) },
                        onMarkKnown = {
                            viewModel.markMastered(cardWord, true)
                            showFlashcardAnswer = false
                            flashcardIndex = (safeIndex + 1) % words.size
                        },
                        onNext = {
                            showFlashcardAnswer = false
                            flashcardIndex = (safeIndex + 1) % words.size
                        }
                    )
                }
            } else {
                items(words, key = { it.id }) { word ->
                    VocabularyWordCard(
                        item = word,
                        onSpeak = { viewModel.speakWord("${word.englishWord}. ${word.exampleSentenceEn}") },
                        onToggleFavorite = { viewModel.toggleFavorite(word) },
                        onToggleMastered = { viewModel.markMastered(word, !word.mastered) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardRevisionCard(
    item: VocabularyItem,
    index: Int,
    total: Int,
    showAnswer: Boolean,
    onToggleAnswer: () -> Unit,
    onSpeak: () -> Unit,
    onMarkKnown: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Card $index of $total • ${item.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.englishWord,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            BengaliText(
                text = "উচ্চারণ: ${item.pronunciationBn} (${item.ipaPronunciation})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(onClick = onSpeak) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                BengaliText("শব্দটি শুনুন")
            }

            if (showAnswer) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BengaliText(
                            text = item.bengaliMeaning,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = item.exampleSentenceEn,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        BengaliText(
                            text = item.exampleSentenceBn,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onToggleAnswer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BengaliText("বাংলা অর্থ দেখুন")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.weight(1f)
                ) {
                    BengaliText("পরের শব্দ")
                }
                Button(
                    onClick = onMarkKnown,
                    modifier = Modifier.weight(1f)
                ) {
                    BengaliText("শিখেছি ✓")
                }
            }
        }
    }
}

@Composable
private fun VocabularyWordCard(
    item: VocabularyItem,
    onSpeak: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleMastered: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_card_${item.id}"),
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
                        text = item.englishWord,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    BengaliText(
                        text = "${item.bengaliMeaning} • উচ্চারণ: ${item.pronunciationBn}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.ipaPronunciation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.testTag("vocab_speak_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = "Play word audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("vocab_fav_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite word",
                            tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.exampleSentenceEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    BengaliText(
                        text = item.exampleSentenceBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                FilterChip(
                    selected = item.mastered,
                    onClick = onToggleMastered,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        BengaliText(if (item.mastered) "শেখা হয়েছে" else "শেখা চিহ্নিত করুন")
                    }
                )
            }
        }
    }
}
