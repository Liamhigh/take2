# Verum Omnis Forensic Engine

**Offline, Stateless Document Analysis with Cryptographic Sealing**

An Android application for collecting, sealing, and reporting forensic evidence in accordance with the Verum Omnis Constitutional Governance Layer.

## Features

- 📸 **Document Capture**: Camera-based document scanning and photo capture
- 📄 **PDF/Image Processing**: Offline document text extraction
- 🧠 **Verum Omnis Logic**: Automated legal subject tagging and dishonesty detection
- 🔐 **Cryptographic Sealing**: SHA-512 hashing with HMAC-SHA512 sealing for tamper detection
- 📍 **GPS Location Capture**: Automatic geolocation of evidence at collection time
- 📊 **AI-Readable Narratives**: Structured forensic reports following legal admissibility standards
- 💾 **Offline-First Design**: No cloud logging, no telemetry, airgap ready
- 🔒 **Stateless Operation**: No persistent user data beyond case files

## Constitutional Governance

This application operates under the Verum Omnis Constitution Mode, which enforces:

### Core Principles
1. **Truth** - Factual accuracy and verifiable evidence
2. **Fairness** - Protection of vulnerable parties
3. **Human Rights** - Dignity, equality, and agency
4. **Non-Extraction** - No sensitive data transmission
5. **Human Authority** - AI assists, never overrides
6. **Integrity** - No manipulation or bias
7. **Independence** - No external influence on outputs

### Forensic Standards
- Hash Standard: SHA-512
- PDF Standard: PDF 1.7
- Watermark: VERUM OMNIS 3D LOGO CENTERED
- QR Code Inclusion: Yes
- Tamper Detection: Mandatory
- Admissibility Standard: Legal-grade

### Security
- Offline First: True
- Stateless: True
- No Cloud Logging: True
- No Telemetry: True
- Airgap Ready: True

## Building

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

### Using Build Script
```bash
# Build debug APK
./scripts/build-android.sh debug

# Build release APK
./scripts/build-android.sh release

# Build both
./scripts/build-android.sh both
```

The APK will be output to `app/build/outputs/apk/`

### Running Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew test --tests "*RuleEngineTest*"
./gradlew test --tests "*CryptoSealerTest*"
```

## Usage

1. **Create a Case** - Start by creating a new forensic case with a descriptive name
2. **Add Evidence** - Use the scanner to capture documents, photos, or text notes
3. **Generate Report** - Create a forensic PDF report with full evidence chain
4. **View/Share Reports** - Access and share sealed forensic reports

## Evidence Types

- Documents (scanned)
- Photos (captured)
- Text (notes and observations)
- Audio (coming soon)
- Video (coming soon)

## Project Structure

```
forensic-engine-android/
│
├── .github/
│   └── workflows/
│       └── build-apk.yml          # CI/CD pipeline
│
├── app/
│   ├── src/main/
│   │   ├── java/org/verumomnis/forensic/
│   │   │   ├── core/              # Core forensic engine
│   │   │   │   ├── DocumentProcessor.kt    # Document processing
│   │   │   │   ├── ForensicEngine.kt       # Main engine
│   │   │   │   ├── ForensicEvidence.kt     # Evidence model
│   │   │   │   ├── RuleEngine.kt           # Verum Omnis logic
│   │   │   │   └── VerumOmnisApplication.kt
│   │   │   ├── crypto/            # Cryptographic sealing
│   │   │   │   └── CryptographicSealingEngine.kt
│   │   │   ├── location/          # GPS location services
│   │   │   │   └── ForensicLocationService.kt
│   │   │   ├── pdf/               # PDF report generation
│   │   │   │   └── ForensicPdfGenerator.kt
│   │   │   ├── report/            # Narrative generation
│   │   │   │   └── ForensicNarrativeGenerator.kt
│   │   │   └── ui/                # User interface
│   │   │       ├── MainActivity.kt
│   │   │       ├── ScannerActivity.kt
│   │   │       ├── ReportViewerActivity.kt
│   │   │       └── theme/
│   │   │           └── Theme.kt
│   │   ├── assets/
│   │   │   └── rules/             # Verum Omnis rule templates
│   │   │       ├── verum_rules.json
│   │   │       ├── dishonesty_matrix.json
│   │   │       ├── legal_subjects.json
│   │   │       └── extraction_protocol.json
│   │   └── res/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── scripts/
│   ├── build-android.sh           # Build script
│   └── generate-assets.py         # Rule asset generator
│
├── verum-constitution.json        # Constitutional rules
├── build.gradle.kts
└── README.md
```

## Rule Customization

The forensic analysis rules can be customized by editing the JSON files in `app/src/main/assets/rules/`:

- **verum_rules.json** - Main configuration and legal subjects
- **dishonesty_matrix.json** - Contradiction and fabrication detection patterns
- **legal_subjects.json** - Legal subject categories and keywords
- **extraction_protocol.json** - Keyword extraction and tagging protocol

To regenerate rule assets:
```bash
python scripts/generate-assets.py
```

## License

Copyright © 2024 Verum Global Foundation

## Creator

Liam Highcock

## Architecture

### Key Implementation Components

#### DocumentProcessor
Stateless document processing that:
- Extracts text from PDFs, images, and text files
- Applies Verum Omnis analysis rules
- Generates forensic narratives
- Creates cryptographically sealed output

#### RuleEngine
Implements the Verum Omnis logic:
- **Keyword Scanning**: Identifies relevant terms from extraction protocol
- **Legal Subject Tagging**: Classifies content by legal categories
- **Dishonesty Detection**: Finds contradictions, omissions, and fabrications
- **Score Calculation**: Weighted severity assessment

#### CryptoSealer
Provides cryptographic evidence sealing:
- SHA-512 content hashing
- HMAC-SHA512 seal signatures
- Tamper detection verification
- Chain of custody tracking

### Stateless Operation

All processing is stateless - no data persists between sessions:
```kotlin
class StatelessForensicEngine {
    fun analyze(input: ForensicInput): ForensicOutput {
        return ForensicOutput(
            narrative = generateNarrative(input),
            sealedPdf = createSealedPdf(input),
            timestamp = System.currentTimeMillis(),
            // No references to previous sessions
        )
    }
}
```
