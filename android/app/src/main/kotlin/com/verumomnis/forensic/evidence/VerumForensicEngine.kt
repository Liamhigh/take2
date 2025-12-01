package com.verumomnis.forensic.evidence

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.verumomnis.forensic.core.*
import com.verumomnis.forensic.crypto.CryptoSealService
import com.verumomnis.forensic.crypto.DeviceFingerprint
import com.verumomnis.forensic.crypto.LocalTimestampAuthority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

/**
 * Verum Omnis Forensic Engine Implementation
 * 
 * Constitutional Compliance:
 * - Stateless: No persistent state between operations
 * - Offline: No network requirements
 * - Sealed: All output is cryptographically sealed with SHA-512
 * - Local: All processing happens on-device
 * - No telemetry: Zero data transmission
 */
class VerumForensicEngine(
    private val context: Context
) : ForensicEngine {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    private val appVersion: String
        get() = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }

    override suspend fun processEvidence(
        evidence: List<RawEvidence>,
        metadata: EvidenceMetadata
    ): Result<EvidencePackage> = withContext(Dispatchers.IO) {
        try {
            // Process each evidence item
            val evidenceItems = evidence.map { raw ->
                processRawEvidence(raw)
            }

            // Create content hash (hash of all evidence hashes)
            val contentHash = createContentHash(evidenceItems)

            // Create metadata hash
            val metadataHash = createMetadataHash(metadata, evidenceItems)

            // Create timestamp proof
            val timestampProof = LocalTimestampAuthority.createTimestamp()

            // Create combined seal hash
            val sealHash = CryptoSealService.createSealHash(
                contentHash,
                metadataHash,
                timestampProof.timestamp.toString()
            )

            // Create sealer info
            val sealerInfo = SealerInfo(
                appVersion = appVersion,
                deviceFingerprint = DeviceFingerprint.generate(context),
                constitutionMode = true
            )

            // Create forensic seal
            val seal = ForensicSeal(
                timestamp = timestampProof.timestamp,
                hashAlgorithm = "SHA-512",
                contentHash = contentHash,
                metadataHash = metadataHash,
                combinedSealHash = sealHash,
                sealerInfo = sealerInfo
            )

            // Create initial chain of custody record
            val custodyRecord = CustodyRecord(
                action = CustodyAction.CREATED,
                previousHash = "GENESIS",
                newHash = sealHash,
                note = "Evidence package created"
            )

            // Create final package
            val evidencePackage = EvidencePackage(
                evidenceItems = evidenceItems,
                seal = seal,
                metadata = metadata,
                chainOfCustody = listOf(custodyRecord)
            )

            Result.success(evidencePackage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPackage(evidencePackage: EvidencePackage): VerificationResult = 
        withContext(Dispatchers.IO) {
            val issues = mutableListOf<VerificationIssue>()

            // Verify seal integrity
            val sealIntact = evidencePackage.seal.verifySealIntegrity()
            if (!sealIntact) {
                issues.add(
                    VerificationIssue(
                        severity = IssueSeverity.CRITICAL,
                        code = "SEAL_INVALID",
                        message = "Forensic seal is not properly formed"
                    )
                )
            }

            // Verify content hash matches
            val recalculatedContentHash = createContentHash(evidencePackage.evidenceItems)
            val contentIntact = CryptoSealService.verifyHashMatch(
                recalculatedContentHash,
                evidencePackage.seal.contentHash
            )
            if (!contentIntact) {
                issues.add(
                    VerificationIssue(
                        severity = IssueSeverity.CRITICAL,
                        code = "CONTENT_TAMPERED",
                        message = "Evidence content has been modified since sealing"
                    )
                )
            }

            // Verify each evidence file still exists and matches
            for (item in evidencePackage.evidenceItems) {
                val file = File(item.localPath)
                if (file.exists()) {
                    val currentHash = CryptoSealService.hashFile(file)
                    if (!CryptoSealService.verifyHashMatch(currentHash, item.contentHash)) {
                        issues.add(
                            VerificationIssue(
                                severity = IssueSeverity.CRITICAL,
                                code = "FILE_TAMPERED",
                                message = "File content has changed",
                                affectedItem = item.itemId
                            )
                        )
                    }
                } else {
                    issues.add(
                        VerificationIssue(
                            severity = IssueSeverity.WARNING,
                            code = "FILE_MISSING",
                            message = "Evidence file not found at expected location",
                            affectedItem = item.itemId
                        )
                    )
                }
            }

            // Verify chain of custody
            val chainValid = verifyChainOfCustody(evidencePackage.chainOfCustody)
            if (!chainValid) {
                issues.add(
                    VerificationIssue(
                        severity = IssueSeverity.ERROR,
                        code = "CHAIN_BROKEN",
                        message = "Chain of custody verification failed"
                    )
                )
            }

            // Verify hash algorithm is SHA-512
            if (evidencePackage.seal.hashAlgorithm != "SHA-512") {
                issues.add(
                    VerificationIssue(
                        severity = IssueSeverity.ERROR,
                        code = "WRONG_ALGORITHM",
                        message = "Hash algorithm must be SHA-512 per constitution"
                    )
                )
            }

            VerificationResult(
                isValid = issues.none { it.severity == IssueSeverity.CRITICAL },
                sealIntact = sealIntact,
                contentIntact = contentIntact,
                chainOfCustodyValid = chainValid,
                issues = issues
            )
        }

    override suspend fun appendEvidence(
        existingPackage: EvidencePackage,
        newEvidence: List<RawEvidence>
    ): Result<EvidencePackage> = withContext(Dispatchers.IO) {
        try {
            // Process new evidence
            val newItems = newEvidence.map { processRawEvidence(it) }
            
            // Combine with existing
            val allItems = existingPackage.evidenceItems + newItems

            // Create new hashes
            val contentHash = createContentHash(allItems)
            val metadataHash = createMetadataHash(existingPackage.metadata, allItems)
            val timestampProof = LocalTimestampAuthority.createTimestamp()
            val sealHash = CryptoSealService.createSealHash(
                contentHash,
                metadataHash,
                timestampProof.timestamp.toString()
            )

            // Create new seal
            val newSeal = ForensicSeal(
                timestamp = timestampProof.timestamp,
                hashAlgorithm = "SHA-512",
                contentHash = contentHash,
                metadataHash = metadataHash,
                combinedSealHash = sealHash,
                sealerInfo = SealerInfo(
                    appVersion = appVersion,
                    deviceFingerprint = DeviceFingerprint.generate(context),
                    constitutionMode = true
                )
            )

            // Add to chain of custody
            val newCustodyRecord = CustodyRecord(
                action = CustodyAction.CREATED,
                previousHash = existingPackage.seal.combinedSealHash,
                newHash = sealHash,
                note = "Evidence appended: ${newItems.size} new items"
            )

            val updatedChain = existingPackage.chainOfCustody + newCustodyRecord

            Result.success(
                EvidencePackage(
                    evidenceItems = allItems,
                    seal = newSeal,
                    metadata = existingPackage.metadata,
                    chainOfCustody = updatedChain
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportToPdf(
        evidencePackage: EvidencePackage,
        outputPath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // PDF generation would use iTextPDF library
            // For now, return success with path
            // Full implementation would include:
            // - PDF 1.7 format as per constitution
            // - 3D watermark: "VERUM OMNIS 3D LOGO CENTERED"
            // - QR code for verification
            // - Full evidence listing with hashes
            // - Chain of custody
            // - Seal information
            
            Result.success(outputPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateVerificationQR(evidencePackage: EvidencePackage): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                // QR code would contain:
                // - Package ID
                // - Combined seal hash
                // - Timestamp
                // - Verification URL (optional, for online verification)
                
                val qrData = buildString {
                    append("VERUM_OMNIS_SEAL|")
                    append(evidencePackage.packageId)
                    append("|")
                    append(evidencePackage.seal.combinedSealHash)
                    append("|")
                    append(evidencePackage.seal.timestamp)
                }
                
                Result.success(qrData.toByteArray(Charsets.UTF_8))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Private helper methods

    private fun processRawEvidence(raw: RawEvidence): EvidenceItem {
        val file = File(raw.filePath)
        return EvidenceItem(
            originalFilename = raw.originalFilename,
            mimeType = raw.mimeType,
            sizeBytes = file.length(),
            contentHash = CryptoSealService.hashFile(file),
            capturedAt = raw.captureTimestamp,
            localPath = raw.filePath
        )
    }

    private fun createContentHash(items: List<EvidenceItem>): String {
        val combinedHashes = items
            .sortedBy { it.itemId }
            .joinToString("|") { it.contentHash }
        return CryptoSealService.hashString(combinedHashes)
    }

    private fun createMetadataHash(metadata: EvidenceMetadata, items: List<EvidenceItem>): String {
        val metadataJson = gson.toJson(metadata)
        val itemIds = items.sortedBy { it.itemId }.joinToString(",") { it.itemId }
        return CryptoSealService.hashString("$metadataJson|$itemIds")
    }

    private fun verifyChainOfCustody(chain: List<CustodyRecord>): Boolean {
        if (chain.isEmpty()) return false
        
        // First record must reference GENESIS
        if (chain.first().previousHash != "GENESIS") return false
        
        // Each subsequent record must reference previous hash
        for (i in 1 until chain.size) {
            if (chain[i].previousHash != chain[i - 1].newHash) {
                return false
            }
        }
        
        return true
    }
}
