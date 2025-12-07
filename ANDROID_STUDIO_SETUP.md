# Android Studio Setup Guide

This guide provides step-by-step instructions for opening and building the Verum Omnis Forensic app in Android Studio.

## ✅ Prerequisites

Before you begin, ensure you have:

1. **Android Studio** - Hedgehog (2023.1.1) or later
   - Download from: https://developer.android.com/studio
   
2. **JDK 17** - Required for this project
   - Android Studio includes JDK, but you can verify/configure it in: File → Project Structure → SDK Location
   
3. **Android SDK 34** - Will be installed automatically by Android Studio
   - Minimum SDK: 26
   - Target SDK: 34
   - Compile SDK: 34

4. **Internet Connection** - Required for initial Gradle sync to download:
   - Android Gradle Plugin (8.6.1)
   - Kotlin Plugin (2.0.21)
   - Dependencies from Maven Central and Google Maven

## 📋 Step-by-Step Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/Liamhigh/take2.git
cd take2
git checkout copilot/fix-gradle-sync-issues
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Click **"Open"** (or File → Open)
3. Navigate to the cloned repository directory
4. Select the `take2` folder (the one containing `build.gradle.kts`)
5. Click **"OK"**

### Step 3: Wait for Gradle Sync

Android Studio will automatically:
1. Download Gradle 8.9 (if not already cached)
2. Download Android Gradle Plugin 8.6.1
3. Download Kotlin Plugin 2.0.21
4. Download all project dependencies
5. Index the project files

**Expected Duration:** 2-5 minutes on first open (faster on subsequent opens)

**Progress Indicators:**
- Bottom status bar shows "Gradle Build Running..."
- Build output appears in the "Build" tab at bottom
- A sync icon appears in the toolbar while syncing

### Step 4: Verify Gradle Sync Success

Once sync completes, verify:

✅ **No errors in "Build" tab** at the bottom
✅ **Project structure visible** in left sidebar with `app` module expanded
✅ **Green "Run" button** enabled in toolbar (▶️ icon)
✅ **No red underlines** in Kotlin files when you open them

If you see errors, see the [Troubleshooting](#troubleshooting) section below.

### Step 5: Build the Project

Choose one of these options:

#### Option A: Build Debug APK (Recommended for Testing)
```bash
# In Android Studio Terminal (View → Tool Windows → Terminal):
./gradlew assembleDebug
```

**Output Location:** `app/build/outputs/apk/debug/app-debug.apk`

#### Option B: Build Release APK
```bash
./gradlew assembleRelease
```

**Output Location:** `app/build/outputs/apk/release/app-release.apk`

#### Option C: Use Android Studio GUI
1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete
3. Click **"locate"** in the success notification to find the APK

### Step 6: Run on Device/Emulator

#### To run on a physical device:
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click the green **"Run"** button (▶️) in Android Studio
5. Select your device from the list
6. Click **"OK"**

#### To run on an emulator:
1. Click **Tools → Device Manager**
2. Create a new Virtual Device if needed:
   - Choose a device (e.g., Pixel 6)
   - Choose Android API 34 or higher
   - Click **"Finish"**
3. Click the green **"Run"** button (▶️)
4. Select the emulator from the list
5. Click **"OK"**

## 🔧 Gradle Configuration Details

This project uses:

### Gradle Version
- **Gradle:** 8.9
- **Gradle Wrapper:** Included (no separate installation needed)
- **Wrapper Location:** `gradle/wrapper/gradle-wrapper.properties`

### Gradle Plugins
```kotlin
// build.gradle.kts (root)
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

### Key Dependencies
```kotlin
// app/build.gradle.kts
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.5")

    // QR Code
    implementation("com.google.zxing:core:3.5.2")

    // Camera/ML Kit
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("com.google.mlkit:text-recognition:16.0.0")
}
```

### Repository Configuration
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()           // Required for Android Gradle Plugin
        mavenCentral()     // Required for most dependencies
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()           // Required for AndroidX, Play Services
        mavenCentral()     // Required for most third-party libs
    }
}
```

