package com.verumomnis.forensic.crypto

import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic Sealing Service
 * 
 * Constitutional Requirements:
 * - hash_standard: SHA-512
 * - seal_required: true
 * - tamper_detection: mandatory
 * 
 * All operations are:
 * - Stateless: No keys are stored between operations
 * - Offline: No network requirements
 * - Local: All processing on device
 */
object CryptoSealService {

    private const val HASH_ALGORITHM = "SHA-512"
    private const val HMAC_ALGORITHM = "HmacSHA512"
    private const val BUFFER_SIZE = 8192

    /**
     * Compute SHA-512 hash of a file
     * 
     * @param file The file to hash
     * @return Hex-encoded SHA-512 hash
     */
    fun hashFile(file: File): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        file.inputStream().buffered(BUFFER_SIZE).use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().toHexString()
    }

    /**
     * Compute SHA-512 hash of an input stream
     * 
     * @param inputStream The stream to hash
     * @return Hex-encoded SHA-512 hash
     */
    fun hashStream(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        inputStream.buffered(BUFFER_SIZE).use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().toHexString()
    }

    /**
     * Compute SHA-512 hash of a byte array
     * 
     * @param data The data to hash
     * @return Hex-encoded SHA-512 hash
     */
    fun hashBytes(data: ByteArray): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        return digest.digest(data).toHexString()
    }

    /**
     * Compute SHA-512 hash of a string
     * 
     * @param text The text to hash
     * @return Hex-encoded SHA-512 hash
     */
    fun hashString(text: String): String {
        return hashBytes(text.toByteArray(Charsets.UTF_8))
    }

    /**
     * Create a combined seal hash from content and metadata hashes
     * 
     * @param contentHash The hash of the evidence content
     * @param metadataHash The hash of the evidence metadata
     * @param timestamp ISO-8601 timestamp string
     * @return Combined seal hash
     */
    fun createSealHash(
        contentHash: String,
        metadataHash: String,
        timestamp: String
    ): String {
        val combinedData = "$contentHash|$metadataHash|$timestamp|$CONSTITUTION_MARKER"
        return hashString(combinedData)
    }

    /**
     * Create HMAC-SHA512 signature
     * 
     * @param data The data to sign
     * @param key The signing key
     * @return Hex-encoded HMAC signature
     */
    fun createHmacSignature(data: ByteArray, key: ByteArray): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        return mac.doFinal(data).toHexString()
    }

    /**
     * Verify HMAC-SHA512 signature
     * 
     * @param data The data to verify
     * @param key The verification key
     * @param signature The expected signature
     * @return True if signature matches
     */
    fun verifyHmacSignature(data: ByteArray, key: ByteArray, signature: String): Boolean {
        val computed = createHmacSignature(data, key)
        return computed.equals(signature, ignoreCase = true)
    }

    /**
     * Generate cryptographically secure random bytes
     * 
     * @param length Number of bytes to generate
     * @return Random byte array
     */
    fun generateSecureRandom(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }

    /**
     * Verify that two hashes match (constant-time comparison)
     * 
     * @param hash1 First hash
     * @param hash2 Second hash
     * @return True if hashes match
     */
    fun verifyHashMatch(hash1: String, hash2: String): Boolean {
        if (hash1.length != hash2.length) return false
        
        var result = 0
        for (i in hash1.indices) {
            result = result or (hash1[i].code xor hash2[i].code)
        }
        return result == 0
    }

    /**
     * Convert byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private const val CONSTITUTION_MARKER = "VERUM_OMNIS_CONSTITUTION_v1.0"
}
