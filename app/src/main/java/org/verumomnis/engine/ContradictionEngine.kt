package org.verumomnis.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Contradiction Engine for Verum Omnis
 * 
 * Analyzes text for contradictions and inconsistencies
 */
class ContradictionEngine {
    
    private val sentences = mutableListOf<Sentence>()
    
    /**
     * Ingest text and split into sentences
     */
    suspend fun ingest(text: String) = withContext(Dispatchers.Default) {
        sentences.clear()
        val lines = text.split("\n").filter { it.isNotBlank() }
        lines.forEachIndexed { index, line ->
            sentences.add(Sentence(line.trim(), index))
        }
    }
    
    /**
     * Analyze ingested text for contradictions
     */
    suspend fun analyze(): List<ContradictionResult> = withContext(Dispatchers.Default) {
        val results = mutableListOf<ContradictionResult>()
        
        // Compare each sentence with every other sentence
        for (i in sentences.indices) {
            for (j in (i + 1) until sentences.size) {
                val sentenceA = sentences[i]
                val sentenceB = sentences[j]
                
                val contradiction = detectContradiction(sentenceA, sentenceB)
                if (contradiction != null) {
                    results.add(contradiction)
                }
            }
        }
        
        results
    }
    
    /**
     * Detect if two sentences contradict each other
     */
    private fun detectContradiction(a: Sentence, b: Sentence): ContradictionResult? {
        val textA = a.text.lowercase()
        val textB = b.text.lowercase()
        
        // Check for opposite statements
        if (hasOppositeStatements(textA, textB)) {
            return ContradictionResult(
                a = a,
                b = b,
                reason = "Opposite statements detected"
            )
        }
        
        // Check for temporal contradictions (did vs didn't, was vs wasn't)
        if (hasTemporalContradiction(textA, textB)) {
            return ContradictionResult(
                a = a,
                b = b,
                reason = "Temporal contradiction detected"
            )
        }
        
        // Check for negation contradictions
        if (hasNegationContradiction(textA, textB)) {
            return ContradictionResult(
                a = a,
                b = b,
                reason = "Negation contradiction detected"
            )
        }
        
        return null
    }
    
    private fun hasOppositeStatements(a: String, b: String): Boolean {
        val opposites = listOf(
            "yes" to "no",
            "true" to "false",
            "always" to "never",
            "present" to "absent",
            "paid" to "unpaid",
            "received" to "not received"
        )
        
        for ((pos, neg) in opposites) {
            if ((a.contains(pos) && b.contains(neg)) || 
                (a.contains(neg) && b.contains(pos))) {
                return true
            }
        }
        
        return false
    }
    
    private fun hasTemporalContradiction(a: String, b: String): Boolean {
        val patterns = listOf(
            "did" to "didn't",
            "was" to "wasn't",
            "were" to "weren't",
            "has" to "hasn't",
            "have" to "haven't"
        )
        
        for ((pos, neg) in patterns) {
            if ((a.contains(pos) && b.contains(neg)) || 
                (a.contains(neg) && b.contains(pos))) {
                return true
            }
        }
        
        return false
    }
    
    private fun hasNegationContradiction(a: String, b: String): Boolean {
        // Check if one sentence is a negation of the other
        return (a.contains("not") && !b.contains("not") || 
                !a.contains("not") && b.contains("not")) &&
                similarSentences(a, b)
    }
    
    private fun similarSentences(a: String, b: String): Boolean {
        val wordsA = a.split(" ").filter { it.length > 3 }
        val wordsB = b.split(" ").filter { it.length > 3 }
        
        val commonWords = wordsA.intersect(wordsB.toSet())
        val minWords = minOf(wordsA.size, wordsB.size)
        
        return if (minWords > 0) {
            commonWords.size.toFloat() / minWords > 0.5f
        } else {
            false
        }
    }
    
    /**
     * Build a text report from analysis results
     */
    fun buildReport(results: List<ContradictionResult>): String {
        val sb = StringBuilder()
        sb.appendLine("VERUM OMNIS CONTRADICTION REPORT")
        sb.appendLine("=" .repeat(50))
        sb.appendLine()
        sb.appendLine("Total Contradictions Found: ${results.size}")
        sb.appendLine()
        
        if (results.isEmpty()) {
            sb.appendLine("No contradictions detected in the provided evidence.")
        } else {
            results.forEachIndexed { index, result ->
                sb.appendLine("CONTRADICTION #${index + 1}")
                sb.appendLine("-".repeat(50))
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
        
        sb.appendLine("=" .repeat(50))
        sb.appendLine("End of Report")
        
        return sb.toString()
    }
}
