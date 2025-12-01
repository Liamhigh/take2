# Verum Omnis Forensic Engine

![Build Status](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml/badge.svg)

An Android application for collecting, sealing, and reporting forensic evidence in accordance with the Verum Omnis Constitutional Governance Layer.

## Features

- **Cryptographic Evidence Sealing**: SHA-512 hashing with HMAC-SHA512 sealing for tamper detection
- **GPS Location Capture**: Automatic geolocation of evidence at collection time
- **AI-Readable PDF Reports**: Structured forensic narratives following legal admissibility standards
- **Offline-First Design**: No cloud logging, no telemetry, airgap ready
- **Stateless Operation**: No persistent user data beyond case files

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

**⚠️ Important:** This repository cannot be built locally due to network restrictions that block access to Google's Maven repository. See [BUILD_TROUBLESHOOTING.md](BUILD_TROUBLESHOOTING.md) for details.

### Getting Pre-Built APKs

Use the provided script to download the latest pre-built APK from GitHub Actions:

```bash
./download-apk.sh
```

Or manually download from the [Actions tab](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml).

For complete instructions, see [GETTING_STARTED.md](GETTING_STARTED.md).

### Building in Unrestricted Environments

If you have access to an environment without network restrictions:

#### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

#### Build Debug APK
```bash
./gradlew assembleDebug
```

#### Build Release APK
```bash
./gradlew assembleRelease
```

The APK will be output to `app/build/outputs/apk/`

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
app/src/main/java/org/verumomnis/forensic/
├── core/                    # Core forensic engine
│   ├── ForensicEngine.kt
│   ├── ForensicEvidence.kt
│   └── VerumOmnisApplication.kt
├── crypto/                  # Cryptographic sealing
│   └── CryptographicSealingEngine.kt
├── location/               # GPS location services
│   └── ForensicLocationService.kt
├── pdf/                    # PDF report generation
│   └── ForensicPdfGenerator.kt
├── report/                 # Narrative generation
│   └── ForensicNarrativeGenerator.kt
└── ui/                     # User interface
    ├── MainActivity.kt
    ├── ScannerActivity.kt
    ├── ReportViewerActivity.kt
    └── theme/
        └── Theme.kt
```

## License

Copyright © 2024 Verum Global Foundation

## Creator

Liam Highcock
