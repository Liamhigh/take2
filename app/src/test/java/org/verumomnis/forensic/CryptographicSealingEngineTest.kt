package org.verumomnis.forensic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.crypto.DeviceInfo
import org.verumomnis.forensic.location.ForensicLocation
import java.time.Instant

/**
 * Unit tests for CryptographicSealingEngine
 *
 * Tests cryptographic sealing and verification per verum-constitution.json:
 * - hash_standard: SHA-512
 * - seal_required: true
 * - tamper_detection: mandatory
 *
 * Additional tests for forensic-grade Triple Hash Layer:
 * - Layer 1: SHA-512 of content
 * - Layer 2: SHA-512 of metadata
 * - Layer 3: HMAC-SHA512 combining both
 */
class CryptographicSealingEngineTest {

    private lateinit var sealingEngine: CryptographicSealingEngine

    @Before
    fun setUp() {
        sealingEngine = CryptographicSealingEngine()
    }

    @Test
    fun `computeHash returns consistent SHA-512 hash`() {
        val data = "Test forensic evidence data"

        val hash1 = sealingEngine.computeHash(data)
        val hash2 = sealingEngine.computeHash(data)

        assertEquals(hash1, hash2)
        // SHA-512 produces 128 character hex string (512 bits = 64 bytes = 128 hex chars)
        assertEquals(128, hash1.length)
    }

    @Test
    fun `computeHash returns different hashes for different data`() {
        val data1 = "Evidence A"
        val data2 = "Evidence B"

        val hash1 = sealingEngine.computeHash(data1)
        val hash2 = sealingEngine.computeHash(data2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `computeHash handles empty data`() {
        val hash = sealingEngine.computeHash("")

        assertNotNull(hash)
        assertEquals(128, hash.length)
    }

    @Test
    fun `computeHash handles binary data`() {
        val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())

        val hash = sealingEngine.computeHash(binaryData)

        assertNotNull(hash)
        assertEquals(128, hash.length)
    }

    @Test
    fun `createSeal generates valid seal without location`() {
        val contentHash = sealingEngine.computeHash("Test content")
        val timestamp = Instant.now()

        val seal = sealingEngine.createSeal(
            contentHash = contentHash,
            timestamp = timestamp,
            location = null,
            metadata = emptyMap()
        )

        assertNotNull(seal)
        assertEquals("1.0", seal.version)
        assertEquals("HmacSHA512", seal.algorithm)
        assertEquals(contentHash, seal.contentHash)
        assertEquals(timestamp, seal.timestamp)
        assertNull(seal.location)
        assertNotNull(seal.salt)
        assertNotNull(seal.signature)
    }

    @Test
    fun `createSeal generates valid seal with location`() {
        val contentHash = sealingEngine.computeHash("Test content with location")
        val timestamp = Instant.now()
        val location = ForensicLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            altitude = 10.0,
            accuracy = 5.0f,
            bearing = null,
            speed = null,
            timestamp = timestamp,
            provider = "gps"
        )

        val seal = sealingEngine.createSeal(
            contentHash = contentHash,
            timestamp = timestamp,
            location = location,
            metadata = mapOf("source" to "test")
        )

        assertNotNull(seal)
        assertNotNull(seal.location)
        assertEquals(40.7128, seal.location!!.latitude, 0.0001)
        assertEquals(-74.0060, seal.location!!.longitude, 0.0001)
    }

    @Test
    fun `verifySeal returns true for valid seal`() {
        val content = "Original forensic evidence"
        val contentHash = sealingEngine.computeHash(content)
        val timestamp = Instant.now()

        val seal = sealingEngine.createSeal(
            contentHash = contentHash,
            timestamp = timestamp,
            location = null,
            metadata = emptyMap()
        )

        val isValid = sealingEngine.verifySeal(seal, contentHash)

        assertTrue(isValid)
    }

    @Test
    fun `verifySeal returns false for tampered content`() {
        val originalContent = "Original forensic evidence"
        val tamperedContent = "Tampered forensic evidence"
        val originalHash = sealingEngine.computeHash(originalContent)
        val tamperedHash = sealingEngine.computeHash(tamperedContent)
        val timestamp = Instant.now()

        val seal = sealingEngine.createSeal(
            contentHash = originalHash,
            timestamp = timestamp,
            location = null,
            metadata = emptyMap()
        )

        val isValid = sealingEngine.verifySeal(seal, tamperedHash)

        assertFalse("Tampered content should fail verification", isValid)
    }

    @Test
    fun `different seals have unique salts`() {
        val contentHash = sealingEngine.computeHash("Same content")
        val timestamp = Instant.now()

        val seal1 = sealingEngine.createSeal(contentHash, timestamp, null, emptyMap())
        val seal2 = sealingEngine.createSeal(contentHash, timestamp, null, emptyMap())

        assertNotEquals(seal1.salt, seal2.salt)
        assertNotEquals(seal1.signature, seal2.signature)
    }

    @Test
    fun `seal toJson produces valid JSON structure`() {
        val contentHash = sealingEngine.computeHash("Test content")
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val json = seal.toJson()

        assertTrue(json.contains("\"version\":"))
        assertTrue(json.contains("\"algorithm\":"))
        assertTrue(json.contains("\"content_hash\":"))
        assertTrue(json.contains("\"timestamp\":"))
        assertTrue(json.contains("\"signature\":"))
    }

