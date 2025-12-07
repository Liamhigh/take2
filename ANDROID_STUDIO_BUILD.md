# Android Studio Build Instructions

## Quick Start (5 Minutes)

### Step 1: Clone the Repository
```bash
git clone https://github.com/Liamhigh/take2.git
cd take2
git checkout copilot/complete-forensic-app-delivery
```

### Step 2: Open in Android Studio
1. Launch Android Studio
2. Click "Open" or "Open an Existing Project"
3. Navigate to the cloned repository folder
4. Click "OK"

### Step 3: Sync Project
1. Android Studio will automatically start syncing
2. Wait for "Gradle sync finished" message
3. This should take 2-5 minutes on first run

### Step 4: Run the App
1. Connect an Android device (Android 8.0+) or start an emulator
2. Click the green **Run** button (▶️) or press `Shift+F10`
3. Select your device from the list
4. Wait for the app to install and launch

**That's it! You're done! 🎉**

## What You'll See

### Main Screen
- Two buttons for creating cases:
  - **Create New Case (Full Forensic)** - Uses the full cryptographic sealing engine
  - **Create Simple Case (Contradiction Engine)** - Uses the new contradiction detection engine

### After Creating a Simple Case
1. Case Detail screen opens
2. Add evidence by clicking "Add Evidence"
3. Enter text evidence (e.g., witness statements, documents)
4. Click "Run Contradiction Engine"
5. View the generated report

## Testing the Contradiction Engine

### Test Case 1: Simple Contradiction
1. Create a simple case: "Test Case 1"
2. Add evidence:
   - Summary: "Witness Statement"
   - Content: "I did go to the meeting. I never went to any meeting."
3. Run engine
4. Should detect: "did" vs "never" contradiction

### Test Case 2: Legal Rules
1. Create a simple case: "Test Case 2"
2. Add evidence:
   - Summary: "Contract Review"
   - Content: "This contract contains fraud. There was coercion involved."
3. Run engine
4. Should detect legal findings for "fraud" and "coercion"

### Test Case 3: Multiple Contradictions
1. Create a simple case: "Test Case 3"
2. Add evidence:
   - Summary: "Multiple Statements"
   - Content: "The door was locked. The door was not locked. I always check the door. I never check the door."
3. Run engine
4. Should detect multiple contradictions

## Build Variants

### Debug Build (Default)
- Includes debugging symbols
- No code obfuscation
- Larger APK size
- Default when clicking Run

### Release Build
1. In Android Studio: Build → Select Build Variant
2. Select "release" from dropdown
3. Click Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK will be in: `app/build/outputs/apk/release/`

## Project Structure

```
app/src/main/
├── java/org/verumomnis/forensic/
│   ├── core/           # Full forensic engine (existing)
│   ├── engine/         # Simple contradiction engine (NEW)
│   │   ├── CaseRepository.kt
│   │   ├── ContradictionEngine.kt
│   │   ├── EngineOrchestrator.kt
│   │   ├── EvidenceParser.kt
│   │   └── LawEngine.kt
│   ├── model/          # Data models (NEW)
│   │   ├── Case.kt
│   │   ├── Evidence.kt
│   │   └── Contradiction.kt
│   └── ui/             # User interface
│       ├── MainActivity.kt (UPDATED)
│       ├── CaseDetailActivity.kt (NEW)
│       └── ReportViewerActivity.kt (UPDATED)
└── assets/
    └── rules/
        └── verum_rules.json (NEW)
```

## Dependencies

All dependencies are automatically downloaded during Gradle sync:

- **Kotlin** 2.0.21
- **Jetpack Compose** (for UI)
- **Gson** 2.10.1 (for JSON serialization)
- **iText PDF** 7.2.5 (for PDF generation)
- **ML Kit** (for OCR)
- **JUnit** 4.13.2 (for testing)

## Troubleshooting

### Issue: "Gradle sync failed"
**Solution**: 
- Click "File → Invalidate Caches → Invalidate and Restart"
- Make sure you have internet connection
- Check that Android Studio is up to date

### Issue: "SDK not found"
**Solution**:
- Click "Tools → SDK Manager"
- Install Android SDK 26-34
- Click "Apply" and wait for download

### Issue: "Unable to resolve dependency"
**Solution**:
- Check internet connection
- Click "File → Sync Project with Gradle Files"
- Try "Build → Clean Project" then "Build → Rebuild Project"

### Issue: "Emulator not starting"
**Solution**:
- Open AVD Manager (Tools → AVD Manager)
- Create a new virtual device
- Choose Pixel 4 or newer
- Select API 26 or higher
- Start the emulator

### Issue: "App crashes on launch"
**Solution**:
- Check Logcat for error messages
- Make sure device is Android 8.0+ (API 26+)
- Try "Build → Clean Project" and rebuild

## Running Tests

### Unit Tests
```bash
./gradlew test
```

Or in Android Studio:
1. Right-click on `app/src/test/java`
2. Select "Run 'All Tests'"

### Specific Test
1. Open `ContradictionEngineTest.kt`
2. Click green arrow next to test method
3. Select "Run 'testName()'"

## Code Coverage

Generate code coverage report:
```bash
./gradlew jacocoTestReport
```

View report at: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## Debugging

### Enable Debugging
1. On device: Settings → About Phone → Tap "Build Number" 7 times
2. Settings → Developer Options → Enable "USB Debugging"
3. Connect device to computer
4. Accept USB debugging prompt

### Set Breakpoints
1. Click in the gutter (left of line numbers) to set breakpoint
2. Click Debug button (🐛) instead of Run
3. App will pause at breakpoint
4. Use Debug panel to inspect variables

## Performance Testing

### Profile the App
1. Run → Profile 'app'
2. Select device
3. Choose profiler:
   - CPU Profiler
   - Memory Profiler
   - Network Profiler

## Customization

### Change App Name
Edit: `app/src/main/res/values/strings.xml`
```xml
<string name="app_name">Your App Name</string>
```

### Change Package Name
1. File → Project Structure → Modules → app
2. Change "applicationId" in the module settings
3. Refactor → Rename package in code

### Customize UI Theme
Edit: `app/src/main/java/org/verumomnis/forensic/ui/theme/Theme.kt`

## Advanced Configuration

### Increase Memory for Gradle
Edit: `gradle.properties`
```
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

### Enable Parallel Builds
Edit: `gradle.properties`
```
org.gradle.parallel=true
org.gradle.caching=true
```

### ProGuard Rules (Release Build)
Edit: `app/proguard-rules.pro`
```
-keep class org.verumomnis.forensic.model.** { *; }
-keep class org.verumomnis.forensic.engine.** { *; }
```

## CI/CD Integration

The project includes GitHub Actions workflows:
- `.github/workflows/android.yml` - Automated builds and tests
- View build status in GitHub Actions tab

## Next Steps

1. ✅ **Run the app** - Follow quick start above
2. ✅ **Test contradiction engine** - Try the test cases
3. ✅ **Add custom rules** - Edit `verum_rules.json`
4. ✅ **Extend functionality** - Modify engine code
5. ✅ **Deploy to production** - Generate release APK

## Support Resources

- **Android Studio Docs**: https://developer.android.com/studio
- **Kotlin Docs**: https://kotlinlang.org/docs/home.html
- **Jetpack Compose**: https://developer.android.com/jetpack/compose

---

**Ready to build? Just click File → Open in Android Studio! 🚀**
