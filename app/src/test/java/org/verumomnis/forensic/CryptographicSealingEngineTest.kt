package org.verumomnis.forensic

import org.junit.Test
import org.junit.Assert.*
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import java.time.Instant

/**
 * Unit tests for the Cryptographic Sealing Engine
 */
class CryptographicSealingEngineTest {
    
    @Test
    fun testHashGeneration() {
        val data = "Test document content"
        val hash1 = CryptographicSealingEngine.generateHash(data)
        val hash2 = CryptographicSealingEngine.generateHash(data)
        
        // Same input should produce same hash
        assertEquals(hash1, hash2)
        
        // Hash should be 128 characters (SHA-512 in hex)
        assertEquals(128, hash1.length)
    }
    
    @Test
    fun testDifferentInputsProduceDifferentHashes() {
        val hash1 = CryptographicSealingEngine.generateHash("Document A")
        val hash2 = CryptographicSealingEngine.generateHash("Document B")
        
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun testSealCreation() {
        val documentData = "Test forensic document".toByteArray()
        val timestamp = Instant.now()
        val locationHash = CryptographicSealingEngine.generateHash("0.0,0.0,UTC")
        val sessionId = CryptographicSealingEngine.generateSessionId()
        
        val seal = CryptographicSealingEngine.createSeal(
            documentData = documentData,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        // Verify seal properties
        assertNotNull(seal.documentHash)
        assertNotNull(seal.signature)
        assertNotNull(seal.verificationCode)
        assertEquals(timestamp, seal.timestamp)
        assertEquals(locationHash, seal.locationHash)
        assertEquals(sessionId, seal.sessionId)
        assertEquals("SHA-512", seal.algorithm)
    }
    
    @Test
    fun testSealVerification() {
        val documentData = "Test forensic document".toByteArray()
        val timestamp = Instant.now()
        val locationHash = CryptographicSealingEngine.generateHash("0.0,0.0,UTC")
        val sessionId = CryptographicSealingEngine.generateSessionId()
        
        val seal = CryptographicSealingEngine.createSeal(
            documentData = documentData,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        // Verify seal with original data
        val result = CryptographicSealingEngine.verifySeal(seal, documentData)
        
        assertTrue(result.isValid)
        assertNull(result.failureReason)
    }
    
    @Test
    fun testTamperedDocumentDetection() {
        val originalData = "Original document content".toByteArray()
        val tamperedData = "Tampered document content".toByteArray()
        val timestamp = Instant.now()
        val locationHash = CryptographicSealingEngine.generateHash("0.0,0.0,UTC")
        val sessionId = CryptographicSealingEngine.generateSessionId()
        
        // Create seal with original data
        val seal = CryptographicSealingEngine.createSeal(
            documentData = originalData,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        // Verify seal with tampered data should fail
        val result = CryptographicSealingEngine.verifySeal(seal, tamperedData)
        
        assertFalse(result.isValid)
        assertNotNull(result.failureReason)
        assertTrue(result.failureReason!!.contains("tampered"))
    }
    
    @Test
    fun testSessionIdGeneration() {
        val sessionId1 = CryptographicSealingEngine.generateSessionId()
        val sessionId2 = CryptographicSealingEngine.generateSessionId()
        
        // Session IDs should be unique
        assertNotEquals(sessionId1, sessionId2)
        
        // Session ID should start with VO-
        assertTrue(sessionId1.startsWith("VO-"))
        assertTrue(sessionId2.startsWith("VO-"))
    }
    
    @Test
    fun testEvidenceIdGeneration() {
        val evidenceId1 = CryptographicSealingEngine.generateEvidenceId()
        val evidenceId2 = CryptographicSealingEngine.generateEvidenceId()
        
        // Evidence IDs should be unique
        assertNotEquals(evidenceId1, evidenceId2)
        
        // Evidence ID should start with EV-
        assertTrue(evidenceId1.startsWith("EV-"))
        assertTrue(evidenceId2.startsWith("EV-"))
    }
    
    @Test
    fun testQrCodeContent() {
        val documentData = "Test document".toByteArray()
        val timestamp = Instant.now()
        val locationHash = CryptographicSealingEngine.generateHash("0.0,0.0,UTC")
        val sessionId = CryptographicSealingEngine.generateSessionId()
        
        val seal = CryptographicSealingEngine.createSeal(
            documentData = documentData,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        val qrContent = seal.getQrCodeContent()
        
        // QR content should contain VERUM prefix
        assertTrue(qrContent.startsWith("VERUM:"))
        
        // QR content should contain verification code
        assertTrue(qrContent.contains(seal.verificationCode))
        
        // QR content should contain session ID
        assertTrue(qrContent.contains(seal.sessionId))
    }
    
    @Test
    fun testSealSummary() {
        val documentData = "Test document".toByteArray()
        val timestamp = Instant.now()
        val locationHash = CryptographicSealingEngine.generateHash("0.0,0.0,UTC")
        val sessionId = CryptographicSealingEngine.generateSessionId()
        
        val seal = CryptographicSealingEngine.createSeal(
            documentData = documentData,
            timestamp = timestamp,
            locationHash = locationHash,
            sessionId = sessionId
        )
        
        val summary = seal.getSealSummary()
        
        // Summary should contain key information
        assertTrue(summary.contains("VERUM OMNIS"))
        assertTrue(summary.contains("SHA-512"))
        assertTrue(summary.contains(seal.sessionId))
        assertTrue(summary.contains(seal.verificationCode))
    }
}
