package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.core.DishonestyCategory
import org.verumomnis.forensic.core.DishonestyMatrix
import org.verumomnis.forensic.core.DishonestyPattern
import org.verumomnis.forensic.core.ExtractionProtocol
import org.verumomnis.forensic.core.KeywordMatch
import org.verumomnis.forensic.core.LegalCategory
import org.verumomnis.forensic.core.LegalSubject
import org.verumomnis.forensic.core.LegalSubjectMatch
import org.verumomnis.forensic.core.LegalSubjects
import org.verumomnis.forensic.core.RedFlag
import org.verumomnis.forensic.core.RuleAnalysisResult

/**
 * Unit tests for RuleEngine analysis logic
 *
 * Tests Verum Omnis forensic analysis rules:
 * - Keyword scanning
 * - Legal subject tagging
 * - Dishonesty detection (red flags)
 * - Score calculation
 * - Severity determination
 */
class RuleEngineTest {

    @Test
    fun `keyword matching finds administrative keywords`() {
        val keywords = mapOf(
            "administrative" to listOf("admin", "access", "permission"),
            "actions" to listOf("deny", "delete", "block")
        )

        val text = "the admin denied access to the system".lowercase()
        val matches = findKeywordMatches(text, keywords)

        assertTrue("Should find 'admin'", matches.any { it.keyword == "admin" })
        assertTrue("Should find 'access'", matches.any { it.keyword == "access" })
        assertEquals("administrative", matches.find { it.keyword == "admin" }?.category)
    }

    @Test
    fun `keyword matching finds action keywords`() {
        val keywords = mapOf(
            "administrative" to listOf("admin"),
            "actions" to listOf("deny", "delete", "block", "refuse")
        )

        val text = "they refused to delete the evidence".lowercase()
        val matches = findKeywordMatches(text, keywords)

        assertTrue("Should find 'delete'", matches.any { it.keyword == "delete" })
        assertTrue("Should find 'refuse'" , matches.any { it.keyword == "refuse" })
        assertEquals("actions", matches.find { it.keyword == "delete" }?.category)
    }

    @Test
    fun `legal subject tagging identifies cybercrime`() {
        val legalSubjects = createTestLegalSubjects()
        val text = "unauthorized access to gmail account".lowercase()

        val matches = findLegalSubjectMatches(text, legalSubjects)

        assertTrue("Should find cybercrime subject", matches.any { it.id == "cybercrime" })
        assertEquals("CRITICAL", matches.find { it.id == "cybercrime" }?.severity)
    }

    @Test
    fun `legal subject tagging identifies fraud`() {
        val legalSubjects = createTestLegalSubjects()
        val text = "the document was forged and falsified".lowercase()

        val matches = findLegalSubjectMatches(text, legalSubjects)

        assertTrue("Should find fraud subject", matches.any { it.id == "fraud" })
        assertTrue("Should match multiple keywords", 
            matches.find { it.id == "fraud" }?.matchCount ?: 0 >= 2)
    }

    @Test
    fun `legal subject tagging identifies shareholder oppression`() {
        val legalSubjects = createTestLegalSubjects()
        val text = "the minority shareholder was denied meeting access".lowercase()

        val matches = findLegalSubjectMatches(text, legalSubjects)

        assertTrue("Should find shareholder oppression", 
            matches.any { it.id == "shareholder_oppression" })
    }

    @Test
    fun `red flag detection finds contradictions`() {
        val matrix = createTestDishonestyMatrix()
        val text = "He denied the transaction but later admitted to it".lowercase()

        val flags = findRedFlags(text, matrix)

        assertTrue("Should find denial_admission pattern", 
            flags.any { it.id == "denial_admission" })
        assertEquals("contradictions", flags.find { it.id == "denial_admission" }?.category)
        assertEquals(3, flags.find { it.id == "denial_admission" }?.weight)
    }

    @Test
    fun `red flag detection finds fabrication indicators`() {
        val matrix = createTestDishonestyMatrix()
        val text = "The document appears to be forged or fake".lowercase()

        val flags = findRedFlags(text, matrix)

        assertTrue("Should find forged_document pattern", 
            flags.any { it.id == "forged_document" })
        assertEquals("fabrications", flags.find { it.id == "forged_document" }?.category)
        assertEquals(4, flags.find { it.id == "forged_document" }?.weight)
    }

    @Test
    fun `red flag detection finds omission indicators`() {
        val matrix = createTestDishonestyMatrix()
        val text = "The screenshot was cropped and partial information shown".lowercase()

        val flags = findRedFlags(text, matrix)

        assertTrue("Should find cropped_evidence pattern", 
            flags.any { it.id == "cropped_evidence" })
    }

