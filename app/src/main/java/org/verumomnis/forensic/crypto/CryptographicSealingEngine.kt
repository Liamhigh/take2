package org.verumomnis.forensic.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic Sealing Engine
 * 
 * Provides cryptographic integrity verification for forensic evidence.
 * All operations are performed locally - no network access required.
 * 
 * Standards Compliance:
 * - Hash Algorithm: SHA-512 (as per verum-constitution.json)
 * - HMAC: HMAC-SHA512 for document sealing
 * - Tamper Detection: Mandatory (per constitution)
 * 
 * This sealing mechanism ensures:
 * 1. Document integrity verification
 * 2. Tamper detection capability
 * 3. Chain of custody authentication
 * 4. Legal admissibility of evidence
 */
object CryptographicSealingEngine {
    
    private const val HASH_ALGORITHM = "SHA-512"
    private const val HMAC_ALGORITHM = "HmacSHA512"
    
    /**
     * Generate SHA-512 hash of data
     * 
     * @param data The data to hash
     * @return Hexadecimal string representation of the hash
     */
    fun generateHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate SHA-512 hash of string data
     */
    fun generateHash(data: String): String {
        return generateHash(data.toByteArray(Charsets.UTF_8))
    }
    
    /**
     * Create a cryptographic seal for a document
     * 
     * The seal contains:
     * - Document hash
     * - Timestamp of sealing
     * - Location hash
     * - Session identifier
     * - Seal signature (HMAC)
     * 
     * @param documentData The document bytes
     * @param timestamp When the document was captured
     * @param locationHash Hash of location data
     * @param sessionId Unique session identifier
     * @return CryptographicSeal object
     */
    fun createSeal(
        documentData: ByteArray,
        timestamp: Instant,
        locationHash: String,
        sessionId: String
    ): CryptographicSeal {
        // Generate document hash
        val documentHash = generateHash(documentData)
        
        // Create seal data string
        val sealData = buildSealData(documentHash, timestamp, locationHash, sessionId)
        
        // Generate seal key from combined inputs (deterministic for verification)
        val sealKey = generateSealKey(sessionId, timestamp)
        
        // Generate HMAC signature
        val signature = generateHmac(sealData, sealKey)
        
        // Create verification string for QR code
        val verificationCode = generateVerificationCode(documentHash, signature)
        
        return CryptographicSeal(
            documentHash = documentHash,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId,
            signature = signature,
            verificationCode = verificationCode,
            algorithm = HASH_ALGORITHM,
            sealVersion = "1.0"
        )
    }
    
    /**
     * Verify a cryptographic seal
     * 
     * @param seal The seal to verify
     * @param documentData The original document data
     * @return SealVerificationResult indicating if seal is valid
     */
    fun verifySeal(seal: CryptographicSeal, documentData: ByteArray): SealVerificationResult {
        // Recompute document hash
        val computedHash = generateHash(documentData)
        
        // Check if document hash matches
        if (computedHash != seal.documentHash) {
            return SealVerificationResult(
                isValid = false,
                failureReason = "Document hash mismatch - document may have been tampered with",
                computedHash = computedHash,
                expectedHash = seal.documentHash
            )
        }
        
        // Recreate seal data for signature verification
        val sealData = buildSealData(seal.documentHash, seal.timestamp, seal.locationHash, seal.sessionId)
        val sealKey = generateSealKey(seal.sessionId, seal.timestamp)
        val computedSignature = generateHmac(sealData, sealKey)
        
        // Verify signature
        if (computedSignature != seal.signature) {
            return SealVerificationResult(
                isValid = false,
                failureReason = "Signature mismatch - seal may have been tampered with",
                computedHash = computedHash,
                expectedHash = seal.documentHash
            )
        }
        
        return SealVerificationResult(
            isValid = true,
            failureReason = null,
            computedHash = computedHash,
            expectedHash = seal.documentHash
        )
    }
    
    /**
     * Generate a unique session identifier
     */
    fun generateSessionId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return "VO-" + bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }
    
    /**
     * Generate a unique evidence ID
     */
    fun generateEvidenceId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(8)
        random.nextBytes(bytes)
        val timestamp = System.currentTimeMillis()
        return "EV-${timestamp}-" + bytes.joinToString("") { "%02x".format(it) }.uppercase()
    }
    
    private fun buildSealData(
        documentHash: String,
        timestamp: Instant,
        locationHash: String,
        sessionId: String
    ): String {
        return "VERUM_OMNIS_SEAL|$documentHash|$timestamp|$locationHash|$sessionId"
    }
    
    private fun generateSealKey(sessionId: String, timestamp: Instant): ByteArray {
        val keyMaterial = "VERUM_OMNIS_KEY|$sessionId|$timestamp"
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        return digest.digest(keyMaterial.toByteArray(Charsets.UTF_8))
    }
    
    private fun generateHmac(data: String, key: ByteArray): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hmacBytes)
    }
    
    private fun generateVerificationCode(documentHash: String, signature: String): String {
        val combined = "$documentHash|$signature"
        val hash = generateHash(combined)
        // Return first 32 characters for QR code friendly verification
        return hash.substring(0, 32).uppercase()
    }
}

/**
 * Cryptographic seal containing all verification data
 */
data class CryptographicSeal(
    val documentHash: String,
    val timestamp: Instant,
    val locationHash: String,
    val sessionId: String,
    val signature: String,
    val verificationCode: String,
    val algorithm: String,
    val sealVersion: String
) {
    /**
     * Get a human-readable seal summary
     */
    fun getSealSummary(): String {
        return """
            |VERUM OMNIS CRYPTOGRAPHIC SEAL
            |==============================
            |Seal Version: $sealVersion
            |Algorithm: $algorithm
            |Session ID: $sessionId
            |Timestamp: $timestamp
            |Document Hash: ${documentHash.take(16)}...${documentHash.takeLast(16)}
            |Verification Code: $verificationCode
            |
            |This document has been cryptographically sealed by the
            |Verum Omnis Constitutional Governance Layer.
            |Any modification to this document will invalidate the seal.
        """.trimMargin()
    }
    
    /**
     * Get QR code content for verification
     */
    fun getQrCodeContent(): String {
        return "VERUM:$verificationCode:$sessionId:${timestamp.epochSecond}"
    }
}

/**
 * Result of seal verification
 */
data class SealVerificationResult(
    val isValid: Boolean,
    val failureReason: String?,
    val computedHash: String,
    val expectedHash: String
) {
    fun getVerificationMessage(): String {
        return if (isValid) {
            "✓ SEAL VERIFIED: Document integrity confirmed. Hash matches original."
        } else {
            "✗ SEAL INVALID: $failureReason"
        }
    }
}
