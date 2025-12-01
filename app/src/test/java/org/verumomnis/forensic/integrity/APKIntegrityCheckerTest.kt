package org.verumomnis.forensic.integrity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for APKIntegrityChecker
 * 
 * Note: Many APKIntegrityChecker methods require Android context and
 * are better tested as instrumented tests. These unit tests cover
 * the static methods and utility functions.
 */
class APKIntegrityCheckerTest {
    
    @Test
    fun `EXPECTED_APK_HASH is valid SHA-256 format`() {
        val hash = APKIntegrityChecker.EXPECTED_APK_HASH
        
        // SHA-256 produces 64 hex characters
        assertEquals("SHA-256 hash should be 64 characters", 64, hash.length)
        
        // All characters should be valid hex
        val isValidHex = hash.all { 
            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' 
        }
        assertTrue("Hash should only contain hex characters", isValidHex)
    }
    
    @Test
    fun `embedIntegritySeal contains expected APK hash`() {
        val seal = APKIntegrityChecker.embedIntegritySeal()
        
        assertTrue(seal.contains(APKIntegrityChecker.EXPECTED_APK_HASH))
        assertTrue(seal.contains("VERUM OMNIS APK INTEGRITY SEAL"))
        assertTrue(seal.contains("Build Hash"))
    }
    
    @Test
    fun `getIndependentVerificationInstructions contains verification steps`() {
        val instructions = APKIntegrityChecker.getIndependentVerificationInstructions()
        
        assertTrue(instructions.contains("adb pull"))
        assertTrue(instructions.contains("sha256sum"))
        assertTrue(instructions.contains(APKIntegrityChecker.EXPECTED_APK_HASH))
    }
    
    @Test
    fun `generateVerificationReport produces valid report for matching hashes`() {
        val report = IntegrityReport(
            expectedHash = APKIntegrityChecker.EXPECTED_APK_HASH,
            actualHash = APKIntegrityChecker.EXPECTED_APK_HASH,
            matches = true,
            verificationTime = System.currentTimeMillis(),
            apkPath = "/data/app/test/base.apk",
            errorMessage = null
        )
        
        val verificationReport = APKIntegrityChecker.generateVerificationReport(report)
        
        assertTrue(verificationReport.contains("FORENSIC VERIFICATION REPORT"))
        assertTrue(verificationReport.contains("PASS - Untampered"))
        assertTrue(verificationReport.contains(APKIntegrityChecker.EXPECTED_APK_HASH))
        assertTrue(verificationReport.contains("chain of custody"))
    }
    
    @Test
    fun `generateVerificationReport produces valid report for mismatched hashes`() {
        val report = IntegrityReport(
            expectedHash = APKIntegrityChecker.EXPECTED_APK_HASH,
            actualHash = "0000000000000000000000000000000000000000000000000000000000000000",
            matches = false,
            verificationTime = System.currentTimeMillis(),
            apkPath = "/data/app/test/base.apk",
            errorMessage = null
        )
        
        val verificationReport = APKIntegrityChecker.generateVerificationReport(report)
        
        assertTrue(verificationReport.contains("FAIL - Modified"))
    }
    
    @Test
    fun `APK hash matches expected value from problem statement`() {
        // This is the hash specified in the issue
        val expectedHash = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
        
        assertEquals(
            "APK hash should match the value from the problem statement",
            expectedHash,
            APKIntegrityChecker.EXPECTED_APK_HASH
        )
    }
}
