package org.verumomnis.forensic

import org.junit.Test
import org.junit.Assert.*
import org.verumomnis.forensic.core.DeviceInfo
import org.verumomnis.forensic.core.DocumentType
import org.verumomnis.forensic.core.ForensicEvidence
import org.verumomnis.forensic.core.ForensicLocation
import org.verumomnis.forensic.crypto.CryptographicSealingEngine
import org.verumomnis.forensic.report.*
import java.time.Instant
import java.time.ZoneId

/**
 * Unit tests for the Forensic Narrative Generator
 */
class ForensicNarrativeGeneratorTest {
    
    private val generator = ForensicNarrativeGenerator()
    
    private fun createTestEvidence(): ForensicEvidence {
        val location = ForensicLocation(
            latitude = -30.7333,
            longitude = 30.4528,
            accuracyMeters = 10f,
            altitudeMeters = 50.0,
            country = "South Africa",
            countryCode = "ZA",
            administrativeArea = "KwaZulu-Natal",
            locality = "Port Shepstone",
            fullAddress = "Port Shepstone, KwaZulu-Natal, South Africa",
            timezoneId = "Africa/Johannesburg"
        )
        
        val deviceInfo = DeviceInfo(
            manufacturer = "Samsung",
            model = "Galaxy S21",
            androidVersion = "13",
            sdkVersion = 33,
            deviceId = "ABCD1234"
        )
        
        return ForensicEvidence(
            evidenceId = CryptographicSealingEngine.generateEvidenceId(),
            captureTimestampUtc = Instant.now(),
            captureTimezone = ZoneId.of("Africa/Johannesburg"),
            location = location,
            documentData = "Test document content".toByteArray(),
            documentType = DocumentType.PDF,
            originalFilename = "test_document.pdf",
            originalHash = CryptographicSealingEngine.generateHash("Test document content"),
            extractedText = "This is the extracted text content from the document.",
            documentMetadata = mapOf(
                "Author" to "Test Author",
                "Created" to "2024-01-01"
            ),
            ocrConfidence = 0.95f,
            deviceInfo = deviceInfo,
            sessionId = CryptographicSealingEngine.generateSessionId()
        )
    }
    
    private fun createTestSeal(evidence: ForensicEvidence): org.verumomnis.forensic.crypto.CryptographicSeal {
        val locationHash = CryptographicSealingEngine.generateHash(
            "${evidence.location.latitude},${evidence.location.longitude}"
        )
        
        return CryptographicSealingEngine.createSeal(
            documentData = evidence.documentData,
            timestamp = evidence.captureTimestampUtc,
            locationHash = locationHash,
            sessionId = evidence.sessionId
        )
    }
    
