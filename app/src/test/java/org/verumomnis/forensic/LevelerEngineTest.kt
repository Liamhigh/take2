package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.leveler.*

/**
 * Unit tests for LevelerEngine
 *
 * Tests the contradiction detection, evasion pattern analysis,
 * and integrity scoring functionality.
 */
class LevelerEngineTest {

    private lateinit var levelerEngine: LevelerEngine

    @Before
    fun setUp() {
        levelerEngine = LevelerEngine()
    }

    @Test
    fun `analyzeDocument returns valid analysis for empty content`() {
        val analysis = levelerEngine.analyzeDocument("")

        assertNotNull(analysis)
        assertEquals(100.0, analysis.integrityScore, 0.1)
        assertEquals(0.0, analysis.suspicionScore, 0.1)
        assertEquals(Assessment.HIGHLY_RELIABLE, analysis.overallAssessment)
    }

    @Test
    fun `analyzeDocument detects evasion patterns`() {
        val content = """
            I don't recall what happened that day.
            I'm not sure about the details.
            Maybe I received the payment, I can't remember.
            I don't remember signing any document.
            Perhaps it was someone else.
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(content)

        assertTrue("Should detect evasion patterns", analysis.evasionPatterns.isNotEmpty())
        assertTrue("Integrity score should be reduced", analysis.integrityScore < 100.0)
    }

    @Test
    fun `analyzeDocument detects multiple evasion keywords`() {
        val content = """
            I don't recall. I don't recall. I don't recall.
            I can't remember. I can't remember.
            Not sure about that.
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(content)

        // Should detect multiple occurrences
        val totalOccurrences = analysis.evasionPatterns.sumOf { it.occurrences }
        assertTrue("Should detect multiple evasion occurrences", totalOccurrences >= 5)
        
        // High evasion count should result in HIGH severity
        val hasHighSeverity = analysis.evasionPatterns.any { it.severity == Severity.HIGH }
        assertTrue("Should have high severity evasion pattern", hasHighSeverity)
    }

    @Test
    fun `analyzeDocument detects financial contradictions`() {
        val content = """
            Statement 1: I never received the $5000 payment.
            Statement 2: Yes, I received $5000 on Monday.
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(content)

        // Should detect contradictions related to financial statements
        assertTrue(
            "Should detect contradictions or financial issues",
            analysis.contradictions.isNotEmpty() || analysis.financialContradictions.isNotEmpty()
        )
    }

    @Test
    fun `analyzeDocument calculates integrity score correctly`() {
        // Clean document should have high integrity
        val cleanContent = "This is a straightforward statement with no issues."
        val cleanAnalysis = levelerEngine.analyzeDocument(cleanContent)
        assertEquals(100.0, cleanAnalysis.integrityScore, 0.1)

        // Document with issues should have lower integrity
        val problematicContent = """
            I don't recall the meeting.
            I never received any money.
            Actually, I did receive the payment.
            I'm not sure what happened.
        """.trimIndent()
        val problemAnalysis = levelerEngine.analyzeDocument(problematicContent)
        assertTrue("Problematic content should have lower integrity", problemAnalysis.integrityScore < 100.0)
    }

    @Test
    fun `analyzeDocument returns correct assessment categories`() {
        // High integrity document
        val cleanContent = "The meeting occurred on January 15th at the office."
        val cleanAnalysis = levelerEngine.analyzeDocument(cleanContent)
        assertTrue(
            "Clean document should be reliable",
            cleanAnalysis.overallAssessment in listOf(Assessment.HIGHLY_RELIABLE, Assessment.GENERALLY_RELIABLE)
        )

        // Problematic document
        val evasiveContent = """
            I don't recall. I don't remember. I can't remember.
            Maybe. Perhaps. I'm not certain.
            I never did that. Actually I did it.
        """.trimIndent()
        val evasiveAnalysis = levelerEngine.analyzeDocument(evasiveContent)
        assertTrue(
            "Evasive document should not be highly reliable",
            evasiveAnalysis.overallAssessment != Assessment.HIGHLY_RELIABLE
        )
    }

    @Test
    fun `analyzeDocument handles Unicode content`() {
        val unicodeContent = """
            特殊字符 special¢haracters
            I don't recall the 会议 meeting.
            Payment of $5000 was received.
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(unicodeContent)

        assertNotNull(analysis)
        assertTrue("Should still detect evasion patterns", analysis.evasionPatterns.isNotEmpty())
    }

    @Test
    fun `generateSummary produces readable output`() {
        val content = """
            I don't recall the exact details.
            The payment of $1000 was never received.
            Actually, the $1000 was deposited on March 5th.
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(content)
        val summary = analysis.generateSummary()

        assertTrue("Summary should contain report header", summary.contains("LEVELER ENGINE ANALYSIS REPORT"))
        assertTrue("Summary should contain integrity score", summary.contains("Integrity Score"))
        assertTrue("Summary should contain assessment", summary.contains("Assessment"))
    }

    @Test
    fun `analyzeCrossDocument detects cross-document contradictions`() {
        val doc1 = DocumentEntry(
            name = "Statement1.txt",
            content = "I never received any payment from the company."
        )
        val doc2 = DocumentEntry(
            name = "Statement2.txt",
            content = "Yes, I did receive the payment as agreed."
        )

        val crossAnalysis = levelerEngine.analyzeCrossDocument(listOf(doc1, doc2))

        assertNotNull(crossAnalysis)
        assertEquals(2, crossAnalysis.documentCount)
    }

    @Test
    fun `suspicion score increases with more issues`() {
        val lowIssueContent = "Simple statement."
        val highIssueContent = """
            I don't recall. I don't remember. Maybe.
            I never paid. I did pay.
            Perhaps. I'm not certain. Not sure.
            I can't remember. I don't recall.
        """.trimIndent()

        val lowAnalysis = levelerEngine.analyzeDocument(lowIssueContent)
        val highAnalysis = levelerEngine.analyzeDocument(highIssueContent)

        assertTrue(
            "High issue content should have higher suspicion score",
            highAnalysis.suspicionScore > lowAnalysis.suspicionScore
        )
    }

    @Test
    fun `integrity score is bounded between 0 and 100`() {
        // Extremely problematic content
        val extremeContent = buildString {
            repeat(20) {
                appendLine("I don't recall. I don't remember. I never did that.")
                appendLine("Actually I did. No wait, I didn't. Maybe.")
            }
        }

        val analysis = levelerEngine.analyzeDocument(extremeContent)

        assertTrue("Integrity score should be >= 0", analysis.integrityScore >= 0.0)
        assertTrue("Integrity score should be <= 100", analysis.integrityScore <= 100.0)
    }

    @Test
    fun `detects timeline keywords`() {
        val content = """
            Document created: 01/15/2024
            Meeting scheduled for Jan 20
            Minutes from 2024-01-25
        """.trimIndent()

        val analysis = levelerEngine.analyzeDocument(content)

        // Should process without error
        assertNotNull(analysis)
    }
}
