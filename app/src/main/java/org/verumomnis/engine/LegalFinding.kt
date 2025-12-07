package org.verumomnis.engine

/**
 * Represents a legal finding with classified contradictions
 * 
 * @param subject The legal category
 * @param contradictions List of contradictions that support this finding
 */
data class LegalFinding(
    val subject: LegalSubject,
    val contradictions: List<ContradictionResult>
)
