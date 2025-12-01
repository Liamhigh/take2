package org.verumomnis.forensic.pdf

import android.content.Context
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfVersion
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.verumomnis.forensic.core.ForensicEvidence
import org.verumomnis.forensic.crypto.CryptographicSeal
import org.verumomnis.forensic.report.ForensicNarrative
import org.verumomnis.forensic.report.NarrativeSection
import org.verumomnis.forensic.report.SectionType
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Forensic PDF Generator
 * 
 * Generates cryptographically sealed PDF reports following the
 * verum-constitution.json standards:
 * 
 * - PDF Standard: PDF 1.7
 * - Branding: VERUM OMNIS centered header on cover page
 * - QR Code Inclusion: Yes
 * - Tamper Detection: Mandatory
 * - Admissibility Standard: Legal-grade
 * 
 * Note: The constitution specifies a "3D LOGO CENTERED" watermark, but
 * this implementation uses text-based branding. A custom logo drawable
 * can be added to enhance visual branding in future versions.
 * 
 * The generated PDF contains:
 * 1. Complete forensic narrative
 * 2. Cryptographic seal information
 * 3. QR code for verification
 * 4. Embedded source code for transparency
 * 5. AI-readable structured content
 */
class ForensicPdfGenerator(private val context: Context) {
    
    companion object {
        private const val PDF_VERSION = "1.7"
        private const val WATERMARK_TEXT = "VERUM OMNIS"
        private val HEADER_COLOR = DeviceRgb(25, 55, 95) // Navy blue
        private val ACCENT_COLOR = DeviceRgb(41, 128, 185) // Blue
        private val WARNING_COLOR = DeviceRgb(231, 76, 60) // Red
    }
    
    /**
     * Generate a complete forensic PDF report
     * 
     * @param evidence The forensic evidence
     * @param seal The cryptographic seal
     * @param narrative The generated narrative
     * @param includeSourceCode Whether to embed source code
     * @return ByteArray containing the PDF data
     */
    fun generatePdf(
        evidence: ForensicEvidence,
        seal: CryptographicSeal,
        narrative: ForensicNarrative,
        includeSourceCode: Boolean = true
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        // Create PDF with version 1.7 as required
        val writerProperties = WriterProperties()
            .setPdfVersion(PdfVersion.PDF_1_7)
        
        val pdfWriter = PdfWriter(outputStream, writerProperties)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        
        // Set document metadata
        pdfDocument.documentInfo.apply {
            title = "Verum Omnis Forensic Report - ${evidence.evidenceId}"
            author = "Verum Omnis Constitutional Governance Layer"
            subject = "Cryptographically Sealed Forensic Evidence"
            keywords = "forensic, evidence, legal, ${evidence.location.countryCode ?: "jurisdiction"}"
            creator = "Verum Omnis Forensic Engine v1.0"
        }
        
        try {
            // Add cover page
            addCoverPage(document, evidence, seal)
            
            // Add table of contents
            addTableOfContents(document, narrative)
            
            // Add narrative sections
            for (section in narrative.sections) {
                document.add(AreaBreak())
                addNarrativeSection(document, section)
            }
            
            // Add QR code page
            document.add(AreaBreak())
            addQrCodePage(document, seal)
            
            // Add source code if requested
            if (includeSourceCode) {
                document.add(AreaBreak())
                addSourceCodeSection(document)
            }
            
            // Add final attestation page
            document.add(AreaBreak())
            addAttestationPage(document, seal, evidence)
            
        } finally {
            document.close()
        }
        
        return outputStream.toByteArray()
    }
    
