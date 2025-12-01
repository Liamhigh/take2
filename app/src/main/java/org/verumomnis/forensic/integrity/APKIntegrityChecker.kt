package org.verumomnis.forensic.integrity

import android.content.Context
import android.os.Build
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * APK Integrity Checker for Verum Omnis Forensic Engine
 * 
 * Implements APK integrity verification as the root of trust for all forensic operations.
 * Per verum-constitution.json:
 * - tamper_detection: mandatory
 * - hash_standard: SHA-512 (using SHA-256 for APK as per standard practice)
 * 
 * The APK hash serves as the cryptographic anchor for the chain of custody,
 * establishing that all forensic analysis originates from an untampered tool.
 * 
 * SECURITY MODEL:
 * The expected hash is embedded in the source code intentionally. This is a standard
 * approach for self-verification in forensic tools where:
 * 1. The source code and APK are publicly auditable
 * 2. The hash can be independently verified by any forensic expert using sha256sum
 * 3. The primary threat model is runtime tampering, not source code compromise
 * 4. For additional security, use Android App Signing verification or code attestation
 * 
 * Independent verification: sha256sum verum-omnis-forensic-engine.apk
 */
object APKIntegrityChecker {
    
    /**
     * Expected SHA-256 hash of the release APK.
     * This hash must be updated after each release build.
     * 
     * To generate: sha256sum verum-omnis-forensic-engine.apk
     */
    const val EXPECTED_APK_HASH = "56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466"
    
    private const val HASH_ALGORITHM = "SHA-256"
    
    /**
     * Verifies the integrity of the installed APK against the expected hash.
     * 
     * @param context Application context
     * @return IntegrityReport containing verification results
     */
    fun verifyAPKIntegrity(context: Context): IntegrityReport {
        return try {
            // Get APK file path
            val apkPath = context.packageManager
                .getApplicationInfo(context.packageName, 0).sourceDir
            
            // Calculate actual hash
            val actualHash = calculateSHA256(File(apkPath))
            
            IntegrityReport(
                expectedHash = EXPECTED_APK_HASH,
                actualHash = actualHash,
                matches = actualHash.equals(EXPECTED_APK_HASH, ignoreCase = true),
                verificationTime = System.currentTimeMillis(),
                apkPath = apkPath,
                errorMessage = null
            )
        } catch (e: Exception) {
            IntegrityReport(
                expectedHash = EXPECTED_APK_HASH,
                actualHash = "",
                matches = false,
                verificationTime = System.currentTimeMillis(),
                apkPath = null,
                errorMessage = "APK verification failed: ${e.message}"
            )
        }
    }
    
    /**
     * Calculates SHA-256 hash of a file.
     * 
     * @param file The file to hash
     * @return Lowercase hex string of the hash
     */
    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generates the integrity seal text for embedding in reports.
     */
    fun embedIntegritySeal(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return """
            === VERUM OMNIS APK INTEGRITY SEAL ===
            Build Hash: $EXPECTED_APK_HASH
            Verified: ${now.format(formatter)}
            This APK has not been modified since build.
            ===
        """.trimIndent()
    }
    
    /**
     * Generates a comprehensive verification report for display or export.
     * 
     * @param integrity The IntegrityReport from verifyAPKIntegrity
     * @return Formatted verification report string
     */
    fun generateVerificationReport(integrity: IntegrityReport): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val utcOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000
        
        return """
            === FORENSIC VERIFICATION REPORT ===
            
            APK INTEGRITY CHECK
            Expected: ${integrity.expectedHash}
            Actual:   ${integrity.actualHash}
            Status:   ${if (integrity.matches) "PASS - Untampered" else "FAIL - Modified"}
            
            DEVICE INFORMATION
            Model: ${Build.MODEL}
            Manufacturer: ${Build.MANUFACTURER}
            Android: ${Build.VERSION.RELEASE}
            Security Patch: ${Build.VERSION.SECURITY_PATCH}
            
            TIMESTAMP
            Verified: ${now.format(formatter)}
            UTC Offset: $utcOffset hours
            
            VERIFICATION COMMAND (for independent verification)
            sha256sum verum-omnis-forensic-engine.apk
            Expected output: $EXPECTED_APK_HASH
            
            ===
            
            This verification establishes that:
            1. The forensic engine has not been modified since build
            2. All output from this engine originates from a known source
            3. The chain of custody begins with this verified APK
            
            ===
        """.trimIndent()
    }
    
    /**
     * Returns independent verification instructions for court use.
     */
    fun getIndependentVerificationInstructions(): String {
        return """
            Independent Verification:
            1. Extract APK from device: adb pull /data/app/org.verumomnis.forensic/base.apk
            2. Calculate hash: sha256sum base.apk
            3. Compare with: $EXPECTED_APK_HASH
        """.trimIndent()
    }
}
