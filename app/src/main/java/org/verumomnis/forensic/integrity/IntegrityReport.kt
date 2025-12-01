package org.verumomnis.forensic.integrity

/**
 * Data class representing the result of an APK integrity verification.
 * 
 * This is used to establish the root of trust for all forensic operations.
 * When matches is true, the app is verified as untampered and can proceed
 * with forensic evidence processing. When false, forensic integrity is compromised.
 */
data class IntegrityReport(
    /** The expected SHA-256 hash of the untampered APK */
    val expectedHash: String,
    
    /** The actual SHA-256 hash computed from the installed APK */
    val actualHash: String,
    
    /** True if the actual hash matches the expected hash (case-insensitive) */
    val matches: Boolean,
    
    /** Unix timestamp in milliseconds when verification was performed */
    val verificationTime: Long,
    
    /** Path to the APK file that was verified, or null if verification failed */
    val apkPath: String?,
    
    /** Error message if verification failed, null if successful */
    val errorMessage: String?
) {
    /**
     * Returns the verification status as a human-readable string.
     */
    fun getStatusText(): String = when {
        errorMessage != null -> "ERROR: $errorMessage"
        matches -> "✅ VALID - APK Integrity Verified"
        else -> "❌ TAMPERED - Forensic Engine Compromised"
    }
    
    /**
     * Returns whether the APK is verified and safe for forensic operations.
     */
    fun isVerified(): Boolean = matches && errorMessage == null
    
    /**
     * Converts the report to a map for JSON serialization or logging.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "expected_hash" to expectedHash,
        "actual_hash" to actualHash,
        "matches" to matches,
        "verification_time" to verificationTime,
        "apk_path" to apkPath,
        "error_message" to errorMessage,
        "status" to getStatusText()
    )
}
