package com.example

import com.example.data.seed.BeginnerCurriculumSeed
import com.example.data.seed.VocabularySeed
import com.example.domain.engine.GrammarCorrectionEngine
import com.example.domain.engine.PronunciationEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorEnginesUnitTest {

    @Test
    fun `grammar engine corrects missing copula in My name Kishor`() {
        val result = GrammarCorrectionEngine.analyzeSentence("My name Kishor")
        assertEquals("My name is Kishor.", result.correctedSentence)
        assertTrue(result.bengaliExplanation.contains("'is'"))
    }

    @Test
    fun `grammar engine corrects I fine and I live Dhaka`() {
        val fineResult = GrammarCorrectionEngine.analyzeSentence("I fine")
        assertEquals("I am fine.", fineResult.correctedSentence)

        val liveResult = GrammarCorrectionEngine.analyzeSentence("I live Dhaka")
        assertEquals("I live in Dhaka.", liveResult.correctedSentence)
    }

    @Test
    fun `grammar engine corrects past negative did not went`() {
        val result = GrammarCorrectionEngine.analyzeSentence("I did not went to school")
        assertEquals("I did not go to school.", result.correctedSentence)
    }

    @Test
    fun `grammar engine parses AI teacher correction with Bengali explanation`() {
        val teacherText = """
            Very good! A more correct sentence is 'My name is Kishor.'
            Bengali explanation: নিজের নাম বলার সময় 'My name is' ব্যবহার করতে হয়.
            Now please repeat: My name is Kishor.
        """.trimIndent()
        val parsed = GrammarCorrectionEngine.parseTeacherCorrectionFromText("My name Kishor", teacherText)
        assertNotNull(parsed)
        assertEquals("My name is Kishor.", parsed?.second)
        assertTrue(parsed?.third?.contains("নিজের নাম") == true)
    }

    @Test
    fun `pronunciation evaluator scores matching speech high and identifies difficult words`() {
        val exact = PronunciationEvaluator.evaluate("I am fine thank you", "I am fine thank you")
        assertTrue(exact.matchPercent >= 95)
        assertTrue(exact.difficultWords.isEmpty())

        val partial = PronunciationEvaluator.evaluate("Water please thank you", "Water please")
        assertTrue(partial.matchPercent in 40..85)
        assertTrue(partial.difficultWords.contains("thank"))
        assertTrue(partial.phoneticTipsBn.any { it.contains("th") })
    }

    @Test
    fun `curriculum seed contains all 20 structured lessons and rich vocabulary`() {
        val lessons = BeginnerCurriculumSeed.allLessons()
        assertEquals(20, lessons.size)
        assertEquals((1..20).toList(), lessons.map { it.id })
        assertTrue(lessons.all { it.titleBn.isNotBlank() && it.explanationBn.isNotBlank() })

        val words = VocabularySeed.allWords()
        assertTrue(words.size >= 60)
        assertTrue(words.all { it.englishWord.isNotBlank() && it.bengaliMeaning.isNotBlank() })
    }
}
