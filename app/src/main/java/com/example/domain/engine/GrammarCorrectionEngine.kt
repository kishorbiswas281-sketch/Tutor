package com.example.domain.engine

import com.example.domain.model.GrammarFeedbackItem
import java.util.UUID

object GrammarCorrectionEngine {

    private data class GrammarRule(
        val regex: Regex,
        val transform: (MatchResult, String) -> Pair<String, String>,
        val examples: List<String>,
        val category: String
    )

    private val rules: List<GrammarRule> = listOf(
        GrammarRule(
            regex = Regex("^my\\s+name\\s+(?!is\\b)([a-zA-Z]+)\\.?$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val name = match.groupValues[1].replaceFirstChar { it.uppercase() }
                "My name is $name." to "নিজের নাম বলার সময় 'My name'-এর পরে অবশ্যই 'is' ব্যবহার করতে হয়।"
            },
            examples = listOf("My name is Kishor.", "My name is Nadia.", "I am Rahim."),
            category = "Be-Verb (is/am/are)"
        ),
        GrammarRule(
            regex = Regex("^i\\s+fine\\.?$", RegexOption.IGNORE_CASE),
            transform = { _, _ ->
                "I am fine." to "'I'-এর পরে নিজের অবস্থা বোঝাতে 'am' বসাতে হয়।"
            },
            examples = listOf("I am fine, thank you.", "I am very happy today.", "I am ready."),
            category = "Be-Verb (is/am/are)"
        ),
        GrammarRule(
            regex = Regex("^what\\s+your\\s+name\\??$", RegexOption.IGNORE_CASE),
            transform = { _, _ ->
                "What is your name?" to "প্রশ্ন করার সময় 'What'-এর পরে 'is' বসাতে হয়।"
            },
            examples = listOf("What is your name?", "What is your job?", "Where is your home?"),
            category = "Question Formation"
        ),
        GrammarRule(
            regex = Regex("^i\\s+live\\s+(?!in\\b|at\\b|near\\b|on\\b)([a-zA-Z]+)\\.?$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val place = match.groupValues[1].replaceFirstChar { it.uppercase() }
                "I live in $place." to "কোনো শহর বা দেশের নাম বলার আগে 'in' ব্যবহার করতে হয়।"
            },
            examples = listOf("I live in Dhaka.", "I live in Bangladesh.", "She lives in Sylhet."),
            category = "Prepositions (in/at)"
        ),
        GrammarRule(
            regex = Regex("^i\\s+am\\s+agree\\.?$", RegexOption.IGNORE_CASE),
            transform = { _, _ ->
                "I agree." to "'agree' নিজেই একটি ক্রিয়া (Verb), তাই এর আগে 'am' বসে না।"
            },
            examples = listOf("I agree with you.", "Yes, I agree.", "We agree on this plan."),
            category = "Common Verb Usage"
        ),
        GrammarRule(
            regex = Regex("^i\\s+no\\s+(understand|know|like|want)(.*)$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val verb = match.groupValues[1].lowercase()
                val rest = match.groupValues[2].trim()
                val suffix = if (rest.isNotEmpty()) " $rest" else ""
                "I do not $verb$suffix." to "না-বোধক বাক্যে শুধু 'no' না বসিয়ে 'do not' ব্যবহার করতে হয়।"
            },
            examples = listOf("I do not understand.", "I do not know.", "I do not want tea."),
            category = "Negative Sentences"
        ),
        GrammarRule(
            regex = Regex("^i\\s+did\\s+not\\s+went(.*)$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val rest = match.groupValues[1].trim()
                val suffix = if (rest.isNotEmpty()) " $rest" else ""
                "I did not go$suffix." to "'did not'-এর পরে Verb-এর Past রূপ (went) না বসে মূল রূপ (go) বসে।"
            },
            examples = listOf("I did not go to office.", "She did not eat rice.", "We did not see him."),
            category = "Past Tense"
        ),
        GrammarRule(
            regex = Regex("^(he|she)\\s+have\\s+(.+)$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val subj = match.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
                val rest = match.groupValues[2].trim().trimEnd('.')
                "$subj has $rest." to "'He' বা 'She'-এর সাথে 'have' না বসে 'has' বসে।"
            },
            examples = listOf("She has a book.", "He has two brothers.", "It has four legs."),
            category = "Subject-Verb Agreement"
        ),
        GrammarRule(
            regex = Regex("^(he|she)\\s+(go|eat|like|work|live)\\s+(.+)$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val subj = match.groupValues[1].lowercase().replaceFirstChar { it.uppercase() }
                val verb = match.groupValues[2].lowercase()
                val verbS = if (verb == "go") "goes" else "${verb}s"
                val rest = match.groupValues[3].trim().trimEnd('.')
                "$subj $verbS $rest." to "বর্তমান কালে 'He' বা 'She'-এর পরে Verb-এর সাথে 's' বা 'es' যোগ হয়।"
            },
            examples = listOf("He goes to school.", "She likes tea.", "He works in an office."),
            category = "Subject-Verb Agreement"
        ),
        GrammarRule(
            regex = Regex("^yesterday\\s+i\\s+go(.*)$", RegexOption.IGNORE_CASE),
            transform = { match, _ ->
                val rest = match.groupValues[1].trim().trimEnd('.')
                val suffix = if (rest.isNotEmpty()) " $rest" else ""
                "Yesterday I went$suffix." to "গতকালের (Yesterday) কথা বলার সময় 'go'-এর Past রূপ 'went' ব্যবহার করতে হয়।"
            },
            examples = listOf("Yesterday I went to the market.", "Last week I went home.", "I went there at 5 PM."),
            category = "Past Tense"
        ),
        GrammarRule(
            regex = Regex("^i\\s+am\\s+student\\.?$", RegexOption.IGNORE_CASE),
            transform = { _, _ ->
                "I am a student." to "একবচন পেশা বা পরিচয়ের আগে 'a' বা 'an' বসাতে হয়।"
            },
            examples = listOf("I am a student.", "He is a teacher.", "She is a doctor."),
            category = "Articles (a/an)"
        )
    )

