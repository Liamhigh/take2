package com.verumomnis.forensic.core

/**
 * Forensic Engine Interface
 * 
 * Constitutional compliance:
 * - Stateless: No persistent state between operations
 * - Offline: No network requirements
 * - Sealed: All output is cryptographically sealed
 */
interface ForensicEngine {

    /**
     * Process evidence and create a sealed package
     * 
     * @param evidence List of evidence items to process
     * @param metadata Optional metadata for the package
     * @return Sealed evidence package
     */
    suspend fun processEvidence(
        evidence: List<RawEvidence>,
        metadata: EvidenceMetadata = EvidenceMetadata()
    ): Result<EvidencePackage>

    /**
     * Verify the integrity of a sealed evidence package
     * 
     * @param package Evidence package to verify
     * @return Verification result with details
     */
    suspend fun verifyPackage(evidencePackage: EvidencePackage): VerificationResult

    /**
     * Add evidence to an existing package (creates new sealed version)
     * 
     * @param existingPackage The current package
     * @param newEvidence New evidence to add
     * @return New sealed package with updated chain of custody
     */
    suspend fun appendEvidence(
        existingPackage: EvidencePackage,
        newEvidence: List<RawEvidence>
    ): Result<EvidencePackage>

    /**
     * Export package as PDF (PDF 1.7 standard as per constitution)
     * 
     * @param package Evidence package to export
     * @param outputPath Local path for output
     * @return Path to generated PDF
     */
    suspend fun exportToPdf(
        evidencePackage: EvidencePackage,
        outputPath: String
    ): Result<String>

    /**
     * Generate QR code for package verification
     * 
     * @param package Evidence package
     * @return QR code data
     */
    suspend fun generateVerificationQR(evidencePackage: EvidencePackage): Result<ByteArray>
}

/**
 * Raw evidence input before processing
 */
data class RawEvidence(
    val filePath: String,
    val originalFilename: String,
    val mimeType: String,
    val captureTimestamp: java.time.Instant = java.time.Instant.now()
)

/**
 * Result of package verification
 */
data class VerificationResult(
    val isValid: Boolean,
    val sealIntact: Boolean,
    val contentIntact: Boolean,
    val chainOfCustodyValid: Boolean,
    val issues: List<VerificationIssue> = emptyList(),
    val verifiedAt: java.time.Instant = java.time.Instant.now()
)

/**
 * Issue found during verification
 */
data class VerificationIssue(
    val severity: IssueSeverity,
    val code: String,
    val message: String,
    val affectedItem: String? = null
)

enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}
