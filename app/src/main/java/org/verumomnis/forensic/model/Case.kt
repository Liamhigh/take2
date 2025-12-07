package org.verumomnis.forensic.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a forensic case containing evidence items
 */
data class Case(
    val caseId: String,
    val caseName: String,
    val createdAt: Instant,
    val evidence: MutableList<Evidence> = mutableListOf()
) {
    companion object {
        fun create(name: String): Case {
            return Case(
                caseId = UUID.randomUUID().toString(),
                caseName = name,
                createdAt = Instant.now()
            )
        }
    }
}