    @Test
    fun `seal toMap produces valid map structure`() {
        val contentHash = sealingEngine.computeHash("Test content")
        val seal = sealingEngine.createSeal(contentHash, Instant.now(), null, emptyMap())

        val map = seal.toMap()

        assertTrue(map.containsKey("version"))
        assertTrue(map.containsKey("algorithm"))
        assertTrue(map.containsKey("content_hash"))
        assertTrue(map.containsKey("signature"))
    }

    // =========================================================================
    // TRIPLE HASH LAYER TESTS
    // =========================================================================

    @Test
    fun `createTripleHashSeal creates valid triple hash seal`() {
        val content = "Test forensic evidence content".toByteArray()
        val metadata = mapOf("source" to "test", "type" to "document")
        val deviceInfo = DeviceInfo(
            manufacturer = "TestManufacturer",
            model = "TestModel",
            androidVersion = "13",
            sdkVersion = 33
        )

        val seal = sealingEngine.createTripleHashSeal(
            content = content,
            metadata = metadata,
            deviceInfo = deviceInfo,
            caseName = "Test Case"
        )

        assertNotNull(seal)
        assertEquals(128, seal.contentHash.length)  // SHA-512 = 128 hex chars
        assertEquals(128, seal.metadataHash.length)
        assertNotNull(seal.hmacSeal)
        assertEquals("Test Case", seal.caseName)
        assertEquals(CryptographicSealingEngine.VERSION, seal.version)
    }

    @Test
    fun `createTripleHashSeal produces deterministic content hash`() {
        val content = "Deterministic test content".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)

        val seal1 = sealingEngine.createTripleHashSeal(content, emptyMap(), deviceInfo, "Case1")
        val seal2 = sealingEngine.createTripleHashSeal(content, emptyMap(), deviceInfo, "Case2")

        // Content hash should be the same for same content
        assertEquals(seal1.contentHash, seal2.contentHash)

        // But HMAC seals should differ due to different salts
        assertNotEquals(seal1.hmacSeal, seal2.hmacSeal)
    }

    @Test
    fun `verifyTripleHashSeal detects valid content`() {
        val content = "Test forensic evidence".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)

        val seal = sealingEngine.createTripleHashSeal(
            content = content,
            metadata = emptyMap(),
            deviceInfo = deviceInfo,
            caseName = "Test Case"
        )

        val result = sealingEngine.verifyTripleHashSeal(seal, content)

        assertTrue(result.isValid)
        assertTrue(result.contentIntact)
        assertTrue(result.metadataIntact)
        assertTrue(result.sealIntact)
        assertTrue(result.message.contains("PASSED"))
    }

    @Test
    fun `verifyTripleHashSeal detects tampered content`() {
        val originalContent = "Original evidence".toByteArray()
        val tamperedContent = "Tampered evidence".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)

        val seal = sealingEngine.createTripleHashSeal(
            content = originalContent,
            metadata = emptyMap(),
            deviceInfo = deviceInfo,
            caseName = "Test Case"
        )

        val result = sealingEngine.verifyTripleHashSeal(seal, tamperedContent)

        assertFalse(result.isValid)
        assertFalse(result.contentIntact)
        assertTrue(result.message.contains("TAMPERING DETECTED"))
    }

    @Test
    fun `generateForensicFooter includes required fields`() {
        val content = "Test content".toByteArray()
        val deviceInfo = DeviceInfo("Samsung", "Galaxy S21", "13", 33)

        val seal = sealingEngine.createTripleHashSeal(
            content = content,
            metadata = emptyMap(),
            deviceInfo = deviceInfo,
            caseName = "Court Case 123"
        )

        val footer = sealingEngine.generateForensicFooter(seal)

        assertTrue(footer.contains("Court Case 123"))
        assertTrue(footer.contains("SHA512-"))
        assertTrue(footer.contains("Samsung"))
        assertTrue(footer.contains("Galaxy S21"))
        assertTrue(footer.contains("VERUM OMNIS"))
        assertTrue(footer.contains(CryptographicSealingEngine.VERSION))
    }

    @Test
    fun `TamperDetectionResult generateReport produces readable output`() {
        val content = "Test content".toByteArray()
        val deviceInfo = DeviceInfo("Test", "Test", "13", 33)

        val seal = sealingEngine.createTripleHashSeal(content, emptyMap(), deviceInfo, "Case")
        val result = sealingEngine.verifyTripleHashSeal(seal, content)

        val report = result.generateReport()

        assertTrue(report.contains("TAMPERING DETECTION REPORT"))
        assertTrue(report.contains("Layer 1"))
        assertTrue(report.contains("Layer 2"))
        assertTrue(report.contains("Layer 3"))
        assertTrue(report.contains("RESULT:"))
    }

    @Test
    fun `ISO timestamp formatter produces correct format`() {
        val timestamp = Instant.parse("2025-01-15T10:30:00Z")
        val formatted = CryptographicSealingEngine.ISO_TIMESTAMP_FORMATTER.format(timestamp)

        // Should be in ISO 8601 format with timezone
        assertTrue(formatted.contains("2025-01-15"))
        assertTrue(formatted.contains("T"))
    }
}
