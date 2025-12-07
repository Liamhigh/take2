package org.verumomnis.forensic.model

/**
 * Represents a detected contradiction between two statements
 */
data class Contradiction(
    val statementA: String,
    val statementB: String,
    val reason: String
)