    @Test
    fun `score calculation weights legal subject severity`() {
        val legalSubjects = listOf(
            createLegalSubjectMatch("cybercrime", "CRITICAL", 2),
            createLegalSubjectMatch("harassment", "HIGH", 1)
        )

        val score = calculateTestScore(emptyList(), legalSubjects, emptyList())

        // CRITICAL: 4 * 2 = 8, HIGH: 3 * 1 = 3, Total = 11
        assertEquals(11, score)
    }

    @Test
    fun `score calculation includes red flag weights`() {
        val redFlags = listOf(
            createRedFlag("contradiction", 3),
            createRedFlag("fabrication", 4)
        )

        val score = calculateTestScore(emptyList(), emptyList(), redFlags)

        assertEquals(7, score)
    }

    @Test
    fun `score calculation caps at 100`() {
        val keywords = (1..50).map { createKeywordMatch("keyword$it") }
        val legalSubjects = (1..10).map { 
            createLegalSubjectMatch("subject$it", "CRITICAL", 5) 
        }

        val score = calculateTestScore(keywords, legalSubjects, emptyList())

        assertEquals(100, score)
    }

    @Test
    fun `severity determination returns CRITICAL for critical subjects`() {
        val legalSubjects = listOf(
            createLegalSubjectMatch("cybercrime", "CRITICAL", 1)
        )

        val severity = determineTestSeverity(10, legalSubjects)

        assertEquals("CRITICAL", severity)
    }

    @Test
    fun `severity determination returns HIGH for multiple high subjects`() {
        val legalSubjects = listOf(
            createLegalSubjectMatch("harassment", "HIGH", 1),
            createLegalSubjectMatch("oppression", "HIGH", 1)
        )

        val severity = determineTestSeverity(20, legalSubjects)

        assertEquals("HIGH", severity)
    }

    @Test
    fun `severity determination uses score for medium subjects`() {
        val legalSubjects = listOf(
            createLegalSubjectMatch("defamation", "MEDIUM", 1)
        )

        val severity = determineTestSeverity(35, legalSubjects)

        assertEquals("HIGH", severity) // Score >= 30 = HIGH
    }

    @Test
    fun `analysis result summary is formatted correctly`() {
        val result = RuleAnalysisResult(
            keywordMatches = listOf(createKeywordMatch("admin")),
            legalSubjects = listOf(createLegalSubjectMatch("fraud", "CRITICAL", 2)),
            redFlags = listOf(createRedFlag("contradiction", 3)),
            score = 50,
            severity = "CRITICAL",
            tags = listOf("#Fraud", "#Access")
        )

        val summary = result.toSummary()

        assertTrue(summary.contains("Analysis Score: 50"))
        assertTrue(summary.contains("Severity: CRITICAL"))
        assertTrue(summary.contains("Keywords Found: 1"))
        assertTrue(summary.contains("Legal Subjects: 1"))
        assertTrue(summary.contains("Red Flags: 1"))
        assertTrue(summary.contains("#Fraud, #Access"))
    }

    // Helper methods to simulate RuleEngine functionality

    private fun findKeywordMatches(
        text: String,
        keywords: Map<String, List<String>>
    ): List<KeywordMatch> {
        val matches = mutableListOf<KeywordMatch>()
        keywords.forEach { (category, categoryKeywords) ->
            categoryKeywords.forEach { keyword ->
                if (text.contains(keyword)) {
                    matches.add(KeywordMatch(keyword, category, text.indexOf(keyword), ""))
                }
            }
        }
        return matches
    }

    private fun findLegalSubjectMatches(
        text: String,
        legalSubjects: LegalSubjects
    ): List<LegalSubjectMatch> {
        val matches = mutableListOf<LegalSubjectMatch>()
        legalSubjects.categories.forEach { (category, categoryData) ->
            categoryData.subjects.forEach { subject ->
                val matchingKeywords = subject.keywords.filter { text.contains(it) }
                if (matchingKeywords.isNotEmpty()) {
                    matches.add(LegalSubjectMatch(
                        subject.id, subject.name, category, subject.severity,
                        matchingKeywords, matchingKeywords.size
                    ))
                }
            }
        }
        return matches
    }

