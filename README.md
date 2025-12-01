# Verum Omnis Forensic Engine

**Stateless • Offline • Cryptographically Sealed**

A forensic evidence capture and sealing engine for Android that operates completely offline. Evidence captured by this engine is cryptographically sealed in PDF format with detailed narratives designed for AI legal analysis.

## Overview

The Verum Omnis Forensic Engine is designed to capture, seal, and document evidence in a way that:

1. **Operates Completely Offline** - No data is transmitted to external servers
2. **Is Stateless** - No persistent state is stored between sessions
3. **Cryptographically Seals Evidence** - Using SHA-512 hashing and HMAC signatures
4. **Provides Location & Time** - For jurisdiction determination
5. **Generates AI-Readable Reports** - With detailed narratives for legal AI analysis
6. **Embeds Source Code** - For transparency and reproducibility

## Constitutional Governance

This engine operates under the `verum-constitution.json` which defines:

### Core Principles
1. **TRUTH** - Prioritize factual accuracy and verifiable evidence
2. **FAIRNESS** - Protect vulnerable parties, expose coercion
3. **HUMAN RIGHTS** - Uphold dignity, equality, agency
4. **NON-EXTRACTION** - No sensitive data transmitted externally
5. **HUMAN AUTHORITY** - AI assists, never overrides human judgment
6. **INTEGRITY** - No manipulation, bias, or narrative distortion
7. **INDEPENDENCE** - External actors cannot alter outputs

### Forensic Standards
- **Hash Algorithm**: SHA-512
- **PDF Standard**: PDF 1.7
- **Tamper Detection**: Mandatory
- **Admissibility**: Legal-grade, contradiction-free

## Features

### Document Scanning
- Camera-based document capture
- PDF import functionality
- Automatic text extraction
- Metadata preservation

### Location & Time Capture
- GPS coordinates at time of capture
- Reverse geocoding for jurisdiction
- Timezone-aware timestamps
- Accuracy metrics

### Cryptographic Sealing
- SHA-512 document hashing
- HMAC-SHA512 signatures
- Tamper-evident QR codes
- Verification codes

### AI-Readable Reports
The generated PDF reports include:

1. **Document Identification** - Evidence ID, session ID, hashes
2. **Temporal Context** - UTC time, local time, timezone
3. **Jurisdictional Context** - Country, state, locality, applicable law hints
4. **Chain of Custody** - Device info, capture methodology
5. **Content Analysis** - Extracted text, metadata, OCR confidence
6. **Contradictions & Issues** - Detected problems
7. **Rights Analysis** - Potential rights concerns
8. **AI Guidance** - Instructions for legal AI analysis
9. **Verification** - QR code, hash verification

## Project Structure

```
app/
├── src/main/
│   ├── java/org/verumomnis/forensic/
│   │   ├── core/
│   │   │   ├── VerumOmnisApplication.kt  # Application class
│   │   │   ├── ForensicEvidence.kt       # Evidence data structures
│   │   │   └── ForensicEngine.kt         # Main processing engine
│   │   ├── crypto/
│   │   │   └── CryptographicSealingEngine.kt  # SHA-512 & HMAC sealing
│   │   ├── location/
│   │   │   └── ForensicLocationService.kt     # GPS & geocoding
│   │   ├── pdf/
│   │   │   └── ForensicPdfGenerator.kt        # PDF report generation
│   │   ├── report/
│   │   │   └── ForensicNarrativeGenerator.kt  # AI narrative generation
│   │   └── ui/
│   │       ├── MainActivity.kt            # Main UI
│   │       ├── ScannerActivity.kt         # Document scanner
│   │       └── ReportViewerActivity.kt    # Report listing
│   └── res/
│       └── [Android resources]
└── src/test/
    └── java/org/verumomnis/forensic/
        ├── CryptographicSealingEngineTest.kt
        └── ForensicNarrativeGeneratorTest.kt
```

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```

## Usage

### Basic Workflow

1. **Open the App** - Grant location and camera permissions
2. **Scan Document** - Position document and capture
3. **Add Description** - Describe the evidence
4. **Generate Report** - Engine creates sealed PDF
5. **Share/Store** - Export report as needed

### Programmatic Usage

```kotlin
val engine = ForensicEngine(context)

// Process a document
val report = engine.processDocument(
    documentData = pdfBytes,
    documentType = DocumentType.PDF,
    originalFilename = "contract.pdf",
    extractedText = "Contract text...",
    documentMetadata = mapOf("Author" to "John Doe"),
    ocrConfidence = 0.98f,
    caseContext = CaseContext(
        caseType = "Contract Review",
        userDescription = "Employment contract for review",
        urgency = Urgency.MEDIUM
    ),
    findings = listOf(
        ForensicFinding(
            type = FindingType.POTENTIAL_ISSUE,
            severity = Severity.MEDIUM,
            description = "Non-compete clause may be overly restrictive"
        )
    )
)

// Save the report
val result = engine.saveReport(report, outputDir)
```

### Verifying a Seal

```kotlin
val verificationResult = engine.verifySeal(seal, originalDocumentData)

if (verificationResult.isValid) {
    println("Document integrity confirmed")
} else {
    println("WARNING: ${verificationResult.failureReason}")
}
```

## Report Structure for AI Analysis

The generated reports are structured specifically for AI legal analysis. Each section includes:

- **Content** - Human and machine-readable information
- **AI Instructions** - Specific guidance for AI analysis
- **Metadata** - Structured data for parsing

### Jurisdiction Determination

The report provides:
- GPS coordinates at capture
- Country, state/province, city
- Legal system indication (common law, civil law, etc.)
- Timezone for deadline calculations

This allows AI systems to:
- Identify applicable laws
- Consider jurisdiction-specific statutes
- Calculate time-sensitive deadlines
- Reference appropriate legal precedents

## Security

- **Offline-First**: No network requests
- **Stateless**: No persistent data storage
- **No Telemetry**: No analytics or tracking
- **No Cloud Backup**: Data extraction rules prevent cloud backup
- **Airgap Ready**: Can operate in completely airgapped environments

## Legal Considerations

This tool is designed to assist with evidence documentation. Users should:

1. Understand local laws regarding evidence collection
2. Consult with legal professionals for advice
3. Use generated reports as supporting documentation
4. Verify AI-generated legal analysis with qualified attorneys

## Credits

- **Creator**: Liam Highcock
- **Foundation**: Verum Global Foundation
- **Constitution Version**: 1.0
- **Engine Version**: 1.0

## License

See the Verum Omnis Constitution for governance and usage terms.