## 🛠️ Troubleshooting

### Issue: "Plugin [id: 'com.android.application'] was not found"

**Cause:** Cannot connect to Google Maven repository (`dl.google.com`, `maven.google.com`)

**Solution:**
1. Check your internet connection
2. Check if your firewall/antivirus is blocking Maven repositories
3. If on corporate network, check proxy settings:
   - File → Settings → Appearance & Behavior → System Settings → HTTP Proxy
   - Configure proxy if required
4. Try invalidating caches: File → Invalidate Caches → Invalidate and Restart

### Issue: "Gradle sync failed" with SSL errors

**Cause:** Corporate proxy/firewall with SSL inspection

**Solution:**
1. Add company SSL certificates to Java keystore
2. Or disable SSL validation (not recommended for production):
   - Add to `gradle.properties`:
   ```properties
   systemProp.javax.net.ssl.trustStore=/path/to/truststore
   systemProp.javax.net.ssl.trustStorePassword=changeit
   ```

### Issue: "SDK location not found"

**Cause:** Android SDK not configured

**Solution:**
1. File → Project Structure → SDK Location
2. Set "Android SDK location" to your SDK path
3. If SDK not installed, download via: Tools → SDK Manager

### Issue: "Kotlin not configured"

**Cause:** Kotlin plugin not installed

**Solution:**
1. File → Settings → Plugins
2. Search for "Kotlin"
3. Install the official Kotlin plugin
4. Restart Android Studio

### Issue: Build fails with "Execution failed for task ':app:compileDebugKotlin'"

**Cause:** Incompatible Kotlin version or corrupted cache

**Solution:**
1. Try Build → Clean Project
2. Try Build → Rebuild Project
3. Check that JDK 17 is selected:
   - File → Project Structure → SDK Location → Gradle Settings → Gradle JDK
   - Select "Embedded JDK 17" or any JDK 17 installation

### Issue: "Manifest merger failed"

**Cause:** Conflicting manifest configurations from dependencies

**Solution:**
This project's manifest is correctly configured. If you see this error:
1. Check the Build output for specific conflict details
2. This project should not have manifest conflicts
3. If you've added new dependencies, you may need to add tools:replace in AndroidManifest.xml

### Issue: Missing imports or unresolved references

**Cause:** Gradle sync incomplete or IDE cache issues

**Solution:**
1. File → Sync Project with Gradle Files
2. File → Invalidate Caches → Invalidate and Restart
3. If still failing, delete `.gradle` and `.idea` folders, then reopen project

### Issue: "Cannot find Google Play Services"

**Cause:** Google Repository not available

**Solution:**
1. Ensure internet connection
2. Tools → SDK Manager → SDK Tools tab
3. Check "Google Play Services"
4. Click "Apply"

## 📱 Project Structure Overview

```
VerumOmnisForensic/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/verumomnis/forensic/
│   │   │   │   ├── core/              # Core forensic engine
│   │   │   │   ├── crypto/            # Cryptographic sealing (SHA-512, HMAC)
│   │   │   │   ├── location/          # GPS location services
│   │   │   │   ├── pdf/               # PDF report generation
│   │   │   │   ├── report/            # Narrative generation
│   │   │   │   ├── custody/           # Chain of custody logging
│   │   │   │   ├── verification/      # Offline verification
│   │   │   │   ├── leveler/           # Contradiction detection
│   │   │   │   ├── jurisdiction/      # Multi-jurisdiction compliance
│   │   │   │   └── ui/                # User interface (Compose)
│   │   │   ├── res/                   # Resources (layouts, strings, etc.)
│   │   │   └── AndroidManifest.xml    # App manifest
│   │   └── test/                      # Unit tests
│   └── build.gradle.kts               # App module build configuration
├── gradle/
│   └── wrapper/                       # Gradle wrapper files
├── build.gradle.kts                   # Root build configuration
├── settings.gradle.kts                # Project settings
└── gradle.properties                  # Gradle properties
```

