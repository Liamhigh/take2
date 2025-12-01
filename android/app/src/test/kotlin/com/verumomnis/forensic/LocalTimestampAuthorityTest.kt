package com.verumomnis.forensic.crypto

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for LocalTimestampAuthority
 */
class LocalTimestampAuthorityTest {

    @Test
    fun `createTimestamp should return valid proof`() {
        val proof = LocalTimestampAuthority.createTimestamp()
        
        assertNotNull(proof.timestamp)
        assertNotNull(proof.nonce)
        assertNotNull(proof.proof)
        assertTrue(proof.nonce.isNotEmpty())
        assertEquals(128, proof.proof.length) // SHA-512 hex length
    }

    @Test
    fun `createTimestamp should produce unique proofs`() {
        val proof1 = LocalTimestampAuthority.createTimestamp()
        val proof2 = LocalTimestampAuthority.createTimestamp()
        
        assertNotEquals(proof1.nonce, proof2.nonce)
        assertNotEquals(proof1.proof, proof2.proof)
    }

    @Test
    fun `verifyTimestamp should validate correct proof`() {
        val proof = LocalTimestampAuthority.createTimestamp()
        
        assertTrue(LocalTimestampAuthority.verifyTimestamp(proof))
    }

    @Test
    fun `verifyTimestamp should reject tampered proof`() {
        val proof = LocalTimestampAuthority.createTimestamp()
        val tamperedProof = proof.copy(nonce = "tampered_nonce")
        
        assertFalse(LocalTimestampAuthority.verifyTimestamp(tamperedProof))
    }

    @Test
    fun `verifyTimestamp should reject modified timestamp`() {
        val proof = LocalTimestampAuthority.createTimestamp()
        val tamperedProof = proof.copy(timestamp = Instant.now().plusSeconds(1000))
        
        assertFalse(LocalTimestampAuthority.verifyTimestamp(tamperedProof))
    }
}
