# Android App Skeleton - Verum Omnis Contradiction Engine

## Overview

This branch contains a **complete Android app skeleton** for the Verum Omnis Contradiction Engine. The skeleton includes both:

1. **Simplified ViewBinding-based structure** (package `org.verumomnis`) - Traditional XML layouts
2. **Production forensic app** (package `org.verumomnis.forensic`) - Jetpack Compose UI

## Simplified Skeleton Structure

The following files have been created in the `org.verumomnis` package:

### Kotlin Files

#### Activities
- `app/src/main/java/org/verumomnis/MainActivity.kt` - Main entry point for creating cases
- `app/src/main/java/org/verumomnis/CaseDetailActivity.kt` - Add evidence and generate reports
- `app/src/main/java/org/verumomnis/ReportViewerActivity.kt` - View generated contradiction reports

#### Engine
- `app/src/main/java/org/verumomnis/engine/ContradictionEngine.kt` - Core contradiction detection engine
- `app/src/main/java/org/verumomnis/engine/Sentence.kt` - Data class for sentences
- `app/src/main/java/org/verumomnis/engine/ContradictionResult.kt` - Data class for results

#### Utilities
- `app/src/main/java/org/verumomnis/utils/FileUtils.kt` - File I/O operations
- `app/src/main/java/org/verumomnis/utils/OcrUtils.kt` - OCR stub (TODO: implement)

### XML Layouts

- `app/src/main/res/layout/activity_main.xml` - Main screen with case creation
- `app/src/main/res/layout/activity_case_detail.xml` - Evidence input screen
- `app/src/main/res/layout/activity_report_viewer.xml` - Report display screen

### Resources

- `app/src/main/res/values/colors.xml` - Color definitions
- `app/src/main/res/values/strings.xml` - String resources (already existed)
- `app/src/main/res/values/themes.xml` - Theme definitions (already existed)

### Configuration

- `app/build.gradle.kts` - Updated with ViewBinding support and AppCompat dependency
- `app/src/main/AndroidManifest.xml` - Updated with new activities

## Building in Android Studio

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK API 34

### Steps to Build

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Liamhigh/take2.git
   cd take2
   git checkout copilot/create-android-app-skeleton
   ```

2. **Open in Android Studio**:
   - File → Open → Select the `take2` folder
   - Wait for Gradle sync to complete

3. **Choose which app to run**:
   
   **Option A: Run the simplified skeleton** (uses `org.verumomnis.MainActivity`)
   - Edit `AndroidManifest.xml` and ensure the simplified `MainActivity` has the launcher intent filter
   
   **Option B: Run the production forensic app** (uses `org.verumomnis.forensic.ui.MainActivity`)
   - This is the default configuration
   - Keep the existing manifest configuration

4. **Build the APK**:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or run on device/emulator: Run → Run 'app'

## Key Features

### Simplified Skeleton (`org.verumomnis`)

✅ **Traditional Android Architecture**
- XML layouts with ViewBinding
- AppCompatActivity base classes
- Simple file-based storage
- Straightforward navigation

✅ **Contradiction Engine**
- Detects opposite statements (yes/no, true/false, always/never)
- Identifies temporal contradictions (did/didn't, was/wasn't)
- Recognizes negation contradictions
- Generates formatted text reports

✅ **Case Management**
- Create cases with unique IDs
- Add text evidence
- OCR support (stub - ready for implementation)
- Save and load reports

### Production Forensic App (`org.verumomnis.forensic`)

✅ **Advanced Features**
- Jetpack Compose UI
- Triple-hash SHA-512 cryptography
- GPS jurisdiction detection
- PDF/A-3B compliant reports
- Chain of custody logging
- Offline verification
- 10 forensic modules

## Architecture Comparison

| Feature | Simplified Skeleton | Production App |
|---------|-------------------|----------------|
| UI Framework | XML + ViewBinding | Jetpack Compose |
| Package | `org.verumomnis` | `org.verumomnis.forensic` |
| Complexity | Beginner-friendly | Production-grade |
| Storage | Simple file I/O | Encrypted storage |
| Reports | Text format | PDF/A-3B format |
| Security | Basic | Forensic-grade |

## Extending the Skeleton

The simplified skeleton is designed to be extended by Copilot or developers:

### TODO Items

1. **OCR Implementation** (`OcrUtils.kt`):
   ```kotlin
   // Implement using ML Kit or Tesseract
   // See: https://developers.google.com/ml-kit/vision/text-recognition
   ```

2. **Enhanced Contradiction Detection**:
   - Add semantic analysis
   - Implement timeline detection
   - Add financial contradiction checking
   - See `LevelerEngine.kt` in forensic package for advanced implementation

3. **UI Improvements**:
   - Add progress indicators
   - Implement case list view
   - Add evidence preview
   - Improve report formatting

4. **Data Persistence**:
   - Consider Room database
   - Add export/import functionality
   - Implement backup/restore

## File Structure

```
app/
├── build.gradle.kts                          # Updated with ViewBinding
├── proguard-rules.pro                        # Existing ProGuard rules
└── src/
    └── main/
        ├── AndroidManifest.xml               # Updated with new activities
        ├── java/org/verumomnis/
        │   ├── MainActivity.kt               # ✅ NEW
        │   ├── CaseDetailActivity.kt         # ✅ NEW
        │   ├── ReportViewerActivity.kt       # ✅ NEW
        │   ├── engine/
        │   │   ├── ContradictionEngine.kt    # ✅ NEW
        │   │   ├── Sentence.kt               # ✅ NEW
        │   │   └── ContradictionResult.kt    # ✅ NEW
        │   ├── utils/
        │   │   ├── FileUtils.kt              # ✅ NEW
        │   │   └── OcrUtils.kt               # ✅ NEW
        │   └── forensic/                     # Existing production code
        │       ├── core/
        │       ├── crypto/
        │       ├── custody/
        │       ├── jurisdiction/
        │       ├── leveler/
        │       ├── location/
        │       ├── pdf/
        │       ├── report/
        │       ├── ui/
        │       └── verification/
        └── res/
            ├── layout/
            │   ├── activity_main.xml         # ✅ NEW
            │   ├── activity_case_detail.xml  # ✅ NEW
            │   └── activity_report_viewer.xml # ✅ NEW
            └── values/
                ├── colors.xml                # ✅ NEW
                ├── strings.xml               # Existing
                └── themes.xml                # Existing
```

## Notes for Copilot

This skeleton is ready for AI-assisted development. Key extension points:

1. **OcrUtils.pickImage()** - Implement image selection and OCR
2. **ContradictionEngine** - Enhance detection algorithms
3. **Layouts** - Add more UI components and styling
4. **Navigation** - Consider adding Navigation Component
5. **Testing** - Add unit tests for engine logic

## Support

For questions or issues:
- Review existing production code in `org.verumomnis.forensic` for reference
- Check `LevelerEngine.kt` for advanced contradiction detection
- See `ForensicEngine.kt` for comprehensive forensic analysis

## License

This project is part of the Verum Omnis forensic evidence system.