    fun analyzeSentence(input: String): GrammarFeedbackItem {
        val cleaned = input.trim()
        if (cleaned.isBlank()) {
            return GrammarFeedbackItem(
                id = UUID.randomUUID().toString(),
                originalSentence = "",
                correctedSentence = "",
                bengaliExplanation = "অনুগ্রহ করে পরীক্ষা করার জন্য একটি ইংরেজি বাক্য লিখুন বা বলুন।",
                additionalExamples = emptyList(),
                categoryLabel = "Input Required"
            )
        }

        for (rule in rules) {
            val match = rule.regex.find(cleaned)
            if (match != null) {
                val (corrected, explanation) = rule.transform(match, cleaned)
                return GrammarFeedbackItem(
                    id = UUID.randomUUID().toString(),
                    originalSentence = cleaned,
                    correctedSentence = corrected,
                    bengaliExplanation = explanation,
                    additionalExamples = rule.examples,
                    categoryLabel = rule.category,
                    sourceTimestamp = System.currentTimeMillis()
                )
            }
        }

        val normalized = cleaned.replaceFirstChar { it.uppercase() }
            .let { if (it.endsWith(".") || it.endsWith("?") || it.endsWith("!")) it else "$it." }

        val basicIssue = detectGeneralSpacingOrPronoun(cleaned)
        return if (basicIssue != null) {
            GrammarFeedbackItem(
                id = UUID.randomUUID().toString(),
                originalSentence = cleaned,
                correctedSentence = basicIssue.first,
                bengaliExplanation = basicIssue.second,
                additionalExamples = listOf("I am learning English.", "Thank you for your help."),
                categoryLabel = "Sentence Polish",
                sourceTimestamp = System.currentTimeMillis()
            )
        } else {
            GrammarFeedbackItem(
                id = UUID.randomUUID().toString(),
                originalSentence = cleaned,
                correctedSentence = normalized,
                bengaliExplanation = "বাহ! আপনার বাক্যটি গঠনগতভাবে সঠিক এবং স্পষ্ট। বড় হাতের অক্ষর ও যতিচিহ্ন ঠিক রাখুন।",
                additionalExamples = listOf(normalized),
                categoryLabel = "Correct Sentence",
                sourceTimestamp = System.currentTimeMillis()
            )
        }
    }

