package org.verumomnis.engine

/**
 * Represents a normalized sentence from evidence
 * 
 * @param text The sentence text
 * @param index The sentence order/index
 * @param timestamp Optional timestamp extracted from the sentence (milliseconds since epoch)
 */
data class Sentence(
    val text: String, 
    val index: Int, 
    val timestamp: Long? = null
)
