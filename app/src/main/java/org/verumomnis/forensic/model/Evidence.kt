package org.verumomnis.forensic.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a piece of evidence in a forensic case
 */
data class Evidence(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "text", "file", "image", "pdf"
    val summary: String,
    val content: String = "",
    val filePath: String = "",
    val timestamp: Instant = Instant.now()
)