    private fun detectGeneralSpacingOrPronoun(input: String): Pair<String, String>? {
        if (Regex("\\bi\\b").containsMatchIn(input)) {
            val fixed = input.replace(Regex("\\bi\\b"), "I")
                .replaceFirstChar { it.uppercase() }
                .let { if (it.endsWith(".") || it.endsWith("?")) it else "$it." }
            return fixed to "ইংরেজিতে 'আমি' বোঝাতে 'I' সবসময় বড় হাতের অক্ষরে (Capital letter) লিখতে হয়।"
        }
        return null
    }

    fun parseTeacherCorrectionFromText(
        studentUtterance: String,
        teacherReply: String
    ): Triple<String, String, String>? {
        val quoteRegex = Regex("['\"]([^'\"]{4,120})['\"]")
        val hasCorrectionCue = teacherReply.contains("correct sentence", ignoreCase = true) ||
            teacherReply.contains("better way", ignoreCase = true) ||
            teacherReply.contains("you can say", ignoreCase = true) ||
            teacherReply.contains("please repeat", ignoreCase = true)
        if (!hasCorrectionCue) return null

        val match = quoteRegex.find(teacherReply) ?: return null
        val corrected = match.groupValues[1].trim()
        val bengaliCue = teacherReply.lines()
            .firstOrNull { line ->
                line.contains("Bengali", ignoreCase = true) ||
                    line.any { it in '\u0980'..'\u09FF' }
            }
            ?.replace("Bengali explanation:", "", ignoreCase = true)
            ?.trim()
            ?: "সঠিক বাক্যটি জোরে বলে অনুশীলন করুন।"

        return Triple(studentUtterance, corrected, bengaliCue)
    }

    fun commonBeginnerMistakes(): List<GrammarFeedbackItem> = listOf(
        GrammarFeedbackItem(
            id = "preset-1",
            originalSentence = "My name Kishor.",
            correctedSentence = "My name is Kishor.",
            bengaliExplanation = "নিজের নাম বলার সময় 'My name' এর পরে 'is' ব্যবহার করতে হয়।",
            additionalExamples = listOf("My name is Rahim.", "My name is Fatema."),
            categoryLabel = "Be-Verb (is/am/are)"
        ),
        GrammarFeedbackItem(
            id = "preset-2",
            originalSentence = "I fine.",
            correctedSentence = "I am fine.",
            bengaliExplanation = "নিজের অবস্থা বা অনুভূতি বোঝাতে 'I'-এর পরে 'am' বসাতে হয়।",
            additionalExamples = listOf("I am happy.", "I am ready to learn."),
            categoryLabel = "Be-Verb (is/am/are)"
        ),
        GrammarFeedbackItem(
            id = "preset-3",
            originalSentence = "I live Dhaka.",
            correctedSentence = "I live in Dhaka.",
            bengaliExplanation = "কোনো শহর বা দেশের নামের আগে 'in' ব্যবহার করতে হয়।",
            additionalExamples = listOf("I live in Chattogram.", "I live in Bangladesh."),
            categoryLabel = "Prepositions (in/at)"
        ),
        GrammarFeedbackItem(
            id = "preset-4",
            originalSentence = "I did not went there.",
            correctedSentence = "I did not go there.",
            bengaliExplanation = "'did not' থাকলে পরবর্তী Verb-টি মূল রূপে (go) ফিরে আসে।",
            additionalExamples = listOf("I did not go to work.", "She did not eat breakfast."),
            categoryLabel = "Past Tense"
        ),
        GrammarFeedbackItem(
            id = "preset-5",
            originalSentence = "He go to market.",
            correctedSentence = "He goes to the market.",
            bengaliExplanation = "বর্তমান কালে He/She-এর পর Verb-এর শেষে 's/es' যোগ হয়।",
            additionalExamples = listOf("She reads a book.", "He speaks clearly."),
            categoryLabel = "Subject-Verb Agreement"
        )
    )
}
