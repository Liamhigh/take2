package org.verumomnis.forensic.core

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.report.ForensicNarrativeGenerator
import java.io.ByteArrayOutputStream
import java.time.Instant

/**
 * Document Processor for Verum Omnis Forensic Engine
 *
 * Implements stateless document processing following verum-constitution.json:
 * - Processes PDFs, images, and text files
 * - Applies Verum Omnis logic via RuleEngine
 * - Generates AI-readable narratives
 * - Creates cryptographically sealed output
 *
 * Security principles:
 * - offline_first: true
 * - stateless: true
 * - no_cloud_logging: true
 */
class DocumentProcessor(private val context: Context) {

    companion object {
        private const val MAX_TEXT_LENGTH = 1_000_000 // 1MB max text
    }

    private val ruleEngine = RuleEngine(context)
    private val sealingEngine = CryptographicSealingEngine()
    private val narrativeGenerator = ForensicNarrativeGenerator()

    init {
        ruleEngine.loadRules()
    }

    /**
     * Processes a document and returns a forensic result
     *
     * @param input The document input containing URI and type
     * @return ForensicDocumentResult with analysis, narrative, and seal
     */
    suspend fun processDocument(input: DocumentInput): ForensicDocumentResult =
        withContext(Dispatchers.IO) {
            val timestamp = Instant.now()

            // 1. Extract text from document
            val extractedText = extractText(input)

            // 2. Apply Verum Omnis logic
            val analysis = ruleEngine.analyzeText(extractedText)

            // 3. Generate narrative
            val narrative = generateNarrative(extractedText, analysis)

            // 4. Create content for sealing
            val contentJson = createContentJson(input, analysis, narrative)

            // 5. Compute hash and create seal
            val contentHash = sealingEngine.computeHash(contentJson)
            val seal = sealingEngine.createSeal(
                contentHash = contentHash,
                timestamp = timestamp,
                location = null,
                metadata = mapOf(
                    "document_type" to input.type.name,
                    "processing_version" to "1.0"
                )
            )

            ForensicDocumentResult(
                documentType = input.type,
                extractedText = extractedText,
                analysis = analysis,
                narrative = narrative,
                contentHash = contentHash,
                sealJson = seal.toJson(),
                timestamp = timestamp
            )
        }

    /**
     * Extracts text from the document based on its type
     */
    private fun extractText(input: DocumentInput): String {
        return when (input.type) {
            DocumentType.PDF -> extractFromPdf(input.uri)
            DocumentType.IMAGE -> extractFromImage(input.uri)
            DocumentType.TEXT -> readTextFile(input.uri)
            DocumentType.RAW_TEXT -> input.rawText ?: ""
        }
    }

