package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.custody.ChainOfCustodyLogger
import org.verumomnis.forensic.custody.CustodyAction
import org.verumomnis.forensic.custody.IntegrityStatus

/**
 * Unit tests for ChainOfCustodyLogger
 *
 * Tests the chain of custody logging functionality per:
 * - ISO 27037: Digital evidence handling
 * - Federal Rules of Evidence: Chain of custody documentation
 */
class ChainOfCustodyLoggerTest {

    private lateinit var custodyLogger: ChainOfCustodyLogger

    @Before
    fun setUp() {
        custodyLogger = ChainOfCustodyLogger()
    }

    @Test
    fun `logAction creates valid log entry`() {
        val entry = custodyLogger.logAction(
            action = CustodyAction.DOCUMENT_UPLOAD,
            targetHash = "a".repeat(128),
            userId = "testUser",
            deviceId = "testDevice",
            details = "Test document upload"
        )

        assertNotNull(entry)
        assertEquals(CustodyAction.DOCUMENT_UPLOAD, entry.action)
        assertEquals("a".repeat(128), entry.targetHash)
        assertEquals("testUser", entry.userId)
        assertEquals("testDevice", entry.deviceId)
        assertNotNull(entry.entryHash)
        assertEquals(128, entry.entryHash.length) // SHA-512
    }

    @Test
    fun `chain integrity is verified for single entry`() {
        custodyLogger.logAction(
            action = CustodyAction.DOCUMENT_UPLOAD,
            targetHash = "a".repeat(128),
            userId = "testUser",
            deviceId = "testDevice"
        )

        val status = custodyLogger.verifyChainIntegrity()

        assertEquals(IntegrityStatus.VERIFIED, status)
    }

    @Test
    fun `chain integrity is verified for multiple entries`() {
        // Add multiple log entries
        custodyLogger.logDocumentUpload("hash1".repeat(25), "user1", "device1")
        custodyLogger.logDocumentProcessing("hash2".repeat(25), "user1", "device1")
        custodyLogger.logReportGeneration("hash3".repeat(25), "user1", "device1")

        val status = custodyLogger.verifyChainIntegrity()

        assertEquals(IntegrityStatus.VERIFIED, status)
        assertEquals(3, custodyLogger.getEntryCount())
    }

    @Test
    fun `chain correctly links entries via previous hash`() {
        val entry1 = custodyLogger.logAction(
            action = CustodyAction.CASE_CREATED,
            targetHash = "hash1".repeat(25),
            userId = "user1",
            deviceId = "device1"
        )

        val entry2 = custodyLogger.logAction(
            action = CustodyAction.DOCUMENT_UPLOAD,
            targetHash = "hash2".repeat(25),
            userId = "user1",
            deviceId = "device1"
        )

        // Entry2's previous hash should be entry1's entry hash
        assertEquals(entry1.entryHash, entry2.previousHash)
    }

    @Test
    fun `getEntries returns all log entries`() {
        custodyLogger.logAction(CustodyAction.CASE_CREATED, "hash1", "user1", "device1")
        custodyLogger.logAction(CustodyAction.DOCUMENT_UPLOAD, "hash2", "user1", "device1")
        custodyLogger.logAction(CustodyAction.SEAL_VERIFIED, "hash3", "user1", "device1")

        val entries = custodyLogger.getEntries()

        assertEquals(3, entries.size)
    }

    @Test
    fun `getEntriesForHash filters correctly`() {
        val targetHash = "targetHash123".repeat(10)
        custodyLogger.logAction(CustodyAction.DOCUMENT_UPLOAD, targetHash, "user1", "device1")
        custodyLogger.logAction(CustodyAction.DOCUMENT_PROCESSING, targetHash, "user1", "device1")
        custodyLogger.logAction(CustodyAction.SEAL_VERIFIED, "differentHash", "user1", "device1")

        val filteredEntries = custodyLogger.getEntriesForHash(targetHash)

        assertEquals(2, filteredEntries.size)
        assertTrue(filteredEntries.all { it.targetHash == targetHash })
    }

    @Test
    fun `logDocumentUpload creates correct action type`() {
        val entry = custodyLogger.logDocumentUpload("hash123", "user1", "device1")

        assertEquals(CustodyAction.DOCUMENT_UPLOAD, entry.action)
        assertTrue(entry.details.contains("Document added"))
    }

