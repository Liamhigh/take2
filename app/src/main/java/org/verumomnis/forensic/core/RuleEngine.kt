package org.verumomnis.forensic.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader

/**
 * Rule Engine for Verum Omnis Forensic Analysis
 *
 * Implements the forensic analysis rules from verum-constitution.json:
 * - Contradiction detection
 * - Legal subject tagging
 * - Dishonesty matrix scoring
 * - Evidence extraction protocol
 *
 * This engine is stateless and offline-first per security requirements.
 */
class RuleEngine(private val context: Context) {

    companion object {
        private const val RULES_DIR = "rules"
        private const val VERUM_RULES_FILE = "verum_rules.json"
        private const val DISHONESTY_MATRIX_FILE = "dishonesty_matrix.json"
        private const val LEGAL_SUBJECTS_FILE = "legal_subjects.json"
        private const val EXTRACTION_PROTOCOL_FILE = "extraction_protocol.json"
    }

    private var verumRules: VerumRules? = null
    private var dishonestyMatrix: DishonestyMatrix? = null
    private var legalSubjects: LegalSubjects? = null
    private var extractionProtocol: ExtractionProtocol? = null

    /**
     * Loads all rule files from assets
     */
    fun loadRules(): Boolean {
        return try {
            verumRules = loadVerumRules()
            dishonestyMatrix = loadDishonestyMatrix()
            legalSubjects = loadLegalSubjects()
            extractionProtocol = loadExtractionProtocol()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Analyzes text against all loaded rules
     */
    fun analyzeText(text: String): RuleAnalysisResult {
        val normalizedText = text.lowercase()

        // 1. Scan for keywords
        val keywordMatches = scanForKeywords(normalizedText)

        // 2. Tag legal subjects
        val legalSubjectMatches = tagLegalSubjects(normalizedText)

        // 3. Detect red flags (dishonesty patterns)
        val redFlags = detectRedFlags(normalizedText)

        // 4. Calculate overall score
        val score = calculateScore(keywordMatches, legalSubjectMatches, redFlags)

        // 5. Determine severity
        val severity = determineSeverity(score, legalSubjectMatches)

        // 6. Generate tags
        val tags = generateTags(keywordMatches, legalSubjectMatches)

        return RuleAnalysisResult(
            keywordMatches = keywordMatches,
            legalSubjects = legalSubjectMatches,
            redFlags = redFlags,
            score = score,
            severity = severity,
            tags = tags
        )
    }

    /**
     * Scans text for keywords defined in extraction protocol
     */
    fun scanForKeywords(text: String): List<KeywordMatch> {
        val matches = mutableListOf<KeywordMatch>()
        val protocol = extractionProtocol ?: return matches

        protocol.keywords.forEach { (category, keywords) ->
            keywords.forEach { keyword ->
                if (text.contains(keyword.lowercase())) {
                    val position = text.indexOf(keyword.lowercase())
                    val context = extractContext(text, position, keyword.length)
                    matches.add(
                        KeywordMatch(
                            keyword = keyword,
                            category = category,
                            position = position,
                            context = context
                        )
                    )
                }
            }
        }

        return matches
    }

    /**
     * Tags text with relevant legal subjects
     */
    fun tagLegalSubjects(text: String): List<LegalSubjectMatch> {
        val matches = mutableListOf<LegalSubjectMatch>()
        val subjects = legalSubjects ?: return matches

        subjects.categories.forEach { (category, categoryData) ->
            categoryData.subjects.forEach { subject ->
                val matchingKeywords = subject.keywords.filter { keyword ->
                    text.contains(keyword.lowercase())
                }

                if (matchingKeywords.isNotEmpty()) {
                    matches.add(
                        LegalSubjectMatch(
                            id = subject.id,
                            name = subject.name,
                            category = category,
                            severity = subject.severity,
                            matchingKeywords = matchingKeywords,
                            matchCount = matchingKeywords.size
                        )
                    )
                }
            }
        }

        return matches.sortedByDescending { it.matchCount }
    }

    /**
     * Detects dishonesty red flags in text
     */
    fun detectRedFlags(text: String): List<RedFlag> {
        val flags = mutableListOf<RedFlag>()
        val matrix = dishonestyMatrix ?: return flags

        matrix.categories.forEach { (category, categoryData) ->
            categoryData.patterns.forEach { pattern ->
                val regex = try {
                    Regex(pattern.regex, RegexOption.IGNORE_CASE)
                } catch (_: Exception) {
                    null
                }

                if (regex != null) {
                    val matchResult = regex.find(text)
                    if (matchResult != null) {
                        flags.add(
                            RedFlag(
                                id = pattern.id,
                                category = category,
                                description = pattern.description,
                                weight = categoryData.weight,
                                matchedText = matchResult.value,
                                position = matchResult.range.first
                            )
                        )
                    }
                }
            }
        }

        return flags.sortedByDescending { it.weight }
    }

    /**
     * Calculates overall analysis score
     */
    private fun calculateScore(
        keywords: List<KeywordMatch>,
        legalSubjects: List<LegalSubjectMatch>,
        redFlags: List<RedFlag>
    ): Int {
        var score = 0

        // Keyword matches contribute to score
        score += keywords.size

        // Legal subject matches weighted by severity
        legalSubjects.forEach { subject ->
            score += when (subject.severity) {
                "CRITICAL" -> 4 * subject.matchCount
                "HIGH" -> 3 * subject.matchCount
                "MEDIUM" -> 2 * subject.matchCount
                else -> subject.matchCount
            }
        }

        // Red flags weighted by their weight
        redFlags.forEach { flag ->
            score += flag.weight
        }

        return minOf(score, 100)
    }

    /**
     * Determines overall severity based on analysis
     */
    private fun determineSeverity(score: Int, legalSubjects: List<LegalSubjectMatch>): String {
        // Check for critical legal subjects
        if (legalSubjects.any { it.severity == "CRITICAL" }) {
            return "CRITICAL"
        }

        // Check for multiple high severity subjects
        if (legalSubjects.count { it.severity == "HIGH" } >= 2) {
            return "HIGH"
        }

        // Score-based severity
        return when {
            score >= 50 -> "CRITICAL"
            score >= 30 -> "HIGH"
            score >= 15 -> "MEDIUM"
            score >= 5 -> "LOW"
            else -> "MINIMAL"
        }
    }

    /**
     * Generates forensic tags based on analysis
     */
    private fun generateTags(
        keywords: List<KeywordMatch>,
        legalSubjects: List<LegalSubjectMatch>
    ): List<String> {
        val tags = mutableSetOf<String>()
        val protocol = extractionProtocol ?: return tags.toList()

        // Add tags based on keyword categories
        keywords.map { it.category }.distinct().forEach { category ->
            when (category) {
                "administrative" -> tags.add("#Access")
                "actions" -> tags.add("#ActionRequired")
                "documents" -> tags.add("#DocumentEvidence")
                "financial" -> tags.add("#Financial")
                "corporate" -> tags.add("#Corporate")
            }
        }

        // Add tags based on legal subjects
        legalSubjects.forEach { subject ->
            tags.add("#${subject.id.replaceFirstChar { it.uppercase() }}")
        }

        return tags.toList().sorted()
    }

    /**
     * Extracts context around a match
     */
    private fun extractContext(text: String, position: Int, length: Int): String {
        val contextSize = 100
        val start = maxOf(0, position - contextSize)
        val end = minOf(text.length, position + length + contextSize)
        return text.substring(start, end).trim()
    }

    // Asset loading methods

    private fun loadVerumRules(): VerumRules {
        val json = loadAssetFile("$RULES_DIR/$VERUM_RULES_FILE")
        val obj = JSONObject(json)

        val legalSubjectsList = mutableListOf<LegalSubjectRule>()
        val subjectsArray = obj.getJSONArray("legal_subjects")
        for (i in 0 until subjectsArray.length()) {
            val subjectObj = subjectsArray.getJSONObject(i)
            val keywords = mutableListOf<String>()
            val keywordsArray = subjectObj.getJSONArray("keywords")
            for (j in 0 until keywordsArray.length()) {
                keywords.add(keywordsArray.getString(j))
            }
            legalSubjectsList.add(
                LegalSubjectRule(
                    name = subjectObj.getString("name"),
                    keywords = keywords,
                    severity = subjectObj.getString("severity"),
                    category = subjectObj.getString("category")
                )
            )
        }

        return VerumRules(
            version = obj.getString("version"),
            legalSubjects = legalSubjectsList
        )
    }

    private fun loadDishonestyMatrix(): DishonestyMatrix {
        val json = loadAssetFile("$RULES_DIR/$DISHONESTY_MATRIX_FILE")
        val obj = JSONObject(json)

        val categories = mutableMapOf<String, DishonestyCategory>()

        listOf("contradictions", "omissions", "fabrications", "deflections").forEach { categoryName ->
            if (obj.has(categoryName)) {
                val catObj = obj.getJSONObject(categoryName)
                val patterns = mutableListOf<DishonestyPattern>()

                val patternsArray = catObj.getJSONArray("patterns")
                for (i in 0 until patternsArray.length()) {
                    val patternObj = patternsArray.getJSONObject(i)
                    patterns.add(
                        DishonestyPattern(
                            id = patternObj.getString("id"),
                            regex = patternObj.getString("regex"),
                            description = patternObj.getString("description")
                        )
                    )
                }

                categories[categoryName] = DishonestyCategory(
                    weight = catObj.getInt("weight"),
                    patterns = patterns
                )
            }
        }

        return DishonestyMatrix(categories = categories)
    }

    private fun loadLegalSubjects(): LegalSubjects {
        val json = loadAssetFile("$RULES_DIR/$LEGAL_SUBJECTS_FILE")
        val obj = JSONObject(json)
        val categoriesObj = obj.getJSONObject("categories")

        val categories = mutableMapOf<String, LegalCategory>()

        categoriesObj.keys().forEach { categoryName ->
            val catObj = categoriesObj.getJSONObject(categoryName)
            val subjects = mutableListOf<LegalSubject>()

            val subjectsArray = catObj.getJSONArray("subjects")
            for (i in 0 until subjectsArray.length()) {
                val subjectObj = subjectsArray.getJSONObject(i)
                val keywords = mutableListOf<String>()
                val keywordsArray = subjectObj.getJSONArray("keywords")
                for (j in 0 until keywordsArray.length()) {
                    keywords.add(keywordsArray.getString(j))
                }
                subjects.add(
                    LegalSubject(
                        id = subjectObj.getString("id"),
                        name = subjectObj.getString("name"),
                        severity = subjectObj.getString("severity"),
                        keywords = keywords
                    )
                )
            }

            categories[categoryName] = LegalCategory(
                description = catObj.getString("description"),
                subjects = subjects
            )
        }

        return LegalSubjects(categories = categories)
    }

    private fun loadExtractionProtocol(): ExtractionProtocol {
        val json = loadAssetFile("$RULES_DIR/$EXTRACTION_PROTOCOL_FILE")
        val obj = JSONObject(json)

        val keywordsObj = obj.getJSONObject("step1_keywords")
        val keywords = mutableMapOf<String, List<String>>()

        keywordsObj.keys().forEach { category ->
            if (category != "description") {
                val keywordArray = keywordsObj.getJSONArray(category)
                val keywordList = mutableListOf<String>()
                for (i in 0 until keywordArray.length()) {
                    keywordList.add(keywordArray.getString(i))
                }
                keywords[category] = keywordList
            }
        }

        val tagsObj = obj.getJSONObject("step2_tags")
        val tags = mutableMapOf<String, List<String>>()

        tagsObj.keys().forEach { category ->
            if (category != "description") {
                val tagArray = tagsObj.getJSONArray(category)
                val tagList = mutableListOf<String>()
                for (i in 0 until tagArray.length()) {
                    tagList.add(tagArray.getString(i))
                }
                tags[category] = tagList
            }
        }

        return ExtractionProtocol(
            keywords = keywords,
            tags = tags
        )
    }

    private fun loadAssetFile(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use(BufferedReader::readText)
    }
}

// Data classes for rule structures

data class VerumRules(
    val version: String,
    val legalSubjects: List<LegalSubjectRule>
)

data class LegalSubjectRule(
    val name: String,
    val keywords: List<String>,
    val severity: String,
    val category: String
)

data class DishonestyMatrix(
    val categories: Map<String, DishonestyCategory>
)

data class DishonestyCategory(
    val weight: Int,
    val patterns: List<DishonestyPattern>
)

data class DishonestyPattern(
    val id: String,
    val regex: String,
    val description: String
)

data class LegalSubjects(
    val categories: Map<String, LegalCategory>
)

data class LegalCategory(
    val description: String,
    val subjects: List<LegalSubject>
)

data class LegalSubject(
    val id: String,
    val name: String,
    val severity: String,
    val keywords: List<String>
)

data class ExtractionProtocol(
    val keywords: Map<String, List<String>>,
    val tags: Map<String, List<String>>
)

// Analysis result classes

data class RuleAnalysisResult(
    val keywordMatches: List<KeywordMatch>,
    val legalSubjects: List<LegalSubjectMatch>,
    val redFlags: List<RedFlag>,
    val score: Int,
    val severity: String,
    val tags: List<String>
) {
    fun toSummary(): String = buildString {
        appendLine("Analysis Score: $score")
        appendLine("Severity: $severity")
        appendLine("Keywords Found: ${keywordMatches.size}")
        appendLine("Legal Subjects: ${legalSubjects.size}")
        appendLine("Red Flags: ${redFlags.size}")
        if (tags.isNotEmpty()) {
            appendLine("Tags: ${tags.joinToString(", ")}")
        }
    }
}

data class KeywordMatch(
    val keyword: String,
    val category: String,
    val position: Int,
    val context: String
)

data class LegalSubjectMatch(
    val id: String,
    val name: String,
    val category: String,
    val severity: String,
    val matchingKeywords: List<String>,
    val matchCount: Int
)

data class RedFlag(
    val id: String,
    val category: String,
    val description: String,
    val weight: Int,
    val matchedText: String,
    val position: Int
)
