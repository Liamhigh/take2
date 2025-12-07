package org.verumomnis.forensic.engine

import android.content.Context
import org.verumomnis.forensic.model.Case

/**
 * Orchestrates the full engine pipeline
 * Takes evidence -> runs all engines -> generates full final report
 */
class EngineOrchestrator(private val context: Context) {
    
    private val parser = EvidenceParser(context)
    private val contradictionEngine = ContradictionEngine()
    private val lawEngine = LawEngine(context)
    
    fun run(case: Case): String {
        val allStatements = mutableListOf<String>()
        
        case.evidence.forEach {
            allStatements += parser.parseEvidence(it)
        }
        
        val contradictions = contradictionEngine.detectContradictions(allStatements)
        val legalFindings = lawEngine.evaluate(allStatements)
        
        return buildReport(case, contradictions, legalFindings)
    }
    
    private fun buildReport(
        case: Case,
        contradictions: List<org.verumomnis.forensic.model.Contradiction>,
        legalFindings: List<String>
    ): String {
        val sb = StringBuilder()
        
        sb.appendLine("VERUM OMNIS FORENSIC REPORT")
        sb.appendLine("Case: ${case.caseName}")
        sb.appendLine("====================================")
        sb.appendLine()
        
        sb.appendLine("EVIDENCE SUMMARY:")
        sb.appendLine("------------------------------------")
        case.evidence.forEach {
            sb.appendLine("- ${it.summary}")
        }
        sb.appendLine()
        
        sb.appendLine("CONTRADICTIONS FOUND:")
        sb.appendLine("------------------------------------")
        if (contradictions.isEmpty()) sb.appendLine("None detected.")
        contradictions.forEach {
            sb.appendLine("• \"${it.statementA}\" contradicts \"${it.statementB}\"")
            sb.appendLine("  Reason: ${it.reason}")
        }
        sb.appendLine()
        
        sb.appendLine("LEGAL EVALUATION:")
        sb.appendLine("------------------------------------")
        if (legalFindings.isEmpty()) sb.appendLine("No legal findings.")
        legalFindings.forEach {
            sb.appendLine("- $it")
        }
        
        return sb.toString()
    }
}