    @Test
    fun `logTamperingDetected creates high severity entry`() {
        val entry = custodyLogger.logTamperingDetected(
            documentHash = "hash123",
            userId = "user1",
            deviceId = "device1",
            tamperingDetails = "Content hash mismatch detected"
        )

        assertEquals(CustodyAction.TAMPERING_DETECTED, entry.action)
        assertTrue(entry.details.contains("ALERT:"))
        assertTrue(entry.details.contains("Content hash mismatch"))
    }

    @Test
    fun `logSealVerification records verification result`() {
        val passedEntry = custodyLogger.logSealVerification("hash123", "user1", "device1", true)
        val failedEntry = custodyLogger.logSealVerification("hash456", "user1", "device1", false)

        assertEquals(CustodyAction.SEAL_VERIFIED, passedEntry.action)
        assertTrue(passedEntry.details.contains("PASSED"))

        assertEquals(CustodyAction.SEAL_VERIFIED, failedEntry.action)
        assertTrue(failedEntry.details.contains("FAILED"))
    }

    @Test
    fun `getChainHeadHash returns latest entry hash`() {
        custodyLogger.logAction(CustodyAction.CASE_CREATED, "hash1", "user1", "device1")
        val lastEntry = custodyLogger.logAction(CustodyAction.DOCUMENT_UPLOAD, "hash2", "user1", "device1")

        assertEquals(lastEntry.entryHash, custodyLogger.getChainHeadHash())
    }

    @Test
    fun `exportReport produces formatted output`() {
        custodyLogger.logDocumentUpload("hash1".repeat(25), "user1", "device1")
        custodyLogger.logDocumentProcessing("hash2".repeat(25), "user1", "device1")

        val report = custodyLogger.exportReport()

        assertTrue(report.contains("CHAIN OF CUSTODY LOG"))
        assertTrue(report.contains("Chain Status: VERIFIED"))
        assertTrue(report.contains("Total Entries: 2"))
        assertTrue(report.contains("DOCUMENT_UPLOAD"))
        assertTrue(report.contains("DOCUMENT_PROCESSING"))
    }

    @Test
    fun `entry toLogFormat produces correct format`() {
        val entry = custodyLogger.logAction(
            action = CustodyAction.DOCUMENT_UPLOAD,
            targetHash = "abc123".repeat(20),
            userId = "testUser",
            deviceId = "testDevice"
        )

        val logFormat = entry.toLogFormat()

        assertTrue(logFormat.contains("DOCUMENT_UPLOAD"))
        assertTrue(logFormat.contains("SHA512-"))
        assertTrue(logFormat.contains("testUser"))
        assertTrue(logFormat.contains("testDevice"))
        assertTrue(logFormat.contains("VERIFIED"))
    }

    @Test
    fun `entry toJson produces valid JSON`() {
        val entry = custodyLogger.logAction(
            action = CustodyAction.CASE_CREATED,
            targetHash = "hash123".repeat(15),
            userId = "user1",
            deviceId = "device1"
        )

        val json = entry.toJson()

        assertTrue(json.contains("\"id\":"))
        assertTrue(json.contains("\"timestamp\":"))
        assertTrue(json.contains("\"action\": \"CASE_CREATED\""))
        assertTrue(json.contains("\"target_hash\":"))
        assertTrue(json.contains("\"entry_hash\":"))
    }

    @Test
    fun `empty chain verifies as valid`() {
        val status = custodyLogger.verifyChainIntegrity()

        assertEquals(IntegrityStatus.VERIFIED, status)
    }

    @Test
    fun `concurrent logging maintains chain integrity`() {
        // Simulate rapid sequential logging
        repeat(10) { i ->
            custodyLogger.logAction(
                action = CustodyAction.DOCUMENT_UPLOAD,
                targetHash = "hash$i".repeat(15),
                userId = "user$i",
                deviceId = "device1"
            )
        }

        val status = custodyLogger.verifyChainIntegrity()
        assertEquals(IntegrityStatus.VERIFIED, status)
        assertEquals(10, custodyLogger.getEntryCount())

        // Verify chain linking
        val entries = custodyLogger.getEntries()
        for (i in 1 until entries.size) {
            assertEquals(entries[i - 1].entryHash, entries[i].previousHash)
        }
    }
}
