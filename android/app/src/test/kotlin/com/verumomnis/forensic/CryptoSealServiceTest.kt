package com.verumomnis.forensic.crypto

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

/**
 * Unit tests for CryptoSealService
 * 
 * Tests cryptographic sealing functionality per constitution:
 * - SHA-512 hashing
 * - Seal creation
 * - Hash verification
 */
class CryptoSealServiceTest {

    @Test
    fun `hashString should produce consistent SHA-512 hash`() {
        val input = "test evidence data"
        val hash1 = CryptoSealService.hashString(input)
        val hash2 = CryptoSealService.hashString(input)
        
        assertEquals(hash1, hash2)
        assertEquals(128, hash1.length) // SHA-512 = 64 bytes = 128 hex chars
    }

    @Test
    fun `hashString should produce different hashes for different inputs`() {
        val hash1 = CryptoSealService.hashString("evidence1")
        val hash2 = CryptoSealService.hashString("evidence2")
        
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `hashBytes should produce valid SHA-512 hash`() {
        val data = "test data".toByteArray()
        val hash = CryptoSealService.hashBytes(data)
        
        assertEquals(128, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
    }

    @Test
    fun `hashStream should produce same result as hashBytes`() {
        val data = "test data for stream".toByteArray()
        val hashFromBytes = CryptoSealService.hashBytes(data)
        val hashFromStream = CryptoSealService.hashStream(ByteArrayInputStream(data))
        
        assertEquals(hashFromBytes, hashFromStream)
    }

    @Test
    fun `createSealHash should combine hashes with timestamp`() {
        val contentHash = CryptoSealService.hashString("content")
        val metadataHash = CryptoSealService.hashString("metadata")
        val timestamp = "2025-01-01T00:00:00Z"
        
        val sealHash = CryptoSealService.createSealHash(contentHash, metadataHash, timestamp)
        
        assertEquals(128, sealHash.length)
        assertNotEquals(contentHash, sealHash)
        assertNotEquals(metadataHash, sealHash)
    }

    @Test
    fun `createSealHash should be deterministic`() {
        val contentHash = CryptoSealService.hashString("content")
        val metadataHash = CryptoSealService.hashString("metadata")
        val timestamp = "2025-01-01T00:00:00Z"
        
        val seal1 = CryptoSealService.createSealHash(contentHash, metadataHash, timestamp)
        val seal2 = CryptoSealService.createSealHash(contentHash, metadataHash, timestamp)
        
        assertEquals(seal1, seal2)
    }

    @Test
    fun `verifyHashMatch should return true for matching hashes`() {
        val hash = CryptoSealService.hashString("test")
        
        assertTrue(CryptoSealService.verifyHashMatch(hash, hash))
    }

    @Test
    fun `verifyHashMatch should return false for different hashes`() {
        val hash1 = CryptoSealService.hashString("test1")
        val hash2 = CryptoSealService.hashString("test2")
        
        assertFalse(CryptoSealService.verifyHashMatch(hash1, hash2))
    }

    @Test
    fun `verifyHashMatch should return false for different length strings`() {
        assertFalse(CryptoSealService.verifyHashMatch("short", "verylongstring"))
    }

    @Test
    fun `generateSecureRandom should produce random bytes of correct length`() {
        val bytes16 = CryptoSealService.generateSecureRandom(16)
        val bytes32 = CryptoSealService.generateSecureRandom(32)
        
        assertEquals(16, bytes16.size)
        assertEquals(32, bytes32.size)
    }

    @Test
    fun `generateSecureRandom should produce different values each time`() {
        val random1 = CryptoSealService.generateSecureRandom(32)
        val random2 = CryptoSealService.generateSecureRandom(32)
        
        assertFalse(random1.contentEquals(random2))
    }

    @Test
    fun `createHmacSignature should produce valid HMAC-SHA512`() {
        val data = "test data".toByteArray()
        val key = "secret key".toByteArray()
        
        val signature = CryptoSealService.createHmacSignature(data, key)
        
        assertEquals(128, signature.length) // HMAC-SHA512 = 64 bytes = 128 hex chars
    }

    @Test
    fun `verifyHmacSignature should validate correct signature`() {
        val data = "test data".toByteArray()
        val key = "secret key".toByteArray()
        
        val signature = CryptoSealService.createHmacSignature(data, key)
        
        assertTrue(CryptoSealService.verifyHmacSignature(data, key, signature))
    }

    @Test
    fun `verifyHmacSignature should reject wrong signature`() {
        val data = "test data".toByteArray()
        val key = "secret key".toByteArray()
        val wrongSignature = CryptoSealService.createHmacSignature("wrong data".toByteArray(), key)
        
        assertFalse(CryptoSealService.verifyHmacSignature(data, key, wrongSignature))
    }

    @Test
    fun `verifyHmacSignature should reject wrong key`() {
        val data = "test data".toByteArray()
        val key = "secret key".toByteArray()
        val wrongKey = "wrong key".toByteArray()
        
        val signature = CryptoSealService.createHmacSignature(data, key)
        
        assertFalse(CryptoSealService.verifyHmacSignature(data, wrongKey, signature))
    }
}
