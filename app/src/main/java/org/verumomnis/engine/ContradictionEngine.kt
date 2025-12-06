package org.verumomnis.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

/**
 * LAYER 2 - CONTRADICTION ENGINE
 * 
 * Detects conflicting statements between any two sentences.
 * Implements 7 deterministic contradiction rules.
 * 
 * This is a rule-based, non-AI engine. Logic never varies between cases.
 */
class ContradictionEngine {
    
    companion object {
        private const val REPORT_LINE_LENGTH = 50
        
        // Keywords for contradiction detection
        private val NEGATION_KEYWORDS = listOf("never", "did not", "didn't", "no", "not", "none")
        private val AFFIRMATION_KEYWORDS = listOf("yes", "did", "made", "sent", "agreed")
        
        // Event keywords for timeline conflicts
        private val EVENT_KEYWORDS = listOf("met", "meeting", "invoice", "payment", "call", "email", "sent")
        
        // Quantity pattern
        private val QUANTITY_PATTERN = Pattern.compile("\\b(one|two|three|four|five|\\d+)\\b")
    }
    
    /**
     * Analyze sentences for contradictions
     * Returns list of all detected contradictions
     */
    suspend fun analyze(sentences: List<Sentence>): List<ContradictionResult> = withContext(Dispatchers.Default) {
        val results = mutableListOf<ContradictionResult>()
        
        // Compare each sentence with every other sentence
        for (i in sentences.indices) {
            for (j in (i + 1) until sentences.size) {
                val sentenceA = sentences[i]
                val sentenceB = sentences[j]
                
                val reason = contradictionRule(sentenceA, sentenceB)
                if (reason != null) {
                    results.add(ContradictionResult(sentenceA, sentenceB, reason))
                }
            }
        }
        
        results
    }
    
    /**
     * Apply contradiction rules to two sentences
     * Returns reason if contradiction detected, null otherwise
     */
    private fun contradictionRule(a: Sentence, b: Sentence): String? {
        val textA = a.text.lowercase()
        val textB = b.text.lowercase()
        
        // RULE 1 - Direct Negation
        val rule1 = checkDirectNegation(textA, textB)
        if (rule1 != null) return rule1
        
        // RULE 2 - Denial vs Evidence
        val rule2 = checkDenialVsEvidence(textA, textB)
        if (rule2 != null) return rule2
        
        // RULE 3 - Timeline Conflicts
        val rule3 = checkTimelineConflict(a, b, textA, textB)
        if (rule3 != null) return rule3
        
        // RULE 4 - Quantity Conflicts
        val rule4 = checkQuantityConflict(textA, textB)
        if (rule4 != null) return rule4
        
        // RULE 5 - Admission vs Later Denial
        val rule5 = checkAdmissionVsDenial(textA, textB)
        if (rule5 != null) return rule5
        
        // RULE 6 - Action vs Outcome Conflict
        val rule6 = checkActionVsOutcome(textA, textB)
        if (rule6 != null) return rule6
        
        // RULE 7 - Data Access Claim Conflicts
        val rule7 = checkDataAccessConflict(textA, textB)
        if (rule7 != null) return rule7
        
        return null
    }
    
    /**
     * RULE 1 - Direct Negation
     * "If one sentence contains 'never', 'did not', 'no', while the other affirms same event."
     */
    private fun checkDirectNegation(a: String, b: String): String? {
        val aHasNegation = NEGATION_KEYWORDS.any { a.contains(it) }
        val bHasNegation = NEGATION_KEYWORDS.any { b.contains(it) }
        
        // One has negation, other doesn't, and they share topic
        if (aHasNegation xor bHasNegation) {
            if (sharesTopic(a, b)) {
                return "RULE 1: Direct negation - one statement negates while other affirms"
            }
        }
        
        return null
    }
    
    /**
     * RULE 2 - Denial vs Evidence
     * Examples: "no payment" vs "payment made", "I never met him" vs "we met Monday"
     */
    private fun checkDenialVsEvidence(a: String, b: String): String? {
        val denialPhrases = listOf(
            "no payment" to "payment",
            "never met" to "met",
            "did not receive" to "received",
            "no contact" to "contact"
        )
        
        for ((denial, evidence) in denialPhrases) {
            if ((a.contains(denial) && b.contains(evidence)) ||
                (b.contains(denial) && a.contains(evidence))) {
                return "RULE 2: Denial contradicts evidence - '$denial' vs '$evidence'"
            }
        }
        
        return null
    }
    
