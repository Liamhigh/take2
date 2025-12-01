package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.core.EvidenceType
import org.verumomnis.forensic.core.ForensicCase
import org.verumomnis.forensic.core.ForensicEvidence
import org.verumomnis.forensic.crypto.CryptographicSeal
import org.verumomnis.forensic.report.ForensicNarrativeGenerator
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for ForensicNarrativeGenerator
 *
 * Tests narrative generation per verum-constitution.json output requirements:
 * - clarity: Simple, direct, human-readable
 * - structure: Timeline + Facts + Contradictions + Violations + Guidance
 * - machine_and_human_readable: true
 */
class ForensicNarrativeGeneratorTest {

    private lateinit var narrativeGenerator: ForensicNarrativeGenerator

    @Before
    fun setUp() {
        narrativeGenerator = ForensicNarrativeGenerator()
    }

    @Test
    fun `generateNarrative produces non-empty output`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertNotNull(narrative)
        assertTrue(narrative.isNotEmpty())
    }

    @Test
    fun `generateNarrative includes executive summary`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("EXECUTIVE SUMMARY"))
        assertTrue(narrative.contains(case.name))
        assertTrue(narrative.contains(case.id))
    }

    @Test
    fun `generateNarrative includes timeline analysis`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("TIMELINE ANALYSIS"))
    }

    @Test
    fun `generateNarrative includes evidence facts`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("EVIDENCE FACTS"))
    }

    @Test
    fun `generateNarrative includes contradiction analysis`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("CONTRADICTION ANALYSIS"))
    }

    @Test
    fun `generateNarrative includes integrity assessment`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("INTEGRITY ASSESSMENT"))
        assertTrue(narrative.contains("SHA-512"))
        assertTrue(narrative.contains("HMAC-SHA512"))
    }

    @Test
    fun `generateNarrative includes recommendations`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("RECOMMENDATIONS"))
    }

    @Test
    fun `generateNarrative handles empty evidence list`() {
        val case = ForensicCase(
            id = "test-case-empty",
            name = "Empty Test Case",
            createdAt = Instant.now(),
            directory = File("/tmp/test-case-empty"),
            evidenceItems = mutableListOf()
        )

        val narrative = narrativeGenerator.generateNarrative(case)

        assertNotNull(narrative)
        assertTrue(narrative.contains("0 items of evidence"))
    }

    @Test
    fun `generateNarrative correctly counts evidence types`() {
        val case = createTestCaseWithMultipleTypes()

        val narrative = narrativeGenerator.generateNarrative(case)

        assertTrue(narrative.contains("DOCUMENT: 2"))
        assertTrue(narrative.contains("PHOTO: 1"))
    }

    @Test
    fun `generateNarrative detects time gaps`() {
        val case = createTestCaseWithTimeGap()

        val narrative = narrativeGenerator.generateNarrative(case)

        // Should note significant time gap
        assertTrue(narrative.contains("TIMELINE ANALYSIS"))
    }

    @Test
    fun `narrative is human readable`() {
        val case = createTestCase()

        val narrative = narrativeGenerator.generateNarrative(case)

        // Check for human-readable structure
        assertTrue(narrative.contains("-".repeat(60)))
        assertTrue(narrative.lines().isNotEmpty())
        // Check average line length is reasonable
        val avgLineLength = narrative.lines()
            .filter { it.isNotBlank() }
            .map { it.length }
            .average()
        assertTrue("Average line length should be readable", avgLineLength < 100)
    }

    private fun createTestCase(): ForensicCase {
        val caseDir = File("/tmp/test-case")
        val timestamp = Instant.now()

        val evidence = ForensicEvidence(
            id = "evidence-1",
            type = EvidenceType.DOCUMENT,
            description = "Test document evidence",
            timestamp = timestamp,
            contentHash = "a".repeat(128),
            seal = createTestSeal(timestamp),
            location = null,
            metadata = mapOf("source" to "test"),
            file = File(caseDir, "evidence-1.dat")
        )

        return ForensicCase(
            id = "test-case-123",
            name = "Test Forensic Case",
            createdAt = timestamp,
            directory = caseDir,
            evidenceItems = mutableListOf(evidence)
        )
    }

    private fun createTestCaseWithMultipleTypes(): ForensicCase {
        val caseDir = File("/tmp/test-case-multi")
        val timestamp = Instant.now()

        val evidenceItems = mutableListOf(
            ForensicEvidence(
                id = "evidence-1",
                type = EvidenceType.DOCUMENT,
                description = "Document 1",
                timestamp = timestamp,
                contentHash = "a".repeat(128),
                seal = createTestSeal(timestamp),
                location = null,
                metadata = emptyMap(),
                file = File(caseDir, "evidence-1.dat")
            ),
            ForensicEvidence(
                id = "evidence-2",
                type = EvidenceType.DOCUMENT,
                description = "Document 2",
                timestamp = timestamp.plusSeconds(60),
                contentHash = "b".repeat(128),
                seal = createTestSeal(timestamp.plusSeconds(60)),
                location = null,
                metadata = emptyMap(),
                file = File(caseDir, "evidence-2.dat")
            ),
            ForensicEvidence(
                id = "evidence-3",
                type = EvidenceType.PHOTO,
                description = "Photo evidence",
                timestamp = timestamp.plusSeconds(120),
                contentHash = "c".repeat(128),
                seal = createTestSeal(timestamp.plusSeconds(120)),
                location = null,
                metadata = emptyMap(),
                file = File(caseDir, "evidence-3.dat")
            )
        )

        return ForensicCase(
            id = "test-case-multi",
            name = "Multi-type Test Case",
            createdAt = timestamp,
            directory = caseDir,
            evidenceItems = evidenceItems
        )
    }

    private fun createTestCaseWithTimeGap(): ForensicCase {
        val caseDir = File("/tmp/test-case-gap")
        val timestamp = Instant.now()

        val evidenceItems = mutableListOf(
            ForensicEvidence(
                id = "evidence-1",
                type = EvidenceType.DOCUMENT,
                description = "First evidence",
                timestamp = timestamp,
                contentHash = "a".repeat(128),
                seal = createTestSeal(timestamp),
                location = null,
                metadata = emptyMap(),
                file = File(caseDir, "evidence-1.dat")
            ),
            ForensicEvidence(
                id = "evidence-2",
                type = EvidenceType.DOCUMENT,
                description = "Evidence after 2 hour gap",
                timestamp = timestamp.plus(Duration.ofHours(2)),
                contentHash = "b".repeat(128),
                seal = createTestSeal(timestamp.plus(Duration.ofHours(2))),
                location = null,
                metadata = emptyMap(),
                file = File(caseDir, "evidence-2.dat")
            )
        )

        return ForensicCase(
            id = "test-case-gap",
            name = "Time Gap Test Case",
            createdAt = timestamp,
            directory = caseDir,
            evidenceItems = evidenceItems
        )
    }

    private fun createTestSeal(timestamp: Instant): CryptographicSeal {
        return CryptographicSeal(
            version = "1.0",
            algorithm = "HmacSHA512",
            contentHash = "a".repeat(128),
            timestamp = timestamp,
            location = null,
            metadata = emptyMap(),
            salt = "testsalt123",
            signature = "testsignature456"
        )
    }
}
