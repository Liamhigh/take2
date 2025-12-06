package org.verumomnis.engine

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

/**
 * LAYER 1 - NARRATIVE ENGINE
 * 
 * Normalizes raw text evidence into clean, indexed, timestamp-aware sentences.
 * This is a deterministic, rule-based normalization engine.
 */
class NarrativeEngine {
    
    private val rawText = StringBuilder()
    private val sentences = mutableListOf<Sentence>()
    
    companion object {
        // Date patterns for timestamp extraction
        private val DATE_PATTERNS = listOf(
            // YYYY-MM-DD
            Pattern.compile("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b"),
            // DD/MM/YYYY
            Pattern.compile("\\b(\\d{2})/(\\d{2})/(\\d{4})\\b"),
            // MM/DD/YYYY
            Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b")
        )
        
        // Time patterns HH:MM
        private val TIME_PATTERN = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b")
        
        // Sentence delimiters
        private val SENTENCE_DELIMITERS = "[.!?]+"
    }
    
    /**
     * Ingest raw text evidence
     */
    fun ingest(rawText: String) {
        this.rawText.clear()
        this.rawText.append(rawText)
    }
    
    /**
     * Tokenize raw text into structured sentences
     */
    fun tokenize(): List<Sentence> {
        sentences.clear()
        
        // Split by sentence delimiters
        val rawSentences = rawText.toString()
            .split(Regex(SENTENCE_DELIMITERS))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        // Create Sentence objects with index and optional timestamp
        rawSentences.forEachIndexed { index, text ->
            val timestamp = extractTimestamp(text)
            sentences.add(Sentence(text, index, timestamp))
        }
        
        return sentences.toList()
    }
    
    /**
     * Extract timestamp from sentence text (optional)
     * Returns milliseconds since epoch, or null if no timestamp found
     */
    fun extractTimestamp(sentence: String): Long? {
        // Try to find date pattern
        for (pattern in DATE_PATTERNS) {
            val matcher = pattern.matcher(sentence)
            if (matcher.find()) {
                try {
                    // Try various date formats
                    val dateStr = matcher.group()
                    val formats = listOf(
                        SimpleDateFormat("yyyy-MM-dd", Locale.US),
                        SimpleDateFormat("dd/MM/yyyy", Locale.US),
                        SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    )
                    
                    for (format in formats) {
                        try {
                            val date = format.parse(dateStr)
                            if (date != null) {
                                return date.time
                            }
                        } catch (e: Exception) {
                            // Try next format
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next pattern
                }
            }
        }
        
        return null
    }
    
    /**
     * Normalize the narrative (currently returns tokenized sentences)
     * Can be extended for additional normalization rules
     */
    fun normalize(): List<Sentence> {
        return tokenize()
    }
    
    /**
     * Get the current sentence list
     */
    fun getSentences(): List<Sentence> {
        return sentences.toList()
    }
}
