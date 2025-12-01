package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.crypto.DeviceInfo
import org.verumomnis.forensic.custody.ChainOfCustodyLogger
import org.verumomnis.forensic.custody.IntegrityStatus
import org.verumomnis.forensic.verification.OfflineVerificationEngine
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for OfflineVerificationEngine
 *
 * Tests the offline verification functionality per forensic requirements:
 * - Hash verification (100% offline)
 * - Chain integrity verification
 * - Timestamp validation (against device clock)
 */
class OfflineVerificationEngineTest {

    private lateinit var verificationEngine: OfflineVerificationEngine
    private lateinit var sealingEngine: CryptographicSealingEngine
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        verificationEngine = OfflineVerificationEngine()
        sealingEngine = CryptographicSealingEngine()
        tempDir = File(System.getProperty("java.io.tmpdir"), "verum_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @org.junit.After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    // =========================================================================
    // HASH VERIFICATION TESTS
    // =========================================================================

    @Test
    fun `verifyFileHash returns valid for matching hash`() {
        val content = "Test file content for verification"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val expectedHash = sealingEngine.computeHash(content)
        val result = verificationEngine.verifyFileHash(testFile, expectedHash)

        assertTrue(result.isValid)
        assertEquals(expectedHash, result.actualHash)
        assertEquals("test.txt", result.fileName)
        assertTrue(result.message.contains("VERIFIED"))
    }

    @Test
    fun `verifyFileHash returns invalid for mismatched hash`() {
        val content = "Original content"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val wrongHash = sealingEngine.computeHash("Different content")
        val result = verificationEngine.verifyFileHash(testFile, wrongHash)

        assertFalse(result.isValid)
        assertNotEquals(wrongHash, result.actualHash)
        assertTrue(result.message.contains("MISMATCH"))
    }

    @Test
    fun `verifyFileHash handles non-existent file`() {
        val nonExistentFile = File(tempDir, "nonexistent.txt")
        val result = verificationEngine.verifyFileHash(nonExistentFile, "somehash")

        assertFalse(result.isValid)
        assertTrue(result.message.contains("not found"))
    }

    @Test
    fun `computeFileHash produces consistent hash`() {
        val content = "Test content for hashing"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val hash1 = verificationEngine.computeFileHash(testFile)
        val hash2 = verificationEngine.computeFileHash(testFile)

        assertEquals(hash1, hash2)
        assertEquals(128, hash1.length) // SHA-512
    }

    @Test
    fun `computeHash matches sealingEngine hash`() {
        val data = "Test data".toByteArray()

        val verificationHash = verificationEngine.computeHash(data)
        val sealingHash = sealingEngine.computeHash(data)

        assertEquals(sealingHash, verificationHash)
    }

    // =========================================================================
    // SEAL VERIFICATION TESTS
    // =========================================================================

    @Test
    fun `verifySeal validates correct seal`() {
        val content = "Test content for seal".toByteArray()
        val contentHash = sealingEngine.computeHash(content)
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val result = verificationEngine.verifySeal(seal, content)

        assertTrue(result.isValid)
        assertTrue(result.message.contains("VERIFIED"))
    }

    @Test
    fun `verifySeal detects tampered content`() {
        val originalContent = "Original content".toByteArray()
        val tamperedContent = "Tampered content".toByteArray()
        val contentHash = sealingEngine.computeHash(originalContent)
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val result = verificationEngine.verifySeal(seal, tamperedContent)

        assertFalse(result.isValid)
        assertTrue(result.message.contains("INVALID"))
    }

    // =========================================================================
    // TRIPLE HASH SEAL VERIFICATION TESTS
    // =========================================================================

    @Test
    fun `verifyTripleHashSeal validates correct seal`() {
        val content = "Test content".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)
        val seal = sealingEngine.createTripleHashSeal(content, emptyMap(), deviceInfo, "Case")

        val result = verificationEngine.verifyTripleHashSeal(seal, content)

        assertTrue(result.isValid)
        assertTrue(result.contentIntact)
        assertTrue(result.metadataIntact)
        assertTrue(result.sealIntact)
    }

    @Test
    fun `verifyTripleHashSeal detects tampered content`() {
        val originalContent = "Original".toByteArray()
        val tamperedContent = "Tampered".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)
        val seal = sealingEngine.createTripleHashSeal(originalContent, emptyMap(), deviceInfo, "Case")

        val result = verificationEngine.verifyTripleHashSeal(seal, tamperedContent)

