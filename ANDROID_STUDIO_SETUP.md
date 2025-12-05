# Android Studio Setup Guide

## Quick Start (5 Minutes)

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17 (bundled with Android Studio)
- **Android SDK**: API Level 34 (will be downloaded automatically)
- **Internet Connection**: Required for initial Gradle sync and dependency download

### Step 1: Clone the Repository

```bash
git clone https://github.com/Liamhigh/take2.git
cd take2
```

Or download as ZIP from GitHub and extract.

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **File → Open** (or **Open** from welcome screen)
3. Navigate to the cloned `take2` directory
4. Click **OK**

### Step 3: Wait for Gradle Sync

Android Studio will automatically:
- ✅ Download Gradle 8.9
- ✅ Sync dependencies from Maven Central and Google Maven
- ✅ Configure the Android SDK (API 34)
- ✅ Index the project files

**This takes 2-5 minutes on first sync** (depending on internet speed).

### Step 4: Build the APK

Once Gradle sync completes:

1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete (1-3 minutes)
3. Click **locate** in the notification to find the APK

Or use the toolbar:
- Click the **Run** button (▶️) to build and install on a connected device/emulator
- Click **Build → Make Project** (Ctrl+F9 / Cmd+F9) to compile

### Step 5: Run on Device/Emulator

**Option A: Physical Device**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect via USB
4. Click **Run** (▶️) in Android Studio
5. Select your device

**Option B: Emulator**
1. Click **Tools → Device Manager**
2. Create a new virtual device (or use existing)
3. Select a system image (API 34 recommended)
4. Click **Run** (▶️)
5. Select the emulator

---

## Detailed Build Instructions

### Build Variants

The project has two build variants:

1. **Debug** (default)
   - Includes debugging symbols
   - No code obfuscation
   - Signed with debug keystore
   - Faster build time

2. **Release**
   - Code optimization enabled (ProGuard)
   - Signed with debug keystore (for testing)
   - Smaller APK size

Switch variants: **Build → Select Build Variant** (usually in bottom-left panel)

### Gradle Commands

You can also build from the terminal within Android Studio:

**Build Debug APK:**
```bash
./gradlew assembleDebug
```

**Build Release APK:**
```bash
./gradlew assembleRelease
```

**Run Tests:**
```bash
./gradlew testDebugUnitTest
```

**Clean Build:**
```bash
./gradlew clean assembleDebug
```

APKs will be in: `app/build/outputs/apk/debug/` or `app/build/outputs/apk/release/`

---

## Troubleshooting

### Issue: Gradle Sync Failed

**Symptoms:** Red errors in build.gradle.kts files

**Solutions:**
1. Check internet connection (Maven repositories must be accessible)
2. Click **File → Invalidate Caches → Invalidate and Restart**
3. Delete `.gradle` folder and sync again
4. Ensure JDK 17 is selected: **File → Project Structure → SDK Location → Gradle JDK**

### Issue: SDK Not Found

**Symptoms:** "Android SDK not found" or "compileSdk 34 not found"

**Solutions:**
1. **Tools → SDK Manager**
2. Install "Android SDK Platform 34" under SDK Platforms tab
3. Click **Apply** and wait for download
4. Sync Gradle again

### Issue: Build Too Slow

**Solutions:**
1. Increase Gradle heap size in `gradle.properties`:
   ```
   org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
   ```
2. Enable parallel builds:
   ```
   org.gradle.parallel=true
   ```
3. Use Gradle daemon (enabled by default)
4. Close unused projects in Android Studio

### Issue: Cannot Access Google Maven

**Symptoms:** "Could not resolve com.android.application:..." errors

**Solutions:**
- ✅ If on restricted network, use **GitHub Actions CI** to build APKs
- ✅ Download pre-built APKs from [Actions tab](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml)
- ✅ Set up a proxy if behind corporate firewall

### Issue: Kotlin Plugin Error

**Symptoms:** "Kotlin not configured" or version mismatch warnings

**Solutions:**
1. The project uses Kotlin 2.0.21 (configured in build.gradle.kts)
2. Update Android Studio to latest version
3. **Tools → Kotlin → Configure Kotlin Plugin Updates** → Check for updates