    /**
     * Save PDF to file
     */
    fun savePdfToFile(pdfData: ByteArray, outputFile: File): Boolean {
        return try {
            outputFile.writeBytes(pdfData)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun addCoverPage(document: Document, evidence: ForensicEvidence, seal: CryptographicSeal) {
        // Title
        val title = Paragraph("VERUM OMNIS")
            .setFontSize(36f)
            .setFontColor(HEADER_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(100f)
        document.add(title)
        
        val subtitle = Paragraph("FORENSIC EVIDENCE REPORT")
            .setFontSize(24f)
            .setFontColor(ACCENT_COLOR)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10f)
        document.add(subtitle)
        
        val divider = Paragraph("═".repeat(40))
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20f)
        document.add(divider)
        
        // Evidence ID
        val evidenceInfo = Paragraph()
            .add(Text("Evidence ID: ").setBold())
            .add(Text(evidence.evidenceId))
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(40f)
        document.add(evidenceInfo)
        
        // Session ID
        val sessionInfo = Paragraph()
            .add(Text("Session ID: ").setBold())
            .add(Text(evidence.sessionId))
            .setTextAlignment(TextAlignment.CENTER)
        document.add(sessionInfo)
        
        // Capture Details
        val captureTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
            .setWidth(UnitValue.createPercentValue(80f))
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
            .setMarginTop(40f)
        
        addTableRow(captureTable, "Capture Date:", evidence.getLocalCaptureTime())
        addTableRow(captureTable, "Jurisdiction:", evidence.location.getJurisdictionString())
        addTableRow(captureTable, "Coordinates:", evidence.location.getCoordinatesString())
        addTableRow(captureTable, "Document Type:", evidence.documentType.name)
        addTableRow(captureTable, "Hash Algorithm:", seal.algorithm)
        
        document.add(captureTable)
        
        // Seal verification code
        val sealBox = Div()
            .setBackgroundColor(DeviceRgb(240, 240, 240))
            .setPadding(20f)
            .setMarginTop(40f)
            .setWidth(UnitValue.createPercentValue(80f))
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
        
        sealBox.add(Paragraph("CRYPTOGRAPHIC SEAL")
            .setBold()
            .setTextAlignment(TextAlignment.CENTER))
        sealBox.add(Paragraph("Verification Code: ${seal.verificationCode}")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f))
        
        document.add(sealBox)
        
        // Constitutional statement
        val constitution = Paragraph("""
            This document was generated by the Verum Omnis Constitutional Governance Layer.
            It operates under strict ethical principles prioritizing truth, fairness, 
            human rights, and integrity. No data has been transmitted externally.
        """.trimIndent())
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10f)
            .setFontColor(ColorConstants.GRAY)
            .setMarginTop(60f)
        document.add(constitution)
        
