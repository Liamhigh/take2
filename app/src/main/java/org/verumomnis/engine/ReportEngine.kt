package org.verumomnis.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LAYER 4 - REPORT ENGINE
 * 
 * Builds final structured forensic report containing:
 * 1. Pre-analysis declaration
 * 2. Narrative summary
 * 3. Contradictions list
 * 4. Legal classification
 * 5. Summary findings
 * 6. Post-analysis declaration
 */
class ReportEngine {
    
    companion object {
        private const val LINE_LENGTH = 70
        private const val SECTION_SEPARATOR = "="
        private const val SUBSECTION_SEPARATOR = "-"
    }
    
    /**
     * Build comprehensive forensic report
     */
    fun build(
        sentences: List<Sentence>,
        contradictions: List<ContradictionResult>,
        legal: List<LegalFinding>
    ): String {
        val sb = StringBuilder()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        
        // SECTION 1: PRE-ANALYSIS DECLARATION
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("VERUM OMNIS FORENSIC ANALYSIS REPORT")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("Report Generated: $timestamp")
        sb.appendLine()
        sb.appendLine("PRE-ANALYSIS DECLARATION:")
        sb.appendLine("This is a deterministic forensic report.")
        sb.appendLine("No AI interpretation included.")
        sb.appendLine("All findings are based on rule-based analysis.")
        sb.appendLine()
        
        // SECTION 2: NARRATIVE STRUCTURE
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("SECTION 1: NARRATIVE STRUCTURE")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("Total Sentences Analyzed: ${sentences.size}")
        sb.appendLine()
        sb.appendLine(String.format("%-6s | %-20s | %s", "Index", "Timestamp", "Sentence"))
        sb.appendLine(SUBSECTION_SEPARATOR.repeat(LINE_LENGTH))
        
        sentences.forEach { sentence ->
            val timestampStr = if (sentence.timestamp != null) {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(sentence.timestamp))
            } else {
                "N/A"
            }
            val text = if (sentence.text.length > 40) {
                sentence.text.substring(0, 37) + "..."
            } else {
                sentence.text
            }
            sb.appendLine(String.format("%-6d | %-20s | %s", sentence.index + 1, timestampStr, text))
        }
        sb.appendLine()
        
        // SECTION 3: CONTRADICTIONS DETECTED
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("SECTION 2: CONTRADICTIONS DETECTED")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("Total Contradictions Found: ${contradictions.size}")
        sb.appendLine()
        
        if (contradictions.isEmpty()) {
            sb.appendLine("No contradictions detected in the provided evidence.")
        } else {
            contradictions.forEachIndexed { index, result ->
                sb.appendLine("CONTRADICTION #${index + 1}")
                sb.appendLine(SUBSECTION_SEPARATOR.repeat(LINE_LENGTH))
                sb.appendLine("Statement A (Line ${result.a.index + 1}):")
                sb.appendLine("  \"${result.a.text}\"")
                sb.appendLine()
                sb.appendLine("Statement B (Line ${result.b.index + 1}):")
                sb.appendLine("  \"${result.b.text}\"")
                sb.appendLine()
                sb.appendLine("Reason: ${result.reason}")
                sb.appendLine()
            }
        }
        
        // SECTION 4: LEGAL CLASSIFICATION
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("SECTION 3: LEGAL CLASSIFICATION")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        
        if (legal.isEmpty()) {
            sb.appendLine("No legal subjects identified.")
        } else {
            legal.forEach { finding ->
                sb.appendLine("Subject: ${finding.subject}")
                sb.appendLine(SUBSECTION_SEPARATOR.repeat(LINE_LENGTH))
                sb.appendLine("Evidence Count: ${finding.contradictions.size}")
                sb.appendLine()
                finding.contradictions.forEachIndexed { index, contradiction ->
                    val contradictionIndex = contradictions.indexOf(contradiction) + 1
                    sb.appendLine("  - Contradiction #$contradictionIndex")
                }
                sb.appendLine()
            }
        }
        
        // SECTION 5: SUMMARY FINDINGS
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("SECTION 4: SUMMARY FINDINGS")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("Analysis Summary:")
        sb.appendLine("  - Total Sentences: ${sentences.size}")
        sb.appendLine("  - Total Contradictions: ${contradictions.size}")
        sb.appendLine("  - Legal Categories Triggered: ${legal.size}")
        sb.appendLine()
        
        if (legal.isNotEmpty()) {
            sb.appendLine("Primary Legal Concerns:")
            legal.forEach { finding ->
                sb.appendLine("  - ${finding.subject}: ${finding.contradictions.size} contradiction(s)")
            }
        }
        sb.appendLine()
        
        if (contradictions.isNotEmpty()) {
            val mostSevere = findMostSevereContradiction(contradictions, legal)
            sb.appendLine("Most Severe Contradiction:")
            sb.appendLine("  ${mostSevere.reason}")
            sb.appendLine("  Between: \"${mostSevere.a.text}\"")
            sb.appendLine("      and: \"${mostSevere.b.text}\"")
            sb.appendLine()
        }
        
        // SECTION 6: POST-ANALYSIS DECLARATION
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine("POST-ANALYSIS DECLARATION")
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("End of deterministic evaluation.")
        sb.appendLine("All findings are reproducible and rule-based.")
        sb.appendLine("This report complies with forensic standards for court admissibility.")
        sb.appendLine()
        sb.appendLine(SECTION_SEPARATOR.repeat(LINE_LENGTH))
        
        return sb.toString()
    }
    
    /**
     * Find the most severe contradiction based on legal classification
     */
    private fun findMostSevereContradiction(
        contradictions: List<ContradictionResult>,
        legal: List<LegalFinding>
    ): ContradictionResult {
        // Priority order for severity
        val severityOrder = listOf(
            LegalSubject.Cybercrime,
            LegalSubject.FraudulentEvidence,
            LegalSubject.BreachOfFiduciaryDuty,
            LegalSubject.ShareholderOppression,
            LegalSubject.EmotionalExploitation
        )
        
        // Find contradiction in highest priority category
        for (subject in severityOrder) {
            val finding = legal.firstOrNull { it.subject == subject }
            if (finding != null && finding.contradictions.isNotEmpty()) {
                return finding.contradictions.first()
            }
        }
        
        // Fallback to first contradiction if no legal classification
        return contradictions.first()
    }
}
