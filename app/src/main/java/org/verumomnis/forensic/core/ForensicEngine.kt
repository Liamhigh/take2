package org.verumomnis.forensic.core

import android.content.Context
import android.os.Build
import org.verumomnis.forensic.crypto.CryptographicSeal
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.location.ForensicLocationService
import org.verumomnis.forensic.pdf.ForensicPdfGenerator
import org.verumomnis.forensic.report.*
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.*

/**
 * Verum Omnis Forensic Engine
 * 
 * The main processing engine that coordinates all forensic operations.
 * This is a stateless, offline-first engine that:
 * 
 * 1. Captures documents (scan or import)
 * 2. Records location and time
 * 3. Generates cryptographic seals
 * 4. Creates AI-readable narratives
 * 5. Produces legally admissible PDF reports
 * 
 * Privacy & Security:
 * - All processing occurs locally
 * - No data transmitted externally
 * - No persistent state stored
 * - Airgap-ready operation
 */
class ForensicEngine(private val context: Context) {
    
    private val locationService = ForensicLocationService(context)
    private val narrativeGenerator = ForensicNarrativeGenerator()
    private val pdfGenerator = ForensicPdfGenerator(context)
    
    /**
     * Process a document and generate a sealed forensic report
     * 
     * @param documentData Raw document bytes
     * @param documentType Type of document
     * @param originalFilename Original filename if known
     * @param extractedText Text extracted from document
     * @param documentMetadata Metadata from document
     * @param ocrConfidence OCR confidence (0.0-1.0)
     * @param caseContext User-provided case context
     * @param findings Detected findings from document analysis
     * @return ForensicReport containing all generated materials
     */
    suspend fun processDocument(
        documentData: ByteArray,
        documentType: DocumentType,
        originalFilename: String?,
        extractedText: String,
        documentMetadata: Map<String, String>,
        ocrConfidence: Float,
        caseContext: CaseContext,
        findings: List<ForensicFinding> = emptyList()
    ): ForensicReport {
        // Generate unique identifiers
        val sessionId = CryptographicSealingEngine.generateSessionId()
        val evidenceId = CryptographicSealingEngine.generateEvidenceId()
        
        // Capture timestamp
        val captureTime = Instant.now()
        val timezone = ZoneId.systemDefault()
        
        // Get location (with fallback for permission issues)
        val location = locationService.getLocationWithFallback()
            ?: createDefaultLocation()
        
        // Generate document hash
        val documentHash = CryptographicSealingEngine.generateHash(documentData)
        
        // Get device info
        val deviceInfo = getDeviceInfo()
        
        // Create evidence object
        val evidence = ForensicEvidence(
            evidenceId = evidenceId,
            captureTimestampUtc = captureTime,
            captureTimezone = timezone,
            location = location,
            documentData = documentData,
            documentType = documentType,
            originalFilename = originalFilename,
            originalHash = documentHash,
            extractedText = extractedText,
            documentMetadata = documentMetadata,
            ocrConfidence = ocrConfidence,
            deviceInfo = deviceInfo,
            sessionId = sessionId
        )
        
        // Generate location hash for seal
        val locationHash = CryptographicSealingEngine.generateHash(
            "${location.latitude},${location.longitude},${location.timezoneId}"
        )
        
        // Create cryptographic seal
        val seal = CryptographicSealingEngine.createSeal(
            documentData = documentData,
            timestamp = captureTime,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        // Generate narrative
        val narrative = narrativeGenerator.generateNarrative(
            evidence = evidence,
            seal = seal,
            findings = findings,
            caseContext = caseContext
        )
        
        // Generate PDF
        val pdfBytes = pdfGenerator.generatePdf(
            evidence = evidence,
            seal = seal,
            narrative = narrative,
            includeSourceCode = true
        )
        
        // Create hash of final PDF
        val pdfHash = CryptographicSealingEngine.generateHash(pdfBytes)
        
        return ForensicReport(
            evidence = evidence,
            seal = seal,
            narrative = narrative,
            pdfBytes = pdfBytes,
            pdfHash = pdfHash
        )
    }
    
    /**
     * Verify a previously generated seal
     */
    fun verifySeal(seal: CryptographicSeal, documentData: ByteArray) =
        CryptographicSealingEngine.verifySeal(seal, documentData)
    
    /**
     * Save report to device storage
     */
    fun saveReport(report: ForensicReport, outputDir: File): SaveResult {
        return try {
            // Create output directory if needed
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            // Generate filename
            val timestamp = report.evidence.captureTimestampUtc.epochSecond
            val filename = "VerumOmnis_${report.evidence.evidenceId}_$timestamp.pdf"
            val outputFile = File(outputDir, filename)
            
            // Save PDF
            pdfGenerator.savePdfToFile(report.pdfBytes, outputFile)
            
            SaveResult(
                success = true,
                filePath = outputFile.absolutePath,
                filename = filename,
                error = null
            )
        } catch (e: Exception) {
            SaveResult(
                success = false,
                filePath = null,
                filename = null,
                error = e.message
            )
        }
    }
    
    /**
     * Quick scan mode - minimal processing for fast capture
     */
    suspend fun quickCapture(
        documentData: ByteArray,
        description: String
    ): ForensicReport {
        return processDocument(
            documentData = documentData,
            documentType = DocumentType.SCANNED_DOCUMENT,
            originalFilename = null,
            extractedText = "",
            documentMetadata = mapOf("quick_capture" to "true"),
            ocrConfidence = 0f,
            caseContext = CaseContext(
                caseType = "Quick Capture",
                userDescription = description,
                urgency = Urgency.MEDIUM
            )
        )
    }
    
    /**
     * Import and process an existing PDF
     */
    suspend fun importPdf(
        pdfBytes: ByteArray,
        filename: String,
        extractedText: String,
        metadata: Map<String, String>,
        caseContext: CaseContext,
        findings: List<ForensicFinding> = emptyList()
    ): ForensicReport {
        return processDocument(
            documentData = pdfBytes,
            documentType = DocumentType.PDF,
            originalFilename = filename,
            extractedText = extractedText,
            documentMetadata = metadata,
            ocrConfidence = 1.0f, // PDFs have exact text
            caseContext = caseContext,
            findings = findings
        )
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            deviceId = generateAnonymizedDeviceId()
        )
    }
    