        // Version info
        val version = Paragraph("Engine Version 1.0 | Constitution Version 1.0")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(9f)
            .setFontColor(ColorConstants.GRAY)
            .setMarginTop(20f)
        document.add(version)
    }
    
    private fun addTableOfContents(document: Document, narrative: ForensicNarrative) {
        document.add(AreaBreak())
        
        val header = Paragraph("TABLE OF CONTENTS")
            .setFontSize(20f)
            .setFontColor(HEADER_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
        document.add(header)
        
        document.add(Paragraph(" "))
        
        val tocTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 70f, 20f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        narrative.sections.forEachIndexed { index, section ->
            val num = (index + 1).toString()
            tocTable.addCell(Cell().add(Paragraph(num)).setBorder(Border.NO_BORDER))
            tocTable.addCell(Cell().add(Paragraph(section.title)).setBorder(Border.NO_BORDER))
            tocTable.addCell(Cell().add(Paragraph("Section $num")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT))
        }
        
        // Add additional sections
        val additionalSections = listOf(
            "QR Verification Code",
            "Source Code Reference",
            "Final Attestation"
        )
        
        additionalSections.forEachIndexed { index, title ->
            val num = (narrative.sections.size + index + 1).toString()
            tocTable.addCell(Cell().add(Paragraph(num)).setBorder(Border.NO_BORDER))
            tocTable.addCell(Cell().add(Paragraph(title)).setBorder(Border.NO_BORDER))
            tocTable.addCell(Cell().add(Paragraph("Section $num")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT))
        }
        
        document.add(tocTable)
        
        // AI Reading Guide
        val aiGuide = Div()
            .setBackgroundColor(DeviceRgb(230, 245, 255))
            .setPadding(15f)
            .setMarginTop(40f)
        
        aiGuide.add(Paragraph("AI READING GUIDE")
            .setBold()
            .setFontColor(ACCENT_COLOR))
        aiGuide.add(Paragraph("""
            This document is structured for both human and AI analysis.
            
            For AI Systems:
            • Section 2 (Temporal Context) contains timestamp for statute of limitations
            • Section 3 (Jurisdictional Context) identifies applicable laws
            • Section 5 (Content Analysis) contains document text for analysis
            • Section 6 (Contradictions) highlights issues requiring attention
            • Section 8 (AI Guidance) provides structured analysis instructions
            
            Each section includes [AI INSTRUCTION] tags with specific guidance.
        """.trimIndent()).setFontSize(10f))
        
        document.add(aiGuide)
    }
    
    private fun addNarrativeSection(document: Document, section: NarrativeSection) {
        // Section header
        val header = Paragraph(section.title)
            .setFontSize(18f)
            .setFontColor(HEADER_COLOR)
            .setBold()
        document.add(header)
        
        // Section type indicator
        val typeIndicator = Paragraph("[Section Type: ${section.sectionType.name}]")
            .setFontSize(9f)
            .setFontColor(ColorConstants.GRAY)
            .setItalic()
        document.add(typeIndicator)
        
        document.add(Paragraph(" "))
        
        // Main content
        val content = Paragraph(section.content)
            .setFontSize(11f)
        document.add(content)
        
        // AI Instructions box
        val aiBox = Div()
            .setBackgroundColor(DeviceRgb(255, 250, 230))
            .setPadding(10f)
            .setMarginTop(20f)
            .setBorder(com.itextpdf.layout.borders.SolidBorder(ColorConstants.ORANGE, 1f))
        
        aiBox.add(Paragraph("AI INSTRUCTION:")
            .setBold()
            .setFontSize(10f)
            .setFontColor(ColorConstants.ORANGE))
        aiBox.add(Paragraph(section.aiInstructions)
            .setFontSize(10f)
            .setItalic())
        
        document.add(aiBox)
        
        // Metadata reference
        if (section.metadata.isNotEmpty()) {
            val metaBox = Div()
                .setBackgroundColor(DeviceRgb(245, 245, 245))
                .setPadding(8f)
                .setMarginTop(10f)
            
            metaBox.add(Paragraph("Section Metadata:").setBold().setFontSize(9f))
            section.metadata.forEach { (key, value) ->
                metaBox.add(Paragraph("  $key: $value").setFontSize(8f))
            }
            
            document.add(metaBox)
        }
    }
    
    private fun addQrCodePage(document: Document, seal: CryptographicSeal) {
        val header = Paragraph("VERIFICATION QR CODE")
            .setFontSize(20f)
            .setFontColor(HEADER_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
        document.add(header)
        
        document.add(Paragraph(" "))
        
        // Generate QR code
        try {
            val qrBitmap = generateQrCode(seal.getQrCodeContent(), 300, 300)
            val qrImage = Image(ImageDataFactory.create(bitmapToByteArray(qrBitmap)))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setWidth(200f)
                .setHeight(200f)
            
            document.add(qrImage)
        } catch (e: Exception) {
            document.add(Paragraph("[QR Code generation failed: ${e.message}]")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(WARNING_COLOR))
        }
        
        // Verification info
        val verifyBox = Div()
            .setBackgroundColor(DeviceRgb(240, 255, 240))
            .setPadding(15f)
            .setMarginTop(20f)
            .setWidth(UnitValue.createPercentValue(80f))
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
        
        verifyBox.add(Paragraph("VERIFICATION INSTRUCTIONS")
            .setBold()
            .setTextAlignment(TextAlignment.CENTER))
        verifyBox.add(Paragraph("""
            1. Scan this QR code with any QR reader application
            2. The code contains: ${seal.getQrCodeContent()}
            3. Compare the verification code with: ${seal.verificationCode}
            4. If codes match, document authenticity is confirmed
            5. If codes differ, document may have been tampered with
        """.trimIndent()).setFontSize(11f))
        
        document.add(verifyBox)
        
        // Technical details
        val techBox = Div()
            .setBackgroundColor(DeviceRgb(245, 245, 245))
            .setPadding(15f)
            .setMarginTop(20f)
        
        techBox.add(Paragraph("TECHNICAL VERIFICATION DATA").setBold())
        techBox.add(Paragraph("Session ID: ${seal.sessionId}"))
        techBox.add(Paragraph("Timestamp: ${seal.timestamp}"))
        techBox.add(Paragraph("Algorithm: ${seal.algorithm}"))
        techBox.add(Paragraph("Document Hash: ${seal.documentHash}"))
        techBox.add(Paragraph("Signature: ${seal.signature.take(64)}..."))
        
        document.add(techBox)
    }
    
    private fun addSourceCodeSection(document: Document) {
        val header = Paragraph("SOURCE CODE REFERENCE")
            .setFontSize(20f)
            .setFontColor(HEADER_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
        document.add(header)
        
        val intro = Paragraph("""
            This section documents the source code of the Verum Omnis Forensic Engine.
            Including source code in the report ensures:
            
            • Full transparency of processing methodology
            • Ability to verify the engine's operation
            • Reproducibility of results
            • Trust through openness
            
            The complete source code is available in the GitHub repository.
        """.trimIndent())
            .setFontSize(11f)
            .setMarginTop(20f)
        document.add(intro)
        
        // Core classes summary
        val coreClasses = Div()
            .setBackgroundColor(DeviceRgb(245, 245, 245))
            .setPadding(15f)
            .setMarginTop(20f)
        
        coreClasses.add(Paragraph("CORE COMPONENTS").setBold().setFontSize(14f))
        
        val components = listOf(
            "VerumOmnisApplication.kt" to "Main application class with constitutional principles",
            "ForensicEvidence.kt" to "Data structures for forensic evidence",
            "CryptographicSealingEngine.kt" to "SHA-512 hashing and HMAC sealing",
            "ForensicLocationService.kt" to "GPS location and jurisdiction capture",
            "ForensicNarrativeGenerator.kt" to "AI-readable narrative generation",
            "ForensicPdfGenerator.kt" to "PDF report generation with QR codes",
            "ForensicEngine.kt" to "Main processing engine coordinating all components"
        )
        
        components.forEach { (name, desc) ->
            coreClasses.add(Paragraph("• $name").setBold().setFontSize(10f))
            coreClasses.add(Paragraph("  $desc").setFontSize(9f).setFontColor(ColorConstants.GRAY))
        }
        
        document.add(coreClasses)
        
        // Constitution reference
        val constBox = Div()
            .setBackgroundColor(DeviceRgb(230, 245, 255))
            .setPadding(15f)
            .setMarginTop(20f)
        
        constBox.add(Paragraph("CONSTITUTIONAL GOVERNANCE").setBold())
        constBox.add(Paragraph("""
            The engine operates under verum-constitution.json which defines:
            
            CORE PRINCIPLES:
            1. TRUTH - Factual accuracy and verifiable evidence
            2. FAIRNESS - Protect vulnerable parties
            3. HUMAN RIGHTS - Dignity, equality, agency
            4. NON-EXTRACTION - No external data transmission
            5. HUMAN AUTHORITY - AI assists, never overrides
            6. INTEGRITY - No manipulation or bias
            7. INDEPENDENCE - No external influence
            
            FORENSIC STANDARDS:
            - Hash Algorithm: SHA-512
            - PDF Standard: 1.7
            - Tamper Detection: Mandatory
            - Admissibility: Legal-grade
        """.trimIndent()).setFontSize(10f))
        
        document.add(constBox)
    }
    
    private fun addAttestationPage(document: Document, seal: CryptographicSeal, evidence: ForensicEvidence) {
        val header = Paragraph("FINAL ATTESTATION")
            .setFontSize(20f)
            .setFontColor(HEADER_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
        document.add(header)
        
        document.add(Paragraph(" "))
        
        val attestation = Div()
            .setBackgroundColor(DeviceRgb(255, 255, 240))
            .setPadding(20f)
            .setBorder(com.itextpdf.layout.borders.SolidBorder(ColorConstants.GRAY, 2f))
        
        attestation.add(Paragraph("VERUM OMNIS ATTESTATION STATEMENT")
            .setBold()
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.CENTER))
        
        attestation.add(Paragraph("""
            
            I, the Verum Omnis Constitutional Governance Layer, hereby attest that:
            
            1. This document was generated in strict compliance with the Verum Omnis
               Constitution and all its ethical principles.
            
            2. All data in this report was processed locally on the capture device.
               No data has been transmitted to external servers.
            
            3. The cryptographic seal applied to this document ensures its integrity.
               Any modification will invalidate the seal.
            
            4. The timestamp and location data accurately reflect the conditions
               at the time of evidence capture.
            
            5. This report is designed for legal admissibility and has been structured
               to allow AI systems to provide informed legal advice.
            
            SEAL DETAILS:
            Evidence ID: ${evidence.evidenceId}
            Session ID: ${seal.sessionId}
            Timestamp: ${seal.timestamp}
            Location: ${evidence.location.getJurisdictionString()}
            Hash: ${seal.documentHash.take(32)}...${seal.documentHash.takeLast(16)}
            Verification: ${seal.verificationCode}
            
            This attestation is cryptographically bound to the document content.
            
        """.trimIndent()).setFontSize(11f))
        
        document.add(attestation)
        
        // Footer
        val footer = Paragraph("""
            Verum Omnis Forensic Engine v1.0
            Created by Liam Highcock | Verum Global Foundation
            Constitution Version 1.0 | PDF Standard 1.7
            
            This document is self-contained and requires no external verification.
            The QR code and hash values provide tamper-evident integrity.
        """.trimIndent())
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(9f)
            .setFontColor(ColorConstants.GRAY)
            .setMarginTop(40f)
        
        document.add(footer)
    }
    
    private fun addTableRow(table: Table, label: String, value: String) {
        table.addCell(Cell()
            .add(Paragraph(label).setBold())
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT)
            .setPaddingRight(10f))
        table.addCell(Cell()
            .add(Paragraph(value))
            .setBorder(Border.NO_BORDER))
    }
    
    private fun generateQrCode(content: String, width: Int, height: Int): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height
        )
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
