package org.verumomnis.forensic.integrity

import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import java.security.MessageDigest

/**
 * Chain of Trust implementation for forensic evidence.
 * 
 * Creates a cryptographic chain linking the APK integrity hash to all processed documents.
 * This establishes provenance and non-repudiation for court admissibility.
 * 
 * Per verum-constitution.json:
 * - hash_standard: SHA-512
 * - admissibility_standard: legal-grade, contradiction-free, complete evidence mapping
 */
class ChainOfTrust {
    
    private val sealingEngine = CryptographicSealingEngine()
    
    /**
     * Creates a complete chain of trust starting with APK hash as root.
     * 
     * @param caseId The forensic case identifier
     * @param documents List of processed documents to include in chain
     * @return TrustChain containing the complete chain with verification data
     */
    fun createChain(caseId: String, documents: List<ProcessedDocument>): TrustChain {
        // Start with APK hash as root of trust
        val chain = mutableListOf(
            ChainLink(
                id = "ROOT",
                type = ChainType.APK_INTEGRITY,
                hash = APKIntegrityChecker.EXPECTED_APK_HASH,
                previousHash = "0".repeat(64),
                timestamp = System.currentTimeMillis(),
                description = "Verum Omnis APK Root Hash (SHA-256)"
            )
        )
        
        // Add each document as a link in the chain
        documents.forEachIndexed { index, doc ->
            val previousHash = chain.last().hash
            val documentHash = sealingEngine.computeHash(doc.bytes)
            
            chain.add(
                ChainLink(
                    id = "DOC_${index + 1}",
                    type = ChainType.DOCUMENT_EVIDENCE,
                    hash = documentHash,
                    previousHash = previousHash,
                    timestamp = doc.timestamp,
                    description = "${doc.type}: ${doc.name}"
                )
            )
        }
        
        // Calculate final chain hash
        val finalHash = calculateChainFinalHash(chain)
        
        return TrustChain(
            caseId = caseId,
            apkHash = APKIntegrityChecker.EXPECTED_APK_HASH,
            chain = chain,
            finalHash = finalHash,
            verificationCommand = generateVerificationCommand(chain),
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Calculates the final hash of the entire chain.
     * This provides a single value that can verify the entire chain integrity.
     */
    private fun calculateChainFinalHash(chain: List<ChainLink>): String {
        val chainData = chain.joinToString("|") { 
            "${it.id}:${it.hash}:${it.previousHash}:${it.timestamp}" 
        }
        return sealingEngine.computeHash(chainData)
    }
    
    /**
     * Generates shell commands for independent verification.
     */
    private fun generateVerificationCommand(chain: List<ChainLink>): String {
        return buildString {
            appendLine("# Independent Chain Verification Commands")
            appendLine()
            appendLine("# 1. Verify APK integrity")
            appendLine("sha256sum verum-omnis-forensic-engine.apk")
            appendLine("# Expected: ${APKIntegrityChecker.EXPECTED_APK_HASH}")
            appendLine()
            
            chain.filter { it.type == ChainType.DOCUMENT_EVIDENCE }.forEach { link ->
                appendLine("# ${link.id}: ${link.description}")
                appendLine("# Hash: ${link.hash}")
                appendLine("# Previous: ${link.previousHash.take(16)}...")
                appendLine()
            }
        }
    }
    
    /**
     * Verifies an existing chain for integrity.
     * 
     * @param chain The TrustChain to verify
     * @return True if the chain is valid, false if tampered
     */
    fun verifyChain(chain: TrustChain): Boolean {
        if (chain.chain.isEmpty()) return false
        
        // Verify root is APK hash
        val root = chain.chain.first()
        if (root.type != ChainType.APK_INTEGRITY) return false
        if (root.hash != APKIntegrityChecker.EXPECTED_APK_HASH) return false
        
        // Verify each link connects to previous
        for (i in 1 until chain.chain.size) {
            val current = chain.chain[i]
            val previous = chain.chain[i - 1]
            
            if (current.previousHash != previous.hash) {
                return false
            }
        }
        
        // Verify final hash
        val expectedFinalHash = calculateChainFinalHash(chain.chain)
        return expectedFinalHash == chain.finalHash
    }
}

/**
 * Represents a single link in the chain of trust.
 */
data class ChainLink(
    /** Unique identifier for this link (e.g., "ROOT", "DOC_1") */
    val id: String,
    
    /** Type of chain link */
    val type: ChainType,
    
    /** SHA-512 hash of the content (SHA-256 for APK) */
    val hash: String,
    
    /** Hash of the previous link in the chain */
    val previousHash: String,
    
    /** Unix timestamp when this link was created */
    val timestamp: Long,
    
    /** Human-readable description of the content */
    val description: String
) {
    /**
     * Converts link to map for serialization.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "type" to type.name,
        "hash" to hash,
        "previous_hash" to previousHash,
        "timestamp" to timestamp,
        "description" to description
    )
}

/**
 * Types of chain links.
 */
enum class ChainType {
    /** Root link - the APK integrity hash */
    APK_INTEGRITY,
    
    /** Document evidence link */
    DOCUMENT_EVIDENCE,
    
    /** Photo evidence link */
    PHOTO_EVIDENCE,
    
    /** Final report link */
    FINAL_REPORT
}

/**
 * Complete chain of trust with verification data.
 */
data class TrustChain(
    /** Case identifier */
    val caseId: String,
    
    /** APK hash (root of trust) */
    val apkHash: String,
    
    /** List of chain links from root to latest */
    val chain: List<ChainLink>,
    
    /** Final hash representing entire chain integrity */
    val finalHash: String,
    
    /** Shell commands for independent verification */
    val verificationCommand: String,
    
    /** Unix timestamp when chain was created */
    val createdAt: Long
) {
    /**
     * Converts chain to map for serialization.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "case_id" to caseId,
        "apk_hash" to apkHash,
        "chain" to chain.map { it.toMap() },
        "final_hash" to finalHash,
        "created_at" to createdAt
    )
    
    /**
     * Generates a summary for display.
     */
    fun getSummary(): String = buildString {
        appendLine("Chain of Trust Summary")
        appendLine("=".repeat(40))
        appendLine("Case ID: $caseId")
        appendLine("APK Root: ${apkHash.take(16)}...")
        appendLine("Links: ${chain.size}")
        appendLine("Final Hash: ${finalHash.take(32)}...")
        appendLine()
        appendLine("Chain Structure:")
        chain.forEach { link ->
            appendLine("  ${link.id}: ${link.type}")
            appendLine("    Hash: ${link.hash.take(32)}...")
            appendLine("    Description: ${link.description}")
        }
    }
}

/**
 * Represents a processed document for chain inclusion.
 */
data class ProcessedDocument(
    /** Document name */
    val name: String,
    
    /** Document type (e.g., "PDF", "Image", "Text") */
    val type: String,
    
    /** Raw bytes of the document */
    val bytes: ByteArray,
    
    /** Processing timestamp */
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedDocument

        if (name != other.name) return false
        if (type != other.type) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
