package org.verumomnis.forensic.core

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.location.ForensicLocationService
import org.verumomnis.forensic.pdf.ForensicPdfGenerator
import org.verumomnis.forensic.report.ForensicNarrativeGenerator
import java.io.File
import java.time.Instant
import java.util.UUID

/**
 * Core Forensic Engine for Verum Omnis
 *
 * Implements the forensic rules from verum-constitution.json:
 * - seal_required: true
 * - hash_standard: SHA-512
 * - pdf_standard: PDF 1.7
 * - tamper_detection: mandatory
 * - admissibility_standard: legal-grade, contradiction-free, complete evidence mapping
 *
 * Security principles:
 * - offline_first: true
 * - stateless: true
 * - no_cloud_logging: true
 * - no_telemetry: true
 * - airgap_ready: true
 */
class ForensicEngine(private val context: Context) {

    companion object {
        private const val TAG = "ForensicEngine"
        const val HASH_STANDARD = "SHA-512"
    }

    private val sealingEngine = CryptographicSealingEngine()
    private val locationService = ForensicLocationService(context)
    private val narrativeGenerator = ForensicNarrativeGenerator()
    private val pdfGenerator = ForensicPdfGenerator(context)

    /**
     * Creates a new forensic case folder for evidence collection
     */
    suspend fun createNewCase(caseName: String): ForensicCase = withContext(Dispatchers.IO) {
        val caseId = UUID.randomUUID().toString()
        val timestamp = Instant.now()

        val caseDir = File(context.filesDir, "cases/$caseId")
        caseDir.mkdirs()

        ForensicCase(
            id = caseId,
            name = caseName,
            createdAt = timestamp,
            directory = caseDir,
            evidenceItems = mutableListOf()
        )
    }

    /**
     * Adds evidence to a case with cryptographic sealing
     */
    suspend fun addEvidence(
        case: ForensicCase,
        evidenceType: EvidenceType,
        description: String,
        data: ByteArray,
        metadata: Map<String, String> = emptyMap()
    ): ForensicEvidence = withContext(Dispatchers.IO) {
        val evidenceId = UUID.randomUUID().toString()
        val timestamp = Instant.now()

        // Get current location if available
        val location = try {
            locationService.getCurrentLocation()
        } catch (_: Exception) {
            null
        }

        // Create cryptographic seal
        val contentHash = sealingEngine.computeHash(data)
        val seal = sealingEngine.createSeal(
            contentHash = contentHash,
            timestamp = timestamp,
            location = location,
            metadata = metadata
        )

        // Save evidence file
        val evidenceFile = File(case.directory, "$evidenceId.dat")
        evidenceFile.writeBytes(data)

        // Save seal file
        val sealFile = File(case.directory, "$evidenceId.seal")
        sealFile.writeText(seal.toJson())

        val evidence = ForensicEvidence(
            id = evidenceId,
            type = evidenceType,
            description = description,
            timestamp = timestamp,
            contentHash = contentHash,
            seal = seal,
            location = location,
            metadata = metadata,
            file = evidenceFile
        )

        case.evidenceItems.add(evidence)
        evidence
    }

    /**
     * Verifies the integrity of evidence
     */
    suspend fun verifyEvidence(evidence: ForensicEvidence): VerificationResult =
        withContext(Dispatchers.IO) {
            val data = evidence.file.readBytes()
            val currentHash = sealingEngine.computeHash(data)

            if (currentHash != evidence.contentHash) {
                return@withContext VerificationResult(
                    isValid = false,
                    message = "Evidence has been tampered with - hash mismatch",
                    originalHash = evidence.contentHash,
                    currentHash = currentHash
                )
            }

            val sealValid = sealingEngine.verifySeal(evidence.seal, currentHash)

            VerificationResult(
                isValid = sealValid,
                message = if (sealValid) "Evidence integrity verified" else "Seal verification failed",
                originalHash = evidence.contentHash,
                currentHash = currentHash
            )
        }

    /**
     * Generates a forensic PDF report for a case
     */
    suspend fun generateReport(case: ForensicCase): File = withContext(Dispatchers.IO) {
        // Generate AI-readable narrative
        val narrative = narrativeGenerator.generateNarrative(case)

        // Generate PDF with seal, watermark, and QR code
        pdfGenerator.generateReport(case, narrative)
    }

    /**
     * Gets device forensic metadata
     */
    fun getDeviceMetadata(): Map<String, String> = mapOf(
        "device_manufacturer" to Build.MANUFACTURER,
        "device_model" to Build.MODEL,
        "android_version" to Build.VERSION.RELEASE,
        "sdk_version" to Build.VERSION.SDK_INT.toString(),
        "engine_version" to VerumOmnisApplication.VERSION,
        "hash_standard" to HASH_STANDARD
    )
}

/**
 * Represents a forensic case container
 */
data class ForensicCase(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val directory: File,
    val evidenceItems: MutableList<ForensicEvidence>
)

/**
 * Types of forensic evidence
 */
enum class EvidenceType {
    DOCUMENT,
    PHOTO,
    VIDEO,
    AUDIO,
    SCREENSHOT,
    TEXT,
    LOCATION,
    METADATA
}

/**
 * Result of evidence verification
 */
data class VerificationResult(
    val isValid: Boolean,
    val message: String,
    val originalHash: String,
    val currentHash: String
)
