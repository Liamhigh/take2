package com.verumomnis.forensic.core

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for Forensic Models
 * 
 * Tests data model integrity per constitution
 */
class ForensicModelsTest {

    @Test
    fun `ForensicSeal should use SHA-512 by default`() {
        val seal = ForensicSeal(
            contentHash = "test_content_hash",
            metadataHash = "test_metadata_hash",
            combinedSealHash = "test_combined_hash",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test_fingerprint"
            )
        )
        
        assertEquals("SHA-512", seal.hashAlgorithm)
    }

    @Test
    fun `ForensicSeal should have constitution version 1_0`() {
        val seal = ForensicSeal(
            contentHash = "test_content_hash",
            metadataHash = "test_metadata_hash",
            combinedSealHash = "test_combined_hash",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test_fingerprint"
            )
        )
        
        assertEquals("1.0", seal.constitutionVersion)
    }

    @Test
    fun `ForensicSeal verifySealIntegrity should pass for valid seal`() {
        val seal = ForensicSeal(
            hashAlgorithm = "SHA-512",
            contentHash = "valid_content_hash",
            metadataHash = "valid_metadata_hash",
            combinedSealHash = "valid_combined_hash",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test_fingerprint"
            )
        )
        
        assertTrue(seal.verifySealIntegrity())
    }

    @Test
    fun `ForensicSeal verifySealIntegrity should fail for wrong algorithm`() {
        val seal = ForensicSeal(
            hashAlgorithm = "MD5",
            contentHash = "valid_content_hash",
            metadataHash = "valid_metadata_hash",
            combinedSealHash = "valid_combined_hash",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test_fingerprint"
            )
        )
        
        assertFalse(seal.verifySealIntegrity())
    }

    @Test
    fun `ForensicSeal verifySealIntegrity should fail for empty hash`() {
        val seal = ForensicSeal(
            contentHash = "",
            metadataHash = "valid_metadata_hash",
            combinedSealHash = "valid_combined_hash",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test_fingerprint"
            )
        )
        
        assertFalse(seal.verifySealIntegrity())
    }

    @Test
    fun `EvidencePackage should generate unique packageId`() {
        val pkg1 = EvidencePackage(
            evidenceItems = emptyList(),
            seal = createTestSeal(),
            metadata = EvidenceMetadata()
        )
        
        val pkg2 = EvidencePackage(
            evidenceItems = emptyList(),
            seal = createTestSeal(),
            metadata = EvidenceMetadata()
        )
        
        assertNotEquals(pkg1.packageId, pkg2.packageId)
    }

    @Test
    fun `EvidenceItem should generate unique itemId`() {
        val item1 = EvidenceItem(
            originalFilename = "file.pdf",
            mimeType = "application/pdf",
            sizeBytes = 1000,
            contentHash = "hash1",
            localPath = "/path/to/file"
        )
        
        val item2 = EvidenceItem(
            originalFilename = "file.pdf",
            mimeType = "application/pdf",
            sizeBytes = 1000,
            contentHash = "hash2",
            localPath = "/path/to/file"
        )
        
        assertNotEquals(item1.itemId, item2.itemId)
    }

    @Test
    fun `CustodyRecord should track action correctly`() {
        val record = CustodyRecord(
            action = CustodyAction.CREATED,
            previousHash = "GENESIS",
            newHash = "new_hash"
        )
        
        assertEquals(CustodyAction.CREATED, record.action)
        assertEquals("GENESIS", record.previousHash)
    }

    @Test
    fun `SealerInfo should have constitutionMode true by default`() {
        val info = SealerInfo(
            appVersion = "1.0.0",
            deviceFingerprint = "fingerprint"
        )
        
        assertTrue(info.constitutionMode)
    }

    private fun createTestSeal(): ForensicSeal {
        return ForensicSeal(
            contentHash = "test_content",
            metadataHash = "test_metadata",
            combinedSealHash = "test_combined",
            sealerInfo = SealerInfo(
                appVersion = "1.0.0",
                deviceFingerprint = "test"
            )
        )
    }
}
