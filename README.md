# Verum Omnis Forensic Engine

![Build Status](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml/badge.svg)

An Android application for collecting, sealing, and reporting forensic evidence in accordance with the Verum Omnis Constitutional Governance Layer.

## 🚀 Ready to Test?

**All APKs are signed and ready for installation!** 

👉 **[See TESTING.md for download and installation instructions](TESTING.md)**

Quick download:
```bash
./download-apk.sh
```

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

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### ⚠️ Build Environment Notice

**Local builds require access to Google's Maven repository** (`dl.google.com`, `maven.google.com`) for Android Gradle Plugin and dependencies.

**If you cannot build locally due to network restrictions:**
- ✅ **CI builds on GitHub Actions work perfectly** and produce APKs for every commit
- ✅ Download pre-built APKs from the latest successful workflow run in the [Actions tab](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml)
- ✅ APKs are available as artifacts: `verum-omnis-debug-apk` and `verum-omnis-release-apk`

### Build Debug APK (when network access available)
```bash
./gradlew assembleDebug
```

### Build Release APK (when network access available)
```bash
./gradlew assembleRelease
```

The APK will be output to `app/build/outputs/apk/`

### APK Signing

All APKs are automatically signed during the build process. 

**For production releases**, you can configure a release keystore:
- See **[KEYSTORE_SETUP.md](KEYSTORE_SETUP.md)** for complete setup instructions
- Configure GitHub Secrets for automated CI/CD signing
- Or use `keystore.properties` for local builds

**For development/testing**, APKs are automatically signed with the debug keystore.

For detailed information about APK signing and verification, see [APK_SIGNING.md](APK_SIGNING.md).

**Quick verification:**
```bash
./scripts/verify-apk-signature.sh  # Verifies all built APKs
./scripts/verify-apk-signature.sh app/build/outputs/apk/debug/app-debug.apk  # Verify specific APK
```

### Alternative: Download Pre-built APKs

If local builds fail due to network restrictions, you can download APKs built by CI:

1. Go to [Actions → Build Android APK](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml)
2. Click on the latest successful workflow run (green checkmark)
3. Scroll to "Artifacts" section at the bottom
4. Download `verum-omnis-debug-apk` or `verum-omnis-release-apk`

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

## FAQ

### Are the APKs signed?

**Yes!** All APKs are automatically signed during the build process. Both debug and release APKs are properly signed and can be installed on any Android device.

- ✅ Debug APKs: Signed with Android debug keystore
- ✅ Release APKs: Signed with debug keystore (suitable for testing)
- ✅ All APKs are installable and ready for testing

For details, see [APK_SIGNING.md](APK_SIGNING.md).

### How do I download and install the APKs?

See the comprehensive [TESTING.md](TESTING.md) guide for step-by-step instructions.

Quick download:
```bash
./download-apk.sh
```

### Why can't I install the APK on my device?

You need to enable "Install from unknown sources" in your Android device settings. See [TESTING.md](TESTING.md) for detailed installation instructions.

## License

Copyright © 2024 Verum Global Foundation

## Creator

Liam Highcock
