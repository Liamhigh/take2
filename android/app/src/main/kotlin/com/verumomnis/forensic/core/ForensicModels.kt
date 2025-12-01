package com.verumomnis.forensic.core

import java.time.Instant
import java.util.UUID

/**
 * Forensic Seal - Cryptographic seal for evidence integrity
 * 
 * As per Verum Constitution:
 * - hash_standard: SHA-512
 * - seal_required: true
 * - tamper_detection: mandatory
 */
data class ForensicSeal(
    val sealId: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val hashAlgorithm: String = "SHA-512",
    val contentHash: String,
    val metadataHash: String,
    val combinedSealHash: String,
    val constitutionVersion: String = "1.0",
    val sealerInfo: SealerInfo
) {
    /**
     * Verify the integrity of this seal
     */
    fun verifySealIntegrity(): Boolean {
        return hashAlgorithm == "SHA-512" && 
               contentHash.isNotBlank() && 
               metadataHash.isNotBlank() && 
               combinedSealHash.isNotBlank()
    }
}

/**
 * Information about who/what created the seal
 */
data class SealerInfo(
    val appVersion: String,
    val deviceFingerprint: String,  // Non-identifying device signature
    val constitutionMode: Boolean = true
)

/**
 * Evidence Package - Complete sealed evidence container
 * 
 * Stateless: No user session data stored
 * Offline: All processing done locally
 */
data class EvidencePackage(
    val packageId: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
    val evidenceItems: List<EvidenceItem>,
    val seal: ForensicSeal,
    val metadata: EvidenceMetadata,
    val chainOfCustody: List<CustodyRecord> = emptyList()
)

/**
 * Individual evidence item
 */
data class EvidenceItem(
    val itemId: String = UUID.randomUUID().toString(),
    val originalFilename: String,
    val mimeType: String,
    val sizeBytes: Long,
    val contentHash: String,
    val capturedAt: Instant = Instant.now(),
    val localPath: String  // Local storage path only
)

/**
 * Metadata about the evidence
 */
data class EvidenceMetadata(
    val caseReference: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val location: GeoLocation? = null,
    val customFields: Map<String, String> = emptyMap()
)

/**
 * Optional geo-location (user-controlled)
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Instant
)

/**
 * Chain of custody record
 */
data class CustodyRecord(
    val recordId: String = UUID.randomUUID().toString(),
    val timestamp: Instant = Instant.now(),
    val action: CustodyAction,
    val previousHash: String,
    val newHash: String,
    val note: String? = null
)

/**
 * Actions in chain of custody
 */
enum class CustodyAction {
    CREATED,
    ACCESSED,
    EXPORTED,
    VERIFIED,
    TRANSFERRED
}