    @Test
    fun testNarrativeGeneration() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Contract Review",
            userDescription = "Review of employment contract for potential issues",
            urgency = Urgency.MEDIUM
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        // Verify narrative properties
        assertNotNull(narrative)
        assertEquals(evidence.evidenceId, narrative.evidenceId)
        assertEquals(evidence.sessionId, narrative.sessionId)
        assertFalse(narrative.sections.isEmpty())
        assertFalse(narrative.fullText.isEmpty())
    }
    
    @Test
    fun testAllSectionsPresent() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Evidence Collection",
            userDescription = "Test description",
            urgency = Urgency.LOW
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        // Verify all expected section types are present
        val sectionTypes = narrative.sections.map { it.sectionType }
        
        assertTrue(sectionTypes.contains(SectionType.IDENTIFICATION))
        assertTrue(sectionTypes.contains(SectionType.TEMPORAL))
        assertTrue(sectionTypes.contains(SectionType.JURISDICTION))
        assertTrue(sectionTypes.contains(SectionType.CHAIN_OF_CUSTODY))
        assertTrue(sectionTypes.contains(SectionType.CONTENT))
        assertTrue(sectionTypes.contains(SectionType.CONTRADICTIONS))
        assertTrue(sectionTypes.contains(SectionType.RIGHTS))
        assertTrue(sectionTypes.contains(SectionType.AI_GUIDANCE))
        assertTrue(sectionTypes.contains(SectionType.VERIFICATION))
    }
    
    @Test
    fun testJurisdictionSection() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Test",
            userDescription = "Test",
            urgency = Urgency.LOW
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        val jurisdictionSection = narrative.sections.find { it.sectionType == SectionType.JURISDICTION }
        assertNotNull(jurisdictionSection)
        
        val content = jurisdictionSection!!.content
        
        // Verify jurisdiction content includes location details
        assertTrue(content.contains("South Africa"))
        assertTrue(content.contains("ZA"))
        assertTrue(content.contains("KwaZulu-Natal"))
        assertTrue(content.contains("Port Shepstone"))
        
        // Verify AI instructions are present
        assertFalse(jurisdictionSection.aiInstructions.isEmpty())
    }
    
    @Test
    fun testTemporalSection() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Test",
            userDescription = "Test",
            urgency = Urgency.LOW
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        val temporalSection = narrative.sections.find { it.sectionType == SectionType.TEMPORAL }
        assertNotNull(temporalSection)
        
        val content = temporalSection!!.content
        
        // Verify temporal content includes time details
        assertTrue(content.contains("UTC Time"))
        assertTrue(content.contains("Local Time"))
        assertTrue(content.contains("Timezone"))
        assertTrue(content.contains("statute of limitations"))
    }
    
    @Test
    fun testFindingsIntegration() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = listOf(
            ForensicFinding(
                type = FindingType.CONTRADICTION,
                severity = Severity.HIGH,
                description = "Date inconsistency detected between document sections",
                evidenceReference = "Section 2.1 vs Section 4.3"
            ),
            ForensicFinding(
                type = FindingType.RIGHTS_CONCERN,
                severity = Severity.MEDIUM,
                description = "Non-compete clause may restrict employee mobility",
                category = "Employment Rights"
            )
        )
        val caseContext = CaseContext(
            caseType = "Contract Review",
            userDescription = "Review of employment contract",
            urgency = Urgency.HIGH
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        // Verify contradictions section includes the finding
        val contradictionsSection = narrative.sections.find { it.sectionType == SectionType.CONTRADICTIONS }
        assertNotNull(contradictionsSection)
        assertTrue(contradictionsSection!!.content.contains("Date inconsistency"))
        
        // Verify rights section includes the concern
        val rightsSection = narrative.sections.find { it.sectionType == SectionType.RIGHTS }
        assertNotNull(rightsSection)
        assertTrue(rightsSection!!.content.contains("Non-compete clause"))
    }
    
    @Test
    fun testAIGuidanceSection() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Fraud Investigation",
            userDescription = "Suspected fraudulent contract modification",
            urgency = Urgency.CRITICAL,
            relatedMatters = listOf("Previous case #123", "Police report #456")
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        val aiSection = narrative.sections.find { it.sectionType == SectionType.AI_GUIDANCE }
        assertNotNull(aiSection)
        
        val content = aiSection!!.content
        
        // Verify AI guidance includes case context
        assertTrue(content.contains("Fraud Investigation"))
        assertTrue(content.contains("Suspected fraudulent contract modification"))
        assertTrue(content.contains("CRITICAL"))
        
        // Verify guidance structure is present
        assertTrue(content.contains("IDENTIFY APPLICABLE LAW"))
        assertTrue(content.contains("ANALYZE THE FACTS"))
        assertTrue(content.contains("RECOMMEND ACTIONS"))
    }
    
    @Test
    fun testVerificationSection() {
        val evidence = createTestEvidence()
        val seal = createTestSeal(evidence)
        val findings = emptyList<ForensicFinding>()
        val caseContext = CaseContext(
            caseType = "Test",
            userDescription = "Test",
            urgency = Urgency.LOW
        )
        
        val narrative = generator.generateNarrative(evidence, seal, findings, caseContext)
        
        val verificationSection = narrative.sections.find { it.sectionType == SectionType.VERIFICATION }
        assertNotNull(verificationSection)
        
        val content = verificationSection!!.content
        
        // Verify cryptographic details are present
        assertTrue(content.contains(seal.verificationCode))
        assertTrue(content.contains(seal.sessionId))
        assertTrue(content.contains("SHA-512"))
        assertTrue(content.contains("QR"))
    }
    
    @Test
    fun testLocationJurisdictionString() {
        val location = ForensicLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            accuracyMeters = 5f,
            altitudeMeters = null,
            country = "United States",
            countryCode = "US",
            administrativeArea = "New York",
            locality = "New York City",
            fullAddress = "New York City, NY, USA",
            timezoneId = "America/New_York"
        )
        
        val jurisdictionString = location.getJurisdictionString()
        
        assertEquals("New York City, New York, United States", jurisdictionString)
    }
    
    @Test
    fun testLocationCoordinatesString() {
        val location = ForensicLocation(
            latitude = -33.9249,
            longitude = 18.4241,
            accuracyMeters = 10f,
            altitudeMeters = null,
            country = "South Africa",
            countryCode = "ZA",
            administrativeArea = "Western Cape",
            locality = "Cape Town",
            fullAddress = "Cape Town, Western Cape, South Africa",
            timezoneId = "Africa/Johannesburg"
        )
        
        val coordsString = location.getCoordinatesString()
        
        assertTrue(coordsString.contains("33.924900"))
        assertTrue(coordsString.contains("S"))
        assertTrue(coordsString.contains("18.424100"))
        assertTrue(coordsString.contains("E"))
    }
}
