package org.verumomnis.forensic.integrity

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for IntegrityReport data class
 */
class IntegrityReportTest {
    
    private lateinit var validReport: IntegrityReport
    private lateinit var invalidReport: IntegrityReport
    private lateinit var errorReport: IntegrityReport
    
    @Before
    fun setUp() {
        val expectedHash = APKIntegrityChecker.EXPECTED_APK_HASH
        
        validReport = IntegrityReport(
            expectedHash = expectedHash,
            actualHash = expectedHash,
            matches = true,
            verificationTime = System.currentTimeMillis(),
            apkPath = "/data/app/test/base.apk",
            errorMessage = null
        )
        
        invalidReport = IntegrityReport(
            expectedHash = expectedHash,
            actualHash = "0000000000000000000000000000000000000000000000000000000000000000",
            matches = false,
            verificationTime = System.currentTimeMillis(),
            apkPath = "/data/app/test/base.apk",
            errorMessage = null
        )
        
        errorReport = IntegrityReport(
            expectedHash = expectedHash,
            actualHash = "",
            matches = false,
            verificationTime = System.currentTimeMillis(),
            apkPath = null,
            errorMessage = "APK verification failed: File not found"
        )
    }
    
    @Test
    fun `valid report returns correct status text`() {
        val status = validReport.getStatusText()
        assertTrue(status.contains("VALID"))
        assertTrue(status.contains("✅"))
    }
    
    @Test
    fun `invalid report returns tampered status text`() {
        val status = invalidReport.getStatusText()
        assertTrue(status.contains("TAMPERED"))
        assertTrue(status.contains("❌"))
    }
    
    @Test
    fun `error report returns error status text`() {
        val status = errorReport.getStatusText()
        assertTrue(status.contains("ERROR"))
        assertTrue(status.contains("File not found"))
    }
    
    @Test
    fun `isVerified returns true only when matches and no error`() {
        assertTrue(validReport.isVerified())
        assertFalse(invalidReport.isVerified())
        assertFalse(errorReport.isVerified())
    }
    
    @Test
    fun `toMap produces correct structure`() {
        val map = validReport.toMap()
        
        assertTrue(map.containsKey("expected_hash"))
        assertTrue(map.containsKey("actual_hash"))
        assertTrue(map.containsKey("matches"))
        assertTrue(map.containsKey("verification_time"))
        assertTrue(map.containsKey("status"))
        
        assertEquals(true, map["matches"])
    }
    
    @Test
    fun `expected hash uses correct format`() {
        // SHA-256 hash should be 64 hex characters
        assertEquals(64, APKIntegrityChecker.EXPECTED_APK_HASH.length)
        
        // Verify it's valid hex
        val isValidHex = APKIntegrityChecker.EXPECTED_APK_HASH.all { 
            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' 
        }
        assertTrue("Expected hash should be valid hex", isValidHex)
    }
}