    /**
     * Extracts text from a PDF file
     * Note: In production, would use PDFBox or similar library
     */
    private fun extractFromPdf(uri: Uri?): String {
        if (uri == null) return ""

        return try {
            // Read PDF bytes
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(4096)
                var bytesRead: Int

                while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
                    buffer.write(data, 0, bytesRead)
                }

                // For now, return a placeholder - in production would parse PDF
                // Using PDFBox: PDDocument.load(buffer.toByteArray())
                "[PDF content would be extracted here - ${buffer.size()} bytes]"
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Extracts text from an image using OCR
     * Note: In production, would use ML Kit Text Recognition
     */
    private fun extractFromImage(uri: Uri?): String {
        if (uri == null) return ""

        return try {
            // For now, return a placeholder - in production would use ML Kit OCR
            // Using ML Kit: TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            "[Image OCR content would be extracted here]"
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Reads a text file
     */
    private fun readTextFile(uri: Uri?): String {
        if (uri == null) return ""

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    val text = reader.readText()
                    if (text.length > MAX_TEXT_LENGTH) {
                        text.take(MAX_TEXT_LENGTH)
                    } else {
                        text
                    }
                }
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Generates a forensic narrative from the analysis
     */
    private fun generateNarrative(text: String, analysis: RuleAnalysisResult): String {
        return buildString {
            appendLine("FORENSIC DOCUMENT ANALYSIS")
            appendLine("=" .repeat(60))
            appendLine()

            // Analysis Summary
            appendLine("ANALYSIS SUMMARY")
            appendLine("-".repeat(40))
            appendLine("Score: ${analysis.score}/100")
            appendLine("Severity: ${analysis.severity}")
            appendLine()

            // Keywords Found
            if (analysis.keywordMatches.isNotEmpty()) {
                appendLine("KEYWORDS DETECTED")
                appendLine("-".repeat(40))
                analysis.keywordMatches.groupBy { it.category }.forEach { (category, matches) ->
                    appendLine("Category: ${category.replaceFirstChar { it.uppercase() }}")
                    matches.forEach { match ->
                        appendLine("  - ${match.keyword}")
                    }
                }
                appendLine()
            }

            // Legal Subjects
            if (analysis.legalSubjects.isNotEmpty()) {
                appendLine("LEGAL SUBJECTS IDENTIFIED")
                appendLine("-".repeat(40))
                analysis.legalSubjects.forEach { subject ->
                    appendLine("${subject.name} [${subject.severity}]")
                    appendLine("  Category: ${subject.category}")
                    appendLine("  Matching keywords: ${subject.matchingKeywords.joinToString(", ")}")
                }
                appendLine()
            }

            // Red Flags
            if (analysis.redFlags.isNotEmpty()) {
                appendLine("RED FLAGS DETECTED")
                appendLine("-".repeat(40))
                analysis.redFlags.forEach { flag ->
                    appendLine("${flag.category.replaceFirstChar { it.uppercase() }}: ${flag.description}")
                    appendLine("  Weight: ${flag.weight}")
                    appendLine("  Matched: \"${flag.matchedText.take(50)}...\"")
                }
                appendLine()
            }

            // Tags
            if (analysis.tags.isNotEmpty()) {
                appendLine("FORENSIC TAGS")
                appendLine("-".repeat(40))
                appendLine(analysis.tags.joinToString(" "))
                appendLine()
            }

            // Extracted Text Preview
            if (text.isNotBlank()) {
                appendLine("TEXT EXCERPT")
                appendLine("-".repeat(40))
                appendLine(text.take(500))
                if (text.length > 500) {
                    appendLine("... [${text.length - 500} more characters]")
                }
            }

            appendLine()
            appendLine("=" .repeat(60))
            appendLine("Generated by Verum Omnis Forensic Engine")
        }
    }

    /**
     * Creates JSON content for sealing
     */
    private fun createContentJson(
        input: DocumentInput,
        analysis: RuleAnalysisResult,
        narrative: String
    ): String {
        return buildString {
            appendLine("{")
            appendLine("  \"document_type\": \"${input.type.name}\",")
            appendLine("  \"analysis\": {")
            appendLine("    \"score\": ${analysis.score},")
            appendLine("    \"severity\": \"${analysis.severity}\",")
            appendLine("    \"keyword_count\": ${analysis.keywordMatches.size},")
            appendLine("    \"legal_subject_count\": ${analysis.legalSubjects.size},")
            appendLine("    \"red_flag_count\": ${analysis.redFlags.size}")
            appendLine("  },")
            appendLine("  \"narrative_hash\": \"${sealingEngine.computeHash(narrative)}\"")
            appendLine("}")
        }
    }
}

/**
 * Types of documents that can be processed
 */
enum class DocumentType {
    PDF,
    IMAGE,
    TEXT,
    RAW_TEXT
}

/**
 * Input for document processing
 */
data class DocumentInput(
    val uri: Uri? = null,
    val type: DocumentType,
    val rawText: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Result of document processing
 */
data class ForensicDocumentResult(
    val documentType: DocumentType,
    val extractedText: String,
    val analysis: RuleAnalysisResult,
    val narrative: String,
    val contentHash: String,
    val sealJson: String,
    val timestamp: Instant
) {
    /**
     * Creates a summary for display
     */
    fun getSummary(): String = buildString {
        appendLine("Document Type: ${documentType.name}")
        appendLine("Analysis Score: ${analysis.score}/100")
        appendLine("Severity: ${analysis.severity}")
        appendLine("Keywords: ${analysis.keywordMatches.size}")
        appendLine("Legal Subjects: ${analysis.legalSubjects.size}")
        appendLine("Red Flags: ${analysis.redFlags.size}")
        appendLine("Hash: ${contentHash.take(32)}...")
        appendLine("Timestamp: $timestamp")
    }
}
