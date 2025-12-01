package org.verumomnis.forensic.integrity

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ChainOfTrust class
 */
class ChainOfTrustTest {
    
    private lateinit var chainOfTrust: ChainOfTrust
    
    @Before
    fun setUp() {
        chainOfTrust = ChainOfTrust()
    }
    
    @Test
    fun `createChain returns chain with APK root`() {
        val documents = listOf(
            ProcessedDocument(
                name = "evidence1.pdf",
                type = "PDF",
                bytes = "Test document content".toByteArray(),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val chain = chainOfTrust.createChain("CASE-001", documents)
        
        // Verify root link
        assertEquals(2, chain.chain.size)
        assertEquals("ROOT", chain.chain.first().id)
        assertEquals(ChainType.APK_INTEGRITY, chain.chain.first().type)
        assertEquals(APKIntegrityChecker.EXPECTED_APK_HASH, chain.chain.first().hash)
    }
    
    @Test
    fun `createChain links documents correctly`() {
        val documents = listOf(
            ProcessedDocument(
                name = "doc1.pdf",
                type = "PDF",
                bytes = "Document 1".toByteArray(),
                timestamp = System.currentTimeMillis()
            ),
            ProcessedDocument(
                name = "doc2.pdf",
                type = "PDF",
                bytes = "Document 2".toByteArray(),
                timestamp = System.currentTimeMillis() + 1000
            )
        )
        
        val chain = chainOfTrust.createChain("CASE-002", documents)
        
        // Verify chain structure
        assertEquals(3, chain.chain.size) // ROOT + 2 documents
        
        // Verify each link connects to previous
        for (i in 1 until chain.chain.size) {
            assertEquals(
                "Link $i should reference previous link's hash",
                chain.chain[i - 1].hash,
                chain.chain[i].previousHash
            )
        }
    }
    
    @Test
    fun `createChain with empty documents returns only root`() {
        val chain = chainOfTrust.createChain("CASE-003", emptyList())
        
        assertEquals(1, chain.chain.size)
        assertEquals("ROOT", chain.chain.first().id)
        assertEquals(ChainType.APK_INTEGRITY, chain.chain.first().type)
    }
    
    @Test
    fun `verifyChain returns true for valid chain`() {
        val documents = listOf(
            ProcessedDocument(
                name = "test.pdf",
                type = "PDF",
                bytes = "Test content".toByteArray(),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val chain = chainOfTrust.createChain("CASE-004", documents)
        
        assertTrue(chainOfTrust.verifyChain(chain))
    }
    
    @Test
    fun `verifyChain returns false for empty chain`() {
        val emptyChain = TrustChain(
            caseId = "CASE-005",
            apkHash = APKIntegrityChecker.EXPECTED_APK_HASH,
            chain = emptyList(),
            finalHash = "",
            verificationCommand = "",
            createdAt = System.currentTimeMillis()
        )
        
        assertFalse(chainOfTrust.verifyChain(emptyChain))
    }
    
    @Test
    fun `verifyChain returns false for tampered chain`() {
        val documents = listOf(
            ProcessedDocument(
                name = "test.pdf",
                type = "PDF",
                bytes = "Test content".toByteArray(),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val validChain = chainOfTrust.createChain("CASE-006", documents)
        
        // Create tampered chain with wrong final hash
        val tamperedChain = validChain.copy(finalHash = "tampered_hash")
        
        assertFalse(chainOfTrust.verifyChain(tamperedChain))
    }
    
    @Test
    fun `chain contains verification command`() {
        val chain = chainOfTrust.createChain("CASE-007", emptyList())
        
        assertTrue(chain.verificationCommand.contains("sha256sum"))
        assertTrue(chain.verificationCommand.contains(APKIntegrityChecker.EXPECTED_APK_HASH))
    }
    
    @Test
    fun `chain summary contains essential information`() {
        val documents = listOf(
            ProcessedDocument(
                name = "evidence.pdf",
                type = "PDF",
                bytes = "Evidence data".toByteArray(),
                timestamp = System.currentTimeMillis()
            )
        )
        
        val chain = chainOfTrust.createChain("CASE-008", documents)
        val summary = chain.getSummary()
        
        assertTrue(summary.contains("CASE-008"))
        assertTrue(summary.contains("Chain of Trust Summary"))
        assertTrue(summary.contains("APK_INTEGRITY"))
    }
    
    @Test
    fun `chain toMap produces valid structure`() {
        val chain = chainOfTrust.createChain("CASE-009", emptyList())
        val map = chain.toMap()
        
        assertTrue(map.containsKey("case_id"))
        assertTrue(map.containsKey("apk_hash"))
        assertTrue(map.containsKey("chain"))
        assertTrue(map.containsKey("final_hash"))
        assertTrue(map.containsKey("created_at"))
    }
    
    @Test
    fun `ChainLink toMap produces valid structure`() {
        val link = ChainLink(
            id = "TEST",
            type = ChainType.DOCUMENT_EVIDENCE,
            hash = "abc123",
            previousHash = "000000",
            timestamp = System.currentTimeMillis(),
            description = "Test document"
        )
        
        val map = link.toMap()
        
        assertTrue(map.containsKey("id"))
        assertTrue(map.containsKey("type"))
        assertTrue(map.containsKey("hash"))
        assertTrue(map.containsKey("previous_hash"))
        assertTrue(map.containsKey("timestamp"))
        assertTrue(map.containsKey("description"))
    }
}
