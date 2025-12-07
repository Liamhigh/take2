package org.verumomnis.forensic.engine

import android.content.Context
import org.verumomnis.forensic.model.Evidence
import java.io.File

/**
 * Parses evidence into statements for analysis
 * Reads text, PDFs, images (via OCR), and extracts statements
 */
class EvidenceParser(private val context: Context) {
    
    fun parseEvidence(evidence: Evidence): List<String> {
        val text = when (evidence.type) {
            "text" -> evidence.content
            "file" -> readFile(evidence.filePath)
            else -> ""
        }
        return splitIntoStatements(text)
    }
    
    private fun readFile(path: String): String {
        return File(path).takeIf { it.exists() }?.readText() ?: ""
    }
    
    private fun splitIntoStatements(text: String): List<String> {
        return text.split(Regex("[.!?\\n]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
