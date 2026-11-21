package com.example.domain.engine

import com.example.domain.model.PronunciationResult
import kotlin.math.max
import kotlin.math.min

object PronunciationEvaluator {

    fun evaluate(targetSentence: String, recognizedSpeech: String): PronunciationResult {
        val cleanTarget = targetSentence.trim()
        val cleanRecognized = recognizedSpeech.trim()

        if (cleanRecognized.isBlank()) {
            return PronunciationResult(
                targetSentence = cleanTarget,
                recognizedSpeech = "",
                matchPercent = 0,
                feedbackBn = "কোনো কথা শনাক্ত হয়নি। মাইক্রোফোনের কাছে গিয়ে স্পষ্টভাবে আবার বলুন।",
                difficultWords = tokenize(cleanTarget),
                phoneticTipsBn = buildTipsForWords(tokenize(cleanTarget)),
                assessmentMethodNoteBn = "নোট: এটি স্পিচ-টু-টেক্সট শব্দ-শনাক্তকরণ ভিত্তিক মূল্যায়ন; সূক্ষ্ম ধ্বনিগত (phonetic) বিশ্লেষণের জন্য AI ভয়েস কলের সাথে অনুশীলন করুন।"
            )
        }

        val targetTokens = tokenize(cleanTarget)
        val spokenTokens = tokenize(cleanRecognized)

        val matchedWords = mutableListOf<String>()
        val difficultWords = mutableListOf<String>()

        for (targetWord in targetTokens) {
            val foundExactOrClose = spokenTokens.any { spoken ->
                spoken == targetWord || levenshteinSimilarity(targetWord, spoken) >= 0.78f
            }
            if (foundExactOrClose) {
                matchedWords.add(targetWord)
            } else {
                difficultWords.add(targetWord)
            }
        }

        val wordRatio = if (targetTokens.isEmpty()) 0f else matchedWords.size.toFloat() / targetTokens.size.toFloat()
        val charSim = levenshteinSimilarity(
            targetTokens.joinToString(" "),
            spokenTokens.joinToString(" ")
        )
        val combinedScore = ((wordRatio * 0.7f + charSim * 0.3f) * 100f).toInt().coerceIn(0, 100)

        val feedbackBn = when {
            combinedScore >= 90 -> "চমৎকার! আপনার বাক্যটি খুব স্পষ্টভাবে শোনা গেছে।"
            combinedScore >= 70 -> "ভালো চেষ্টা! বেশিরভাগ শব্দ স্পষ্ট হয়েছে, নিচের শব্দগুলো আরেকটু ধীরে বলুন।"
            combinedScore >= 45 -> "ভালো শুরু! আরও ধীরে প্রতিটি শব্দ আলাদা করে উচ্চারণ করুন।"
            else -> "চিন্তা নেই, প্রথম দিকে এমন হয়। আগে 'শুনুন' বোতামে চাপ দিয়ে শুনে নিয়ে তারপর বলুন।"
        }

        val tips = buildTipsForWords(difficultWords.ifEmpty { targetTokens })

        return PronunciationResult(
            targetSentence = cleanTarget,
            recognizedSpeech = cleanRecognized,
            matchPercent = combinedScore,
            feedbackBn = feedbackBn,
            difficultWords = difficultWords,
            phoneticTipsBn = tips,
            assessmentMethodNoteBn = "মূল্যায়ন পদ্ধতি: আপনার কথার শব্দ-শনাক্তকরণ (Speech Recognition) মিল ও দূরত্ব যাচাই করা হয়েছে; এটি ল্যাবরেটরি ফোনেটিক স্কোর নয়।"
        )
    }

    private fun tokenize(text: String): List<String> =
        text.lowercase()
            .replace(Regex("[^a-z0-9'\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

    private fun buildTipsForWords(words: List<String>): List<String> {
        val tips = mutableListOf<String>()
        if (words.any { it.contains("th") }) {
            tips.add("'th' (যেমন thank, this) বলার সময় জিহ্বার ডগা দুই দাঁতের মাঝে হালকা ছুঁয়ে বাতাস ছাড়ুন।")
        }
        if (words.any { it.contains("v") }) {
            tips.add("'v' (যেমন very, live) বলার সময় উপরের দাঁত নিচের ঠোঁটে হালকা ছুঁয়ে উচ্চারণ করুন।")
        }
        if (words.any { it.contains("w") }) {
            tips.add("'w' (যেমন water, we) বলার সময় দুই ঠোঁট গোল করে 'উঅ'-এর মতো বলুন।")
        }
        if (words.any { it.contains("f") }) {
            tips.add("'f' (যেমন fine, family) বলার সময় দুই ঠোঁট বন্ধ না করে নিচের ঠোঁটে বাতাস ছাড়ুন।")
        }
        if (words.any { it.contains("sh") }) {
            tips.add("'sh' (যেমন she, shirt) উচ্চারণ বাংলা 'শ'-এর মতো কোমল রাখুন।")
        }
        if (tips.isEmpty()) {
            tips.add("প্রতিটি শব্দের শেষ অক্ষর (যেমন t, d, s) স্পষ্টভাবে উচ্চারণ করার চেষ্টা করুন।")
        }
        return tips.take(3)
    }

    private fun levenshteinSimilarity(a: String, b: String): Float {
        if (a.isEmpty() && b.isEmpty()) return 1f
        if (a.isEmpty() || b.isEmpty()) return 0f
        val maxLen = max(a.length, b.length)
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return 1f - (dp[b.length].toFloat() / maxLen.toFloat())
    }
}