    private fun generateAnonymizedDeviceId(): String {
        // Generate an anonymized device ID that doesn't expose actual device info
        val combined = "${Build.MANUFACTURER}|${Build.MODEL}|${Build.FINGERPRINT}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(combined.toByteArray())
        return hash.take(8).joinToString("") { "%02x".format(it) }.uppercase()
    }
    
    private fun createDefaultLocation(): ForensicLocation {
        // Return a placeholder location when GPS is unavailable
        return ForensicLocation(
            latitude = 0.0,
            longitude = 0.0,
            accuracyMeters = -1f,
            altitudeMeters = null,
            country = "Unknown",
            countryCode = "XX",
            administrativeArea = "Unknown",
            locality = "Unknown",
            fullAddress = "Location unavailable",
            timezoneId = TimeZone.getDefault().id
        )
    }
}

/**
 * Complete forensic report package
 */
data class ForensicReport(
    val evidence: ForensicEvidence,
    val seal: CryptographicSeal,
    val narrative: ForensicNarrative,
    val pdfBytes: ByteArray,
    val pdfHash: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ForensicReport
        return evidence.evidenceId == other.evidence.evidenceId
    }
    
    override fun hashCode(): Int {
        return evidence.evidenceId.hashCode()
    }
}

/**
 * Result of save operation
 */
data class SaveResult(
    val success: Boolean,
    val filePath: String?,
    val filename: String?,
    val error: String?
)
