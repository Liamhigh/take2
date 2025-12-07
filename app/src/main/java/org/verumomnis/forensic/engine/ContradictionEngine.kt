package org.verumomnis.forensic.engine

import org.verumomnis.forensic.model.Contradiction

/**
 * Detects contradictions between statements
 */
class ContradictionEngine {
    
    fun detectContradictions(statements: List<String>): List<Contradiction> {
        val results = mutableListOf<Contradiction>()
        
        for (i in statements.indices) {
            for (j in i + 1 until statements.size) {
                val a = statements[i]
                val b = statements[j]
                
                if (isContradiction(a, b)) {
                    results.add(Contradiction(a, b, "Direct contradiction detected"))
                }
            }
        }
        
        return results
    }
    
    private fun isContradiction(a: String, b: String): Boolean {
        if (a.contains("did") && b.contains("did not")) return true
        if (a.contains("never") && b.contains("did")) return true
        if (a.contains("always") && b.contains("never")) return true
        if (a.contains("was") && b.contains("was not")) return true
        return false
    }
}