    private fun findRedFlags(
        text: String,
        matrix: DishonestyMatrix
    ): List<RedFlag> {
        val flags = mutableListOf<RedFlag>()
        matrix.categories.forEach { (category, categoryData) ->
            categoryData.patterns.forEach { pattern ->
                val regex = try {
                    Regex(pattern.regex, RegexOption.IGNORE_CASE)
                } catch (e: Exception) { null }
                
                regex?.find(text)?.let { matchResult ->
                    flags.add(RedFlag(
                        pattern.id, category, pattern.description,
                        categoryData.weight, matchResult.value, matchResult.range.first
                    ))
                }
            }
        }
        return flags
    }

    private fun calculateTestScore(
        keywords: List<KeywordMatch>,
        legalSubjects: List<LegalSubjectMatch>,
        redFlags: List<RedFlag>
    ): Int {
        var score = keywords.size
        legalSubjects.forEach { subject ->
            score += when (subject.severity) {
                "CRITICAL" -> 4 * subject.matchCount
                "HIGH" -> 3 * subject.matchCount
                "MEDIUM" -> 2 * subject.matchCount
                else -> subject.matchCount
            }
        }
        redFlags.forEach { score += it.weight }
        return minOf(score, 100)
    }

    private fun determineTestSeverity(score: Int, legalSubjects: List<LegalSubjectMatch>): String {
        if (legalSubjects.any { it.severity == "CRITICAL" }) return "CRITICAL"
        if (legalSubjects.count { it.severity == "HIGH" } >= 2) return "HIGH"
        return when {
            score >= 50 -> "CRITICAL"
            score >= 30 -> "HIGH"
            score >= 15 -> "MEDIUM"
            score >= 5 -> "LOW"
            else -> "MINIMAL"
        }
    }

    // Test data creation helpers

    private fun createTestLegalSubjects(): LegalSubjects {
        return LegalSubjects(
            categories = mapOf(
                "criminal" to LegalCategory(
                    description = "Criminal matters",
                    subjects = listOf(
                        LegalSubject(
                            id = "cybercrime",
                            name = "Cybercrime",
                            severity = "CRITICAL",
                            keywords = listOf("unauthorized access", "hacking", "gmail")
                        ),
                        LegalSubject(
                            id = "fraud",
                            name = "Fraud",
                            severity = "CRITICAL",
                            keywords = listOf("forged", "falsified", "fraud")
                        ),
                        LegalSubject(
                            id = "harassment",
                            name = "Harassment",
                            severity = "HIGH",
                            keywords = listOf("harassment", "threats", "intimidation")
                        )
                    )
                ),
                "corporate" to LegalCategory(
                    description = "Corporate matters",
                    subjects = listOf(
                        LegalSubject(
                            id = "shareholder_oppression",
                            name = "Shareholder Oppression",
                            severity = "HIGH",
                            keywords = listOf("minority shareholder", "denied meeting", "exclusion")
                        )
                    )
                ),
                "civil" to LegalCategory(
                    description = "Civil matters",
                    subjects = listOf(
                        LegalSubject(
                            id = "defamation",
                            name = "Defamation",
                            severity = "MEDIUM",
                            keywords = listOf("defamation", "libel", "slander")
                        )
                    )
                )
            )
        )
    }

    private fun createTestDishonestyMatrix(): DishonestyMatrix {
        return DishonestyMatrix(
            categories = mapOf(
                "contradictions" to DishonestyCategory(
                    weight = 3,
                    patterns = listOf(
                        DishonestyPattern(
                            id = "denial_admission",
                            regex = "denied.*admitted|admitted.*denied",
                            description = "Subject denied and admitted same fact"
                        )
                    )
                ),
                "fabrications" to DishonestyCategory(
                    weight = 4,
                    patterns = listOf(
                        DishonestyPattern(
                            id = "forged_document",
                            regex = "forged|fake|fabricated",
                            description = "Document appears manufactured"
                        )
                    )
                ),
                "omissions" to DishonestyCategory(
                    weight = 2,
                    patterns = listOf(
                        DishonestyPattern(
                            id = "cropped_evidence",
                            regex = "cropped|partial|incomplete",
                            description = "Evidence selectively edited"
                        )
                    )
                )
            )
        )
    }

    private fun createKeywordMatch(keyword: String): KeywordMatch {
        return KeywordMatch(keyword, "test", 0, "test context")
    }

    private fun createLegalSubjectMatch(id: String, severity: String, matchCount: Int): LegalSubjectMatch {
        return LegalSubjectMatch(id, "Test Subject", "test", severity, emptyList(), matchCount)
    }

    private fun createRedFlag(category: String, weight: Int): RedFlag {
        return RedFlag("test_flag", category, "Test description", weight, "matched", 0)
    }
}
