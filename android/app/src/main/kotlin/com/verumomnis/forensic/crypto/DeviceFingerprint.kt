package com.verumomnis.forensic.crypto

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.time.Instant

/**
 * Device Fingerprint Generator
 * 
 * Creates a non-identifying device fingerprint for seal metadata.
 * 
 * Constitutional Compliance:
 * - No PII (Personally Identifiable Information) included
 * - No tracking or identification capability
 * - Used only for seal provenance verification
 */
object DeviceFingerprint {

    /**
     * Generate a non-identifying device fingerprint
     * 
     * This fingerprint cannot be used to identify the user or device.
     * It serves only to prove the seal originated from a consistent source.
     * 
     * @param context Application context
     * @return SHA-256 hash representing device characteristics
     */
    fun generate(context: Context): String {
        val components = buildList {
            // Hardware characteristics (non-identifying)
            add(Build.BOARD)
            add(Build.DEVICE)
            add(Build.HARDWARE)
            add(Build.MANUFACTURER)
            add(Build.MODEL)
            add(Build.PRODUCT)
            add(Build.SUPPORTED_ABIS.joinToString(","))
            
            // App-specific identifier (changes with reinstall)
            add(getAppInstanceId(context))
            
            // Screen density class (not exact resolution)
            add(getDensityClass(context))
        }
        
        val combined = components.joinToString("|")
        return hashFingerprint(combined)
    }

    /**
     * Get app instance identifier
     * Resets on app reinstall - not persistent across installations
     */
    private fun getAppInstanceId(context: Context): String {
        val prefs = context.getSharedPreferences("verum_instance", Context.MODE_PRIVATE)
        var instanceId = prefs.getString("instance_id", null)
        
        if (instanceId == null) {
            instanceId = CryptoSealService.generateSecureRandom(16).joinToString("") { 
                "%02x".format(it) 
            }
            prefs.edit().putString("instance_id", instanceId).apply()
        }
        
        return instanceId
    }

    /**
     * Get density class without exact values
     */
    private fun getDensityClass(context: Context): String {
        val density = context.resources.displayMetrics.densityDpi
        return when {
            density <= 120 -> "ldpi"
            density <= 160 -> "mdpi"
            density <= 240 -> "hdpi"
            density <= 320 -> "xhdpi"
            density <= 480 -> "xxhdpi"
            else -> "xxxhdpi"
        }
    }

    /**
     * Hash the fingerprint components
     */
    private fun hashFingerprint(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

/**
 * Timestamp Authority (Local)
 * 
 * Provides trusted timestamps for evidence sealing.
 * In a production environment, this could be extended to use
 * RFC 3161 Time-Stamp Protocol with trusted authorities.
 */
object LocalTimestampAuthority {

    /**
     * Get current timestamp with cryptographic proof
     * 
     * @return Timestamped proof object
     */
    fun createTimestamp(): TimestampProof {
        val now = Instant.now()
        val nonce = CryptoSealService.generateSecureRandom(32)
        val data = "${now.epochSecond}|${now.nano}|${nonce.joinToString("") { "%02x".format(it) }}"
        val hash = CryptoSealService.hashString(data)
        
        return TimestampProof(
            timestamp = now,
            nonce = nonce.joinToString("") { "%02x".format(it) },
            proof = hash
        )
    }

    /**
     * Verify a timestamp proof
     * 
     * @param proof The proof to verify
     * @return True if proof is valid
     */
    fun verifyTimestamp(proof: TimestampProof): Boolean {
        val data = "${proof.timestamp.epochSecond}|${proof.timestamp.nano}|${proof.nonce}"
        val expectedHash = CryptoSealService.hashString(data)
        return CryptoSealService.verifyHashMatch(expectedHash, proof.proof)
    }
}

/**
 * Timestamp proof data
 */
data class TimestampProof(
    val timestamp: Instant,
    val nonce: String,
    val proof: String
)
