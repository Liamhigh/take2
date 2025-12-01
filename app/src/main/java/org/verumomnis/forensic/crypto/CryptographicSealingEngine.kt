package org.verumomnis.forensic.crypto

import org.verumomnis.forensic.location.ForensicLocation
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic Sealing Engine for Verum Omnis
 *
 * Implements the forensic rules from verum-constitution.json:
 * - hash_standard: SHA-512
 * - seal_required: true
 * - tamper_detection: mandatory
 *
 * Uses SHA-512 hashing with HMAC for cryptographic sealing of evidence.
 */
class CryptographicSealingEngine {

    companion object {
        private const val HASH_ALGORITHM = "SHA-512"
        private const val HMAC_ALGORITHM = "HmacSHA512"
        private const val SALT_LENGTH = 32
    }

    private val secureRandom = SecureRandom()

    /**
     * Computes SHA-512 hash of data
     */
    fun computeHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(data)
        return hashBytes.toHexString()
    }

    /**
     * Computes SHA-512 hash of string content
     */
    fun computeHash(content: String): String {
        return computeHash(content.toByteArray(Charsets.UTF_8))
    }

    /**
     * Creates a cryptographic seal for evidence
     */
    fun createSeal(
        contentHash: String,
        timestamp: Instant,
        location: ForensicLocation?,
        metadata: Map<String, String> = emptyMap()
    ): CryptographicSeal {
        // Generate random salt
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        val saltHex = salt.toHexString()

        // Create seal payload
        val sealPayload = buildSealPayload(contentHash, timestamp, location, metadata, saltHex)

        // Generate HMAC signature
        val hmacKey = deriveKey(contentHash, saltHex)
        val signature = computeHmac(sealPayload, hmacKey)

        return CryptographicSeal(
            version = "1.0",
            algorithm = HMAC_ALGORITHM,
            contentHash = contentHash,
            timestamp = timestamp,
            location = location,
            metadata = metadata,
            salt = saltHex,
            signature = signature
        )
    }

    /**
     * Verifies the integrity of a cryptographic seal
     */
    fun verifySeal(seal: CryptographicSeal, currentContentHash: String): Boolean {
        // Verify content hash matches
        if (seal.contentHash != currentContentHash) {
            return false
        }

        // Rebuild payload and verify signature
        val sealPayload = buildSealPayload(
            seal.contentHash,
            seal.timestamp,
            seal.location,
            seal.metadata,
            seal.salt
        )

        val hmacKey = deriveKey(seal.contentHash, seal.salt)
        val expectedSignature = computeHmac(sealPayload, hmacKey)

        return seal.signature == expectedSignature
    }

    /**
     * Builds the seal payload string for hashing
     */
    private fun buildSealPayload(
        contentHash: String,
        timestamp: Instant,
        location: ForensicLocation?,
        metadata: Map<String, String>,
        salt: String
    ): String = buildString {
        append("v1|")
        append("$contentHash|")
        append("${timestamp.epochSecond}|")
        if (location != null) {
            append("${location.latitude},${location.longitude}|")
        } else {
            append("null|")
        }
        append(metadata.entries.sortedBy { it.key }
            .joinToString(",") { "${it.key}=${it.value}" })
        append("|$salt")
    }

    /**
     * Derives HMAC key from content hash and salt
     */
    private fun deriveKey(contentHash: String, salt: String): ByteArray {
        val keyMaterial = "$contentHash:$salt:verum-omnis-forensic"
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        return digest.digest(keyMaterial.toByteArray(Charsets.UTF_8))
    }

    /**
     * Computes HMAC-SHA512 signature
     */
    private fun computeHmac(data: String, key: ByteArray): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hmacBytes)
    }

    /**
     * Extension function to convert ByteArray to hex string
     */
    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }
}

/**
 * Represents a cryptographic seal for forensic evidence
 */
data class CryptographicSeal(
    val version: String,
    val algorithm: String,
    val contentHash: String,
    val timestamp: Instant,
    val location: ForensicLocation?,
    val metadata: Map<String, String>,
    val salt: String,
    val signature: String
) {
    /**
     * Converts seal to JSON string for storage
     */
    fun toJson(): String = buildString {
        appendLine("{")
        appendLine("  \"version\": \"$version\",")
        appendLine("  \"algorithm\": \"$algorithm\",")
        appendLine("  \"content_hash\": \"$contentHash\",")
        appendLine("  \"timestamp\": \"$timestamp\",")
        if (location != null) {
            appendLine("  \"location\": {")
            appendLine("    \"latitude\": ${location.latitude},")
            appendLine("    \"longitude\": ${location.longitude},")
            appendLine("    \"accuracy\": ${location.accuracy}")
            appendLine("  },")
        } else {
            appendLine("  \"location\": null,")
        }
        appendLine("  \"salt\": \"$salt\",")
        appendLine("  \"signature\": \"$signature\"")
        appendLine("}")
    }

    /**
     * Converts seal to map for reporting
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "version" to version,
        "algorithm" to algorithm,
        "content_hash" to contentHash,
        "timestamp" to timestamp.toString(),
        "location" to location?.toMap(),
        "salt" to salt,
        "signature" to signature
    )
}