---

## Project Configuration

### Key Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` (root) | Plugin versions (AGP 8.6.1, Kotlin 2.0.21) |
| `settings.gradle.kts` | Repository configuration, module includes |
| `app/build.gradle.kts` | App dependencies, build config, SDK versions |
| `gradle.properties` | Gradle JVM settings, AndroidX config |
| `gradle-wrapper.properties` | Gradle version (8.9) |

### Versions Used

- **Gradle**: 8.9
- **Android Gradle Plugin (AGP)**: 8.6.1
- **Kotlin**: 2.0.21
- **JDK**: 17
- **Compile SDK**: 34
- **Target SDK**: 34
- **Min SDK**: 26 (Android 8.0)

### Dependencies

Main dependencies:
- Jetpack Compose BOM 2024.01.00
- Material3 (Compose)
- iTextPDF 7.2.5 (PDF generation)
- ZXing 3.5.2 (QR codes)
- Google Location Services 21.0.1
- CameraX 1.3.1
- ML Kit Document Scanner 16.0.0

All dependencies are declared in `app/build.gradle.kts`.

---

## Running Tests in Android Studio

### Unit Tests

1. Right-click on `app/src/test/java` folder
2. Select **Run 'Tests in 'test''**

Or run individual test files:
1. Open a test file (e.g., `CryptographicSealingEngineTest.kt`)
2. Click the green arrow next to the class name
3. Select **Run**

### Test Coverage

Generate test coverage report:

```bash
./gradlew jacocoTestReport
```

Report location: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

---

## Development Workflow

### Recommended Workflow

1. **Sync** - Let Gradle sync complete after cloning
2. **Build** - Build debug APK to verify everything works
3. **Run** - Deploy to device/emulator to test
4. **Edit** - Make changes to code
5. **Hot Reload** - Save files, app updates automatically (when using Run)
6. **Test** - Run unit tests frequently
7. **Commit** - Commit working changes

### Code Style

The project uses Kotlin official code style:
- **File → Settings → Editor → Code Style → Kotlin**
- Scheme: "Project" (configured in `.editorconfig` if present)

### Linting

Run lint checks:

```bash
./gradlew lintDebug
```

Lint report: `app/build/reports/lint-results-debug.html`

---

## CI/CD Integration

This project uses **GitHub Actions** for continuous integration.

### Workflow Triggers

Builds run automatically on:
- Push to `main` branch
- Push to `copilot/**` branches
- Pull requests to `main`
- Manual trigger via Actions tab

### Artifacts

Each successful build produces:
- `verum-omnis-debug-apk` (Debug APK)
- `verum-omnis-release-apk` (Release APK)
- `test-results` (Test reports, lint results, coverage)

Download from: **Actions → Build Android APK → Latest run → Artifacts**

---

## APK Installation

### Install Debug APK

After building in Android Studio:

1. **Locate APK**: `app/build/outputs/apk/debug/app-debug.apk`
2. **Transfer to device** (if not using Run button)
3. **Install**: Tap the APK file on device
4. **Allow unknown sources** if prompted

### Verify APK Signature

```bash
./scripts/verify-apk-signature.sh app/build/outputs/apk/debug/app-debug.apk
```

All APKs are automatically signed (debug keystore for local builds).

---

## Next Steps

- ✅ Read [README.md](README.md) for project overview
- ✅ See [TESTING.md](TESTING.md) for APK installation and testing
- ✅ Check [BUILD_VERIFICATION.md](BUILD_VERIFICATION.md) for verification steps
- ✅ Review [APK_SIGNING.md](APK_SIGNING.md) for signing details

---

## Support

**Build Issues?**
- Check [Troubleshooting](#troubleshooting) section above
- Review GitHub Actions logs for successful build configuration
- Open an issue on GitHub with error logs

**Network Restrictions?**
- Use GitHub Actions to build APKs (CI has full network access)
- Download pre-built APKs from Actions artifacts

---

**Last Updated**: December 2024  
**Tested With**: Android Studio Hedgehog 2023.1.1+, Iguana 2023.2.1+, Jellyfish 2023.3.1+
