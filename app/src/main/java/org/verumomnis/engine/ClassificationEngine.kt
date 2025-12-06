package org.verumomnis.engine

/**
 * LAYER 3 - CLASSIFICATION ENGINE
 * 
 * Maps contradictions to legal subject categories defined in Verum Omnis.
 * This is a deterministic, keyword-based classification system.
 */
class ClassificationEngine {
    
    companion object {
        // Keywords for each legal subject
        private val SHAREHOLDER_OPPRESSION_KEYWORDS = listOf(
            "profit", "agreement", "deal", "decision", "responsibility", "ownership",
            "shareholder", "dividend", "equity", "shares", "business"
        )
        
        private val FRAUDULENT_EVIDENCE_KEYWORDS = listOf(
            "delete", "removed", "cropped", "missing", "screenshot", "edited",
            "altered", "modified", "tampered", "fabricated"
        )
        
        private val CYBERCRIME_KEYWORDS = listOf(
            "access", "login", "password", "device", "breach", "unauthorized",
            "hacked", "account", "credentials", "system"
        )
        
        private val BREACH_OF_FIDUCIARY_DUTY_KEYWORDS = listOf(
            "managing", "accounting", "decision-making", "duty", "lied",
            "fiduciary", "trust", "responsibility", "obligation", "director"
        )
        
        private val EMOTIONAL_EXPLOITATION_KEYWORDS = listOf(
            "gaslight", "you said", "you did", "never happened", "emotional",
            "manipulate", "abuse", "control", "deny", "twist"
        )
    }
    
    /**
     * Classify contradictions into legal subject categories
     * Each contradiction may map to multiple categories
     */
    fun classify(results: List<ContradictionResult>): List<LegalFinding> {
        val findingsMap = mutableMapOf<LegalSubject, MutableList<ContradictionResult>>()
        
        // Initialize all subjects
        LegalSubject.values().forEach { subject ->
            findingsMap[subject] = mutableListOf()
        }
        
        // Classify each contradiction
        for (contradiction in results) {
            val subjects = determineSubjects(contradiction)
            for (subject in subjects) {
                findingsMap[subject]?.add(contradiction)
            }
        }
        
        // Convert to list of LegalFinding (only include subjects with contradictions)
        return findingsMap
            .filter { it.value.isNotEmpty() }
            .map { (subject, contradictions) ->
                LegalFinding(subject, contradictions)
            }
    }
    
    /**
     * Determine which legal subjects apply to a contradiction
     */
    private fun determineSubjects(contradiction: ContradictionResult): List<LegalSubject> {
        val subjects = mutableListOf<LegalSubject>()
        val combinedText = "${contradiction.a.text} ${contradiction.b.text} ${contradiction.reason}".lowercase()
        
        // RULE A - Corporate / Business Conflicts → ShareholderOppression
        if (matchesKeywords(combinedText, SHAREHOLDER_OPPRESSION_KEYWORDS)) {
            subjects.add(LegalSubject.ShareholderOppression)
        }
        
        // RULE B - Evidence Tampering → FraudulentEvidence
        if (matchesKeywords(combinedText, FRAUDULENT_EVIDENCE_KEYWORDS)) {
            subjects.add(LegalSubject.FraudulentEvidence)
        }
        
        // RULE C - Device / Account Access → Cybercrime
        if (matchesKeywords(combinedText, CYBERCRIME_KEYWORDS)) {
            subjects.add(LegalSubject.Cybercrime)
        }
        
        // RULE D - Trust / Duty Conflicts → BreachOfFiduciaryDuty
        if (matchesKeywords(combinedText, BREACH_OF_FIDUCIARY_DUTY_KEYWORDS)) {
            subjects.add(LegalSubject.BreachOfFiduciaryDuty)
        }
        
        // RULE E - Manipulation / Denial → EmotionalExploitation
        if (matchesKeywords(combinedText, EMOTIONAL_EXPLOITATION_KEYWORDS)) {
            subjects.add(LegalSubject.EmotionalExploitation)
        }
        
        return subjects
    }
    
    /**
     * Check if text matches any of the trigger keywords
     */
    private fun matchesKeywords(text: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> text.contains(keyword) }
    }
}