## 🧪 Running Tests

### In Android Studio:
1. Right-click on `app/src/test` folder
2. Select "Run 'Tests in 'test''"

### From Command Line:
```bash
./gradlew test
./gradlew testDebugUnitTest  # Debug variant only
```

### View Test Results:
- **HTML Report:** `app/build/reports/tests/testDebugUnitTest/index.html`
- **XML Report:** `app/build/test-results/testDebugUnitTest/`

## 📊 Code Coverage

Generate test coverage report:
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

View coverage report:
- **HTML Report:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## 🚀 Build Variants

This project has two build variants:

### Debug
- **Minification:** Disabled
- **Test Coverage:** Enabled
- **Signing:** Debug keystore (automatic)
- **Use for:** Development and testing

### Release
- **Minification:** Enabled (ProGuard)
- **Test Coverage:** Disabled
- **Signing:** Debug keystore (suitable for testing)
- **Use for:** Performance testing and distribution

Switch variants in Android Studio:
- View → Tool Windows → Build Variants
- Select "debug" or "release" for app module

## 📦 ProGuard Configuration

Release builds use ProGuard for code minification and obfuscation.

ProGuard rules: `app/proguard-rules.pro`

To see mapping file after release build:
- `app/build/outputs/mapping/release/mapping.txt`

## 🎯 Next Steps After Setup

1. **Explore the code:**
   - Start with `MainActivity.kt` - main entry point
   - Look at `ForensicEngine.kt` - core forensic logic
   - Review `CryptographicSealingEngine.kt` - security implementation

2. **Run the app:**
   - Create a test case
   - Add evidence
   - Generate a forensic report
   - Verify the cryptographic seal

3. **Read the documentation:**
   - `README.md` - Project overview
   - `TESTING.md` - Testing on real devices
   - `APK_SIGNING.md` - APK signing details

## 💡 Tips for Android Studio

1. **Enable Auto-Import:**
   - File → Settings → Editor → General → Auto Import
   - Check "Add unambiguous imports on the fly"

2. **Increase Memory (if needed):**
   - File → Settings → Appearance & Behavior → System Settings → Memory Settings
   - Increase heap size if building is slow

3. **Enable Offline Mode (after first sync):**
   - File → Settings → Build, Execution, Deployment → Gradle
   - Check "Offline work" (only after successful initial sync)

4. **Useful Shortcuts:**
   - `Ctrl+N` / `Cmd+O` - Open class
   - `Ctrl+Shift+N` / `Cmd+Shift+O` - Open file
   - `Shift+Shift` - Search everywhere
   - `Ctrl+Shift+A` / `Cmd+Shift+A` - Find action

## 🔐 Security Note

This is a forensic evidence collection app. When running:
- Screenshots are prevented (FLAG_SECURE)
- No cloud logging or telemetry
- All data stays on device
- Designed for airgap/offline operation

## 📞 Support

If you encounter issues not covered in this guide:

1. Check GitHub Actions builds to verify project builds successfully:
   - https://github.com/Liamhigh/take2/actions

2. Download pre-built APKs if local build fails:
   ```bash
   ./download-apk.sh
   ```

3. Review existing documentation:
   - `BUILD_STATUS.md` - Latest build information
   - `BUILD_VERIFICATION.md` - Build verification details
   - `BUILD_FIX_SUMMARY.md` - Build fixes and improvements

## ✅ Success Checklist

- [ ] Android Studio Hedgehog or later installed
- [ ] JDK 17 configured
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed successfully
- [ ] No errors in Build output
- [ ] Debug APK builds successfully
- [ ] App runs on emulator or device
- [ ] Tests run successfully

---

**Last Updated:** December 3, 2024  
**Project:** Verum Omnis Forensic Engine  
**Branch:** copilot/fix-gradle-sync-issues  
**Gradle:** 8.9  
**Android Gradle Plugin:** 8.6.1  
**Kotlin:** 2.0.21
