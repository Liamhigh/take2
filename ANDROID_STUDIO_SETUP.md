# Android Studio Setup Guide

This guide provides step-by-step instructions for cloning and building the Verum Omnis Forensic Engine in Android Studio.

## Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio** (Latest stable version recommended)
   - Download from: https://developer.android.com/studio
   - Minimum version: Android Studio Hedgehog (2023.1.1) or later

2. **Android SDK** with the following components:
   - Android SDK Platform 34 (Android 14)
   - Android SDK Build-Tools 34.0.0 or later
   - Android SDK Platform-Tools
   - Android Emulator (optional, for testing)

3. **Java Development Kit (JDK)**
   - JDK 17 (required for this project)
   - Android Studio typically bundles this, but you can verify in Settings → Build, Execution, Deployment → Build Tools → Gradle

4. **Git** (for cloning the repository)
   - Download from: https://git-scm.com/downloads

## Clean Clone and Build Steps

### Step 1: Clone the Repository

Open a terminal or command prompt and run:

```bash
git clone https://github.com/Liamhigh/take2.git
cd take2
git checkout copilot/fix-gradle-sync-issues-again
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **"Open"** from the welcome screen (or File → Open if you have a project open)
3. Navigate to the cloned repository directory (`take2`)
4. Click **"OK"** to open the project

### Step 3: Configure Android SDK

When you first open the project, Android Studio will detect it's an Android project and may prompt you to:

1. **Accept the Android Gradle Plugin version**
   - Click "OK" or "Accept" when prompted

2. **Download missing SDK components**
   - Android Studio will automatically detect missing SDK platforms and tools
   - Click "Install Missing Components" when prompted
   - This may include:
     - Android SDK Platform 34
     - Build Tools
     - Other required components

3. **Wait for Gradle Sync**
   - Android Studio will automatically sync the Gradle project
   - This may take several minutes on the first run as it downloads dependencies
   - Progress will be shown in the status bar at the bottom

### Step 4: Verify Gradle Sync

Once Gradle sync completes successfully, you should see:

- ✅ "Gradle sync finished" message in the Build window
- ✅ No errors in the "Build" tab at the bottom
- ✅ Project structure visible in the Project pane on the left

If you encounter any sync errors, see the **Troubleshooting** section below.

### Step 5: Build the Project

There are several ways to build:

**Option A: Build APK (Debug)**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
- Output location: `app/build/outputs/apk/debug/app-debug.apk`

**Option B: Build APK (Release)**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
- Then select the release variant from the Build Variants panel
- Output location: `app/build/outputs/apk/release/app-release-unsigned.apk`

**Option C: Run on Device/Emulator**
```
Run → Run 'app' (or press Shift+F10)
```
- This builds and installs the app on a connected device or emulator

### Step 6: Run the Application

1. **Connect a Physical Device:**
   - Enable Developer Options and USB Debugging on your Android device
   - Connect via USB
   - Select your device from the device dropdown in the toolbar

2. **Or Use an Emulator:**
   - Tools → Device Manager
   - Create a new virtual device (API 26 or higher recommended)
   - Select the emulator from the device dropdown

3. **Click the Run button (▶)** or press **Shift+F10**

The app should install and launch automatically.

## Project Structure

```
take2/
├── app/                          # Main application module
│   ├── build.gradle.kts         # App-level Gradle configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/org/verumomnis/forensic/
│   │   │   │   ├── core/        # Core forensic engine
│   │   │   │   ├── crypto/      # Cryptographic sealing
│   │   │   │   ├── custody/     # Chain of custody
│   │   │   │   ├── jurisdiction/ # Legal compliance
│   │   │   │   ├── leveler/     # Data normalization
│   │   │   │   ├── location/    # GPS services
│   │   │   │   ├── pdf/         # PDF generation
│   │   │   │   ├── report/      # Report generation
│   │   │   │   ├── ui/          # User interface
│   │   │   │   └── verification/ # Offline verification
│   │   │   └── res/             # Resources (layouts, strings, etc.)
│   │   └── test/                # Unit tests
├── build.gradle.kts             # Project-level Gradle configuration
├── settings.gradle.kts          # Gradle settings
├── gradle.properties            # Gradle properties
└── gradlew                      # Gradle wrapper script
```

## Key Configuration Files

### build.gradle.kts (Project Level)
```kotlin
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