    /**
     * RULE 3 - Timeline Conflicts
     * Different months/dates for same event topic
     */
    private fun checkTimelineConflict(a: Sentence, b: Sentence, textA: String, textB: String): String? {
        // Check if both sentences mention the same event type
        for (event in EVENT_KEYWORDS) {
            if (textA.contains(event) && textB.contains(event)) {
                // Check if they have different timestamps
                if (a.timestamp != null && b.timestamp != null && a.timestamp != b.timestamp) {
                    return "RULE 3: Timeline conflict - same event '$event' with different dates"
                }
                
                // Check for month names that differ
                val months = listOf("january", "february", "march", "april", "may", "june",
                                   "july", "august", "september", "october", "november", "december",
                                   "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
                
                val monthA = months.firstOrNull { textA.contains(it) }
                val monthB = months.firstOrNull { textB.contains(it) }
                
                if (monthA != null && monthB != null && monthA != monthB) {
                    return "RULE 3: Timeline conflict - event '$event' in different months"
                }
            }
        }
        
        return null
    }
    
    /**
     * RULE 4 - Quantity Conflicts
     * Different numbers for same subject: "one meeting" vs "three meetings"
     */
    private fun checkQuantityConflict(a: String, b: String): String? {
        val matcherA = QUANTITY_PATTERN.matcher(a)
        val matcherB = QUANTITY_PATTERN.matcher(b)
        
        if (matcherA.find() && matcherB.find()) {
            val qtyA = matcherA.group()
            val qtyB = matcherB.group()
            
            if (qtyA != qtyB && sharesTopic(a, b)) {
                return "RULE 4: Quantity conflict - different counts for same subject ('$qtyA' vs '$qtyB')"
            }
        }
        
        return null
    }
    
    /**
     * RULE 5 - Admission vs Later Denial
     * "I agreed" vs "I never agreed"
     */
    private fun checkAdmissionVsDenial(a: String, b: String): String? {
        val admissionPhrases = listOf("i agreed", "i accepted", "i said", "i confirmed", "i promised")
        val denialPhrases = listOf("never agreed", "didn't agree", "never accepted", "never said", "never promised")
        
        for (admission in admissionPhrases) {
            for (denial in denialPhrases) {
                if ((a.contains(admission) && b.contains(denial)) ||
                    (b.contains(admission) && a.contains(denial))) {
                    return "RULE 5: Admission contradicts later denial"
                }
            }
        }
        
        return null
    }
    
    /**
     * RULE 6 - Action vs Outcome Conflict
     * "I sent nothing" vs "email attached"
     */
    private fun checkActionVsOutcome(a: String, b: String): String? {
        val actionDenials = listOf("sent nothing", "no email", "no attachment", "didn't send")
        val outcomes = listOf("email", "attached", "sent", "received")
        
        for (denial in actionDenials) {
            if (a.contains(denial) || b.contains(denial)) {
                for (outcome in outcomes) {
                    if ((a.contains(denial) && b.contains(outcome)) ||
                        (b.contains(denial) && a.contains(outcome))) {
                        return "RULE 6: Action denial conflicts with outcome evidence"
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * RULE 7 - Data Access Claim Conflicts
     * "I did not access" vs evidence of access attempt
     */
    private fun checkDataAccessConflict(a: String, b: String): String? {
        val accessDenials = listOf("did not access", "never accessed", "no access", "didn't login")
        val accessEvidence = listOf("logged in", "accessed", "login attempt", "password", "entered")
        
        for (denial in accessDenials) {
            for (evidence in accessEvidence) {
                if ((a.contains(denial) && b.contains(evidence)) ||
                    (b.contains(denial) && a.contains(evidence))) {
                    return "RULE 7: Data access denial conflicts with access evidence"
                }
            }
        }
        
        return null
    }
    
    /**
     * Check if two sentences share a common topic
     */
    private fun sharesTopic(a: String, b: String): Boolean {
        val wordsA = a.split(" ").filter { it.length > 3 }.toSet()
        val wordsB = b.split(" ").filter { it.length > 3 }.toSet()
        
        val commonWords = wordsA.intersect(wordsB)
        val minWords = minOf(wordsA.size, wordsB.size)
        
        return if (minWords > 0) {
            commonWords.size.toFloat() / minWords > 0.3f
        } else {
            false
        }
    }
    
    /**
     * Build a text report from analysis results (legacy method for compatibility)
     */
    fun buildReport(results: List<ContradictionResult>): String {
        val sb = StringBuilder()
        sb.appendLine("VERUM OMNIS CONTRADICTION REPORT")
        sb.appendLine("=".repeat(REPORT_LINE_LENGTH))
        sb.appendLine()
        sb.appendLine("Total Contradictions Found: ${results.size}")
        sb.appendLine()
        
        if (results.isEmpty()) {
            sb.appendLine("No contradictions detected in the provided evidence.")
        } else {
            results.forEachIndexed { index, result ->
                sb.appendLine("CONTRADICTION #${index + 1}")
                sb.appendLine("-".repeat(REPORT_LINE_LENGTH))
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
        
        sb.appendLine("=".repeat(REPORT_LINE_LENGTH))
        sb.appendLine("End of Report")
        
        return sb.toString()
    }
}