        assertFalse(result.isValid)
        assertFalse(result.contentIntact)
    }

    // =========================================================================
    // CHAIN INTEGRITY VERIFICATION TESTS
    // =========================================================================

    @Test
    fun `verifyChainIntegrity validates intact chain`() {
        val custodyLogger = ChainOfCustodyLogger()
        custodyLogger.logDocumentUpload("hash1".repeat(25), "user1", "device1")
        custodyLogger.logDocumentProcessing("hash2".repeat(25), "user1", "device1")

        val result = verificationEngine.verifyChainIntegrity(custodyLogger)

        assertTrue(result.isValid)
        assertEquals(IntegrityStatus.VERIFIED, result.status)
        assertEquals(2, result.entryCount)
        assertTrue(result.message.contains("intact"))
    }

    @Test
    fun `verifyChainIntegrity validates empty chain`() {
        val custodyLogger = ChainOfCustodyLogger()

        val result = verificationEngine.verifyChainIntegrity(custodyLogger)

        assertTrue(result.isValid)
        assertEquals(IntegrityStatus.VERIFIED, result.status)
        assertEquals(0, result.entryCount)
    }

    // =========================================================================
    // TIMESTAMP VALIDATION TESTS
    // =========================================================================

    @Test
    fun `validateTimestamp validates recent timestamp`() {
        val recentTimestamp = Instant.now().minusSeconds(30)

        val result = verificationEngine.validateTimestamp(recentTimestamp)

        assertTrue(result.isValid)
        assertFalse(result.isFuture)
        assertFalse(result.isTooOld)
        assertTrue(result.message.contains("VALID"))
    }

    @Test
    fun `validateTimestamp detects future timestamp`() {
        val futureTimestamp = Instant.now().plusSeconds(120)

        val result = verificationEngine.validateTimestamp(futureTimestamp)

        assertFalse(result.isValid)
        assertTrue(result.isFuture)
        assertTrue(result.message.contains("future"))
    }

    @Test
    fun `validateTimestamp detects very old timestamp`() {
        val oldTimestamp = Instant.now().minus(Duration.ofDays(5000))

        val result = verificationEngine.validateTimestamp(oldTimestamp)

        assertFalse(result.isValid)
        assertTrue(result.isTooOld)
        assertTrue(result.message.contains("old"))
    }

    @Test
    fun `validateTimestamp allows within threshold future`() {
        val nearFutureTimestamp = Instant.now().plusSeconds(30)

        val result = verificationEngine.validateTimestamp(nearFutureTimestamp, allowedFutureSeconds = 60)

        assertTrue(result.isValid)
    }

    // =========================================================================
    // COMPREHENSIVE VERIFICATION TESTS
    // =========================================================================

    @Test
    fun `performComprehensiveVerification validates all components`() {
        val content = "Test content for comprehensive verification"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val contentHash = sealingEngine.computeHash(content)
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val result = verificationEngine.performComprehensiveVerification(testFile, seal)

        assertTrue(result.isValid)
        assertTrue(result.hashVerification.isValid)
        assertTrue(result.sealVerification.isValid)
        assertTrue(result.timestampValidation.isValid)
        assertTrue(result.message.contains("PASSED"))
    }

    @Test
    fun `performComprehensiveVerification detects hash mismatch`() {
        val originalContent = "Original content"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Tampered content")

        val originalHash = sealingEngine.computeHash(originalContent)
        val seal = sealingEngine.createSeal(originalHash, Instant.now(), null, emptyMap())

        val result = verificationEngine.performComprehensiveVerification(testFile, seal)

        assertFalse(result.isValid)
        assertFalse(result.hashVerification.isValid)
    }

    @Test
    fun `generateVerificationInstructions produces valid instructions`() {
        val content = "Test content".toByteArray()
        val deviceInfo = DeviceInfo("Samsung", "Galaxy S21", "13", 33)
        val seal = sealingEngine.createTripleHashSeal(content, emptyMap(), deviceInfo, "Test Case")

        val instructions = verificationEngine.generateVerificationInstructions(seal)

        assertTrue(instructions.contains("INDEPENDENT HASH VERIFICATION"))
        assertTrue(instructions.contains("SHA-512"))
        assertTrue(instructions.contains("openssl"))
        assertTrue(instructions.contains("sha512sum"))
        assertTrue(instructions.contains("Test Case"))
        assertTrue(instructions.contains("Samsung"))
        assertTrue(instructions.contains("100% offline"))
    }

    // =========================================================================
    // RESULT REPORT GENERATION TESTS
    // =========================================================================

    @Test
    fun `HashVerificationResult generateReport produces readable output`() {
        val content = "Test content"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val expectedHash = sealingEngine.computeHash(content)
        val result = verificationEngine.verifyFileHash(testFile, expectedHash)

        val report = result.generateReport()

        assertTrue(report.contains("HASH VERIFICATION RESULT"))
        assertTrue(report.contains("test.txt"))
        assertTrue(report.contains("MATCH"))
    }

    @Test
    fun `ComprehensiveVerificationResult generateReport produces full report`() {
        val content = "Test content"
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(content)

        val contentHash = sealingEngine.computeHash(content)
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val result = verificationEngine.performComprehensiveVerification(testFile, seal)
        val report = result.generateReport()

        assertTrue(report.contains("COMPREHENSIVE VERIFICATION REPORT"))
        assertTrue(report.contains("HASH VERIFICATION"))
        assertTrue(report.contains("SEAL VERIFICATION"))
        assertTrue(report.contains("TIMESTAMP VALIDATION"))
    }
}