### build.gradle.kts (App Level)
```kotlin
android {
    namespace = "org.verumomnis.forensic"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "org.verumomnis.forensic"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

## Troubleshooting

### Issue: Gradle Sync Fails

**Symptom:** "Could not resolve dependencies" or "Plugin not found" errors

**Solution:**
1. Check your internet connection
2. Verify you're not behind a restrictive firewall that blocks Maven repositories
3. Try: File → Invalidate Caches → Invalidate and Restart
4. Ensure Android SDK is properly installed:
   - File → Settings → Appearance & Behavior → System Settings → Android SDK
   - Verify SDK Platform 34 is installed

### Issue: SDK Not Found

**Symptom:** "SDK location not found" error

**Solution:**
1. Open File → Settings → Appearance & Behavior → System Settings → Android SDK
2. Note the SDK location path
3. Create a `local.properties` file in the project root with:
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   ```
   - On macOS: `/Users/[username]/Library/Android/sdk`
   - On Linux: `/home/[username]/Android/Sdk`
   - On Windows: `C\:\\Users\\[username]\\AppData\\Local\\Android\\Sdk`

### Issue: Build Fails with "Could not download Google Maven dependencies"

**Symptom:** Build fails when trying to download Android libraries

**Solution:**
- This should not happen in Android Studio as it has built-in repository access
- If you're in a restricted network environment, you may need to:
  1. Configure proxy settings: File → Settings → Appearance & Behavior → System Settings → HTTP Proxy
  2. Or use the pre-built APKs from GitHub Actions (see TESTING.md)

### Issue: JDK Version Mismatch

**Symptom:** "Unsupported class file major version" or JDK version errors

**Solution:**
1. File → Settings → Build, Execution, Deployment → Build Tools → Gradle
2. Set "Gradle JDK" to JDK 17
3. If JDK 17 is not available:
   - Click "Download JDK"
   - Select version 17 from a vendor like Oracle or Eclipse Temurin

### Issue: Missing Dependencies

**Symptom:** "Cannot resolve symbol" errors in Kotlin files

**Solution:**
1. Build → Clean Project
2. Build → Rebuild Project
3. File → Sync Project with Gradle Files
4. If still failing, File → Invalidate Caches → Invalidate and Restart

### Issue: Kotlin Compiler Errors

**Symptom:** Kotlin compilation errors after sync

**Solution:**
1. Verify Kotlin plugin is enabled: File → Settings → Plugins → Kotlin (should be checked)
2. Check Kotlin version matches in build.gradle.kts (currently 2.0.21)
3. Try: Build → Clean Project, then Build → Rebuild Project

## Running Tests

### Unit Tests

Run all unit tests:
```
Run → Run 'All Tests'
```

Or run specific test files from the Project pane:
- Navigate to `app/src/test/java/org/verumomnis/forensic/`
- Right-click on a test file
- Select "Run [TestName]"

### Test Coverage

Generate test coverage report:
```bash
./gradlew jacocoTestReport
```
- Report location: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## Additional Resources

- **Main Documentation:** [README.md](README.md)
- **Testing Guide:** [TESTING.md](TESTING.md)
- **Build Status:** [BUILD_STATUS.md](BUILD_STATUS.md)
- **Pre-built APKs:** Use `./download-apk.sh` or see [TESTING.md](TESTING.md)

## Quick Reference: Common Tasks

| Task | Command/Action |
|------|----------------|
| Open Android SDK Manager | Tools → SDK Manager |
| Open Device Manager | Tools → Device Manager |
| Sync Gradle | File → Sync Project with Gradle Files |
| Clean Build | Build → Clean Project |
| Rebuild | Build → Rebuild Project |
| Run App | Run → Run 'app' (Shift+F10) |
| Build APK | Build → Build Bundle(s) / APK(s) → Build APK(s) |
| View Build Output | View → Tool Windows → Build |
| View Logcat | View → Tool Windows → Logcat |

## Minimum System Requirements

- **Operating System:** Windows 10/11, macOS 10.14+, or Linux (64-bit)
- **RAM:** 8 GB minimum (16 GB recommended)
- **Disk Space:** 8 GB minimum for Android Studio + SDK
- **Screen Resolution:** 1280x800 minimum

## Need Help?

If you encounter issues not covered in this guide:

1. Check the official Android Studio documentation: https://developer.android.com/studio/intro
2. Verify all prerequisites are correctly installed
3. Try a clean rebuild: Build → Clean Project, then Build → Rebuild Project
4. Check for Android Studio updates: Help → Check for Updates

---

**Note:** This project is configured to build successfully in Android Studio with standard settings. The sandbox environment used for CI/CD has network restrictions that don't apply to your local Android Studio installation.
