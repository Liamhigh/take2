package org.verumomnis.forensic.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Forensic Evidence Container
 * 
 * A self-contained, immutable record of scanned evidence with full
 * forensic metadata including location, time, and cryptographic sealing.
 * 
 * This structure is designed to be:
 * - Completely self-documenting for AI analysis
 * - Legally admissible with full chain of custody
 * - Jurisdiction-aware for legal advice purposes
 */
data class ForensicEvidence(
    // Unique identifier for this evidence record
    val evidenceId: String,
    
    // Timestamp when evidence was captured (UTC)
    val captureTimestampUtc: Instant,
    
    // Local timezone at capture location
    val captureTimezone: ZoneId,
    
    // GPS coordinates at time of capture
    val location: ForensicLocation,
    
    // The scanned document data
    val documentData: ByteArray,
    
    // Document type (PDF, IMAGE, etc.)
    val documentType: DocumentType,
    
    // Original filename if applicable
    val originalFilename: String?,
    
    // SHA-512 hash of original document
    val originalHash: String,
    
    // Extracted text content from document
    val extractedText: String,
    
    // Metadata extracted from document
    val documentMetadata: Map<String, String>,
    
    // OCR confidence score (0.0 - 1.0)
    val ocrConfidence: Float,
    
    // Device information for chain of custody
    val deviceInfo: DeviceInfo,
    
    // Capture session identifier
    val sessionId: String
) {
    /**
     * Get human-readable capture time in local timezone
     */
    fun getLocalCaptureTime(): String {
        return captureTimestampUtc
            .atZone(captureTimezone)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
    }
    
    /**
     * Get ISO-8601 formatted timestamp for legal documents
     */
    fun getIsoTimestamp(): String {
        return DateTimeFormatter.ISO_INSTANT.format(captureTimestampUtc)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ForensicEvidence
        return evidenceId == other.evidenceId
    }
    
    override fun hashCode(): Int {
        return evidenceId.hashCode()
    }
}

/**
 * Forensic Location Data
 * 
 * Complete location information for jurisdiction determination
 */
data class ForensicLocation(
    // Latitude in decimal degrees
    val latitude: Double,
    
    // Longitude in decimal degrees
    val longitude: Double,
    
    // Accuracy in meters
    val accuracyMeters: Float,
    
    // Altitude in meters above sea level (if available)
    val altitudeMeters: Double?,
    
    // Country name (determined by reverse geocoding)
    val country: String?,
    
    // Country ISO code (e.g., "ZA", "US", "GB")
    val countryCode: String?,
    
    // State/Province
    val administrativeArea: String?,
    
    // City/Town
    val locality: String?,
    
    // Full address string
    val fullAddress: String?,
    
    // Timezone ID for this location
    val timezoneId: String
) {
    /**
     * Get jurisdiction string for legal purposes
     */
    fun getJurisdictionString(): String {
        val parts = mutableListOf<String>()
        locality?.let { parts.add(it) }
        administrativeArea?.let { parts.add(it) }
        country?.let { parts.add(it) }
        if (parts.isEmpty()) {
            return "Unknown Jurisdiction (${latitude}, ${longitude})"
        }
        return parts.joinToString(", ")
    }
    
    /**
     * Get coordinates in standard format
     */
    fun getCoordinatesString(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format("%.6f°%s, %.6f°%s", 
            kotlin.math.abs(latitude), latDir,
            kotlin.math.abs(longitude), lonDir)
    }
}

/**
 * Device information for chain of custody
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val deviceId: String  // Anonymized device identifier
)

/**
 * Supported document types
 */
enum class DocumentType {
    PDF,
    IMAGE_JPEG,
    IMAGE_PNG,
    IMAGE_TIFF,
    SCANNED_DOCUMENT,
    UNKNOWN
}
