# Build Verification Checklist

This document helps you verify that the Verum Omnis Forensic app is correctly set up and builds successfully in Android Studio.

## ✅ Pre-Build Verification

### Step 1: Project Structure Check

Verify the following files exist:

```bash
# Root project files
✓ build.gradle.kts
✓ settings.gradle.kts
✓ gradle.properties
✓ gradlew
✓ gradle/wrapper/gradle-wrapper.jar
✓ gradle/wrapper/gradle-wrapper.properties

# App module files
✓ app/build.gradle.kts
✓ app/proguard-rules.pro
✓ app/src/main/AndroidManifest.xml

# Source code packages
✓ app/src/main/java/org/verumomnis/forensic/core/
✓ app/src/main/java/org/verumomnis/forensic/crypto/
✓ app/src/main/java/org/verumomnis/forensic/ui/
✓ app/src/main/java/org/verumomnis/forensic/location/
✓ app/src/main/java/org/verumomnis/forensic/pdf/
✓ app/src/main/java/org/verumomnis/forensic/report/
✓ app/src/main/java/org/verumomnis/forensic/custody/
✓ app/src/main/java/org/verumomnis/forensic/verification/
✓ app/src/main/java/org/verumomnis/forensic/leveler/
✓ app/src/main/java/org/verumomnis/forensic/jurisdiction/

# Test files
✓ app/src/test/java/org/verumomnis/forensic/

# Resource files
✓ app/src/main/res/values/strings.xml
✓ app/src/main/res/values/themes.xml
✓ app/src/main/res/xml/network_security_config.xml
✓ app/src/main/res/xml/data_extraction_rules.xml
✓ app/src/main/res/xml/file_paths.xml
```

**Verify in Android Studio:**
1. Open Project view (left sidebar)
2. Expand all folders
3. Confirm all packages are visible
4. No folders show with red underlines

### Step 2: Gradle Configuration Check

#### Root build.gradle.kts
```kotlin
plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

#### settings.gradle.kts
```kotlin
rootProject.name = "VerumOmnisForensic"
include(":app")
```

#### gradle.properties
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

#### gradle-wrapper.properties
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
```

**Verify in Android Studio:**
1. File → Project Structure
2. Project: Gradle Version should be 8.9
3. Modules → app → Properties: Compile SDK should be 34

### Step 3: Manifest Verification

Check `app/src/main/AndroidManifest.xml` contains:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Permissions -->
    ✓ <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    ✓ <uses-permission android:name="android.permission.CAMERA" />
    
    <application
        ✓ android:name=".core.VerumOmnisApplication"
        ✓ android:theme="@style/Theme.VerumOmnisForensic">
        
        <!-- Activities -->
        ✓ <activity android:name=".ui.MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        ✓ <activity android:name=".ui.ScannerActivity" />
        ✓ <activity android:name=".ui.ReportViewerActivity" />
        
        <!-- File Provider -->
        ✓ <provider android:name="androidx.core.content.FileProvider" />
    </application>
</manifest>
```

**Verify in Android Studio:**
1. Open `AndroidManifest.xml`
2. No red underlines on class names
3. No "Cannot resolve symbol" errors
4. All activities are recognized

### Step 4: Dependencies Verification

Check `app/build.gradle.kts` includes all required dependencies:

```kotlin
dependencies {
    // Core Android
    ✓ implementation("androidx.core:core-ktx:1.12.0")
    ✓ implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    ✓ implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    ✓ implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    ✓ implementation("androidx.compose.ui:ui")
    ✓ implementation("androidx.compose.material3:material3")

    // Location
    ✓ implementation("com.google.android.gms:play-services-location:21.0.1")

    // PDF Generation
    ✓ implementation("com.itextpdf:itext7-core:7.2.5")
    ✓ implementation("org.slf4j:slf4j-android:1.7.36")

    // QR Code
    ✓ implementation("com.google.zxing:core:3.5.2")

    // Camera/ML Kit
    ✓ implementation("androidx.camera:camera-core:1.3.1")
    ✓ implementation("com.google.mlkit:text-recognition:16.0.0")

    // Testing
    ✓ testImplementation("junit:junit:4.13.2")
}
```

**Verify in Android Studio:**
1. View → Tool Windows → Build
2. Check for "BUILD SUCCESSFUL" after sync
3. No dependency resolution errors
4. External Libraries shows all dependencies

## 🔨 Build Verification

### Step 5: Gradle Sync

1. **Trigger Sync:**
   - File → Sync Project with Gradle Files
   - Wait for completion (2-5 minutes first time)

2. **Check Output:**
   - Build tab at bottom shows "BUILD SUCCESSFUL"
   - No errors in "Problems" tab
   - Event Log shows successful sync

3. **Verify Dependencies Downloaded:**
   ```
   External Libraries (in Project view) should show:
   ✓ Gradle: androidx.core:core-ktx:1.12.0
   ✓ Gradle: androidx.compose.ui:ui
   ✓ Gradle: com.google.android.gms:play-services-location:21.0.1
   ✓ Gradle: com.itextpdf:itext7-core:7.2.5
   ✓ Gradle: com.google.zxing:core:3.5.2
   ... and many more
   ```

### Step 6: Source Code Compilation Check

1. **Open Key Files:**
   - `MainActivity.kt` - No red underlines
   - `ForensicEngine.kt` - No red underlines
   - `CryptographicSealingEngine.kt` - No red underlines

2. **Check for Errors:**
   - View → Tool Windows → Problems
   - Should show 0 errors
   - Warnings are OK (if any)

3. **Verify Imports:**
   All imports should be recognized (no red underlines):
   ```kotlin
   ✓ import androidx.compose.material3.*
   ✓ import androidx.activity.ComponentActivity
   ✓ import com.google.android.gms.location.*
   ✓ import com.google.zxing.*
   ✓ import org.verumomnis.forensic.core.*
   ```

### Step 7: Build Debug APK

**Command Line:**
```bash
./gradlew assembleDebug
```

**Expected Output:**
```
BUILD SUCCESSFUL in Xs
XX actionable tasks: XX executed
```

**Or in Android Studio:**
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for build to complete
3. Look for success notification

**Verify Output:**
```bash
# Check APK exists
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Should see file ~36 MB
-rw-r--r-- 1 user group 36M Dec 3 12:00 app-debug.apk
```

### Step 8: Run Tests

**Command Line:**
```bash
./gradlew test
```

**Expected Output:**
```
BUILD SUCCESSFUL in Xs
XX tests completed, 0 failed
```

**Or in Android Studio:**
1. Right-click `app/src/test`
2. Select "Run 'Tests in 'test''"
3. All tests should pass (green)

**Verify:**
```bash
# Check test report
open app/build/reports/tests/testDebugUnitTest/index.html

# Should show:
✓ Tests: X
✓ Failures: 0
✓ Success rate: 100%
```

## 🚀 Runtime Verification

### Step 9: Run on Emulator

1. **Create Emulator (if needed):**
   - Tools → Device Manager
   - Create Device → Pixel 6
   - System Image: Android 13 (API 33) or higher
   - Finish

2. **Run App:**
   - Click Run button (▶️)
   - Select emulator
   - Wait for app to launch

3. **Verify App Launches:**
   ✓ App icon appears in emulator
   ✓ "VERUM OMNIS" header visible
   ✓ "Constitution Mode: ACTIVE" badge shown
   ✓ "Create New Case" button visible
   ✓ No crash dialogs

4. **Basic Functionality Test:**
   ✓ Click "Create New Case" → Dialog appears
   ✓ Enter case name → Case created
   ✓ "Current Case" section updates
   ✓ "Add Evidence" button becomes enabled

### Step 10: LogCat Verification

1. **Open LogCat:**
   - View → Tool Windows → Logcat

2. **Check for Init Messages:**
   ```
   ✓ VerumOmnisApp: Verum Omnis Forensic Engine initialized
   ✓ VerumOmnisApp: Version: 5.2.6
   ✓ VerumOmnisApp: Mode: Offline-First, Stateless
   ✓ VerumOmnisApp: Standards: ISO 27037, PDF/A-3B, Daubert
   ```

3. **No Error Messages:**
   - Filter by "Error" level
   - Should be empty or only system errors
   - No app-related crashes

## 📋 Final Checklist

Before considering the setup complete, verify:

### Build Environment
- [x] Android Studio Hedgehog or later installed
- [x] JDK 17 configured
- [x] Android SDK 34 installed
- [x] Gradle 8.9 downloaded

### Project Files
- [x] All source files present
- [x] No missing imports
- [x] No compilation errors
- [x] All tests pass

### Build Outputs
- [x] Debug APK builds successfully
- [x] APK size reasonable (~35-40 MB for debug)
- [x] APK is signed (even debug builds)
- [x] Tests run and pass

### Runtime
- [x] App launches on emulator
- [x] UI renders correctly
- [x] Basic functionality works
- [x] No crashes in LogCat

## 🎉 Success!

If all items above are checked, your setup is complete and working correctly!

## 🐛 Troubleshooting Failed Checks

### If Gradle Sync Fails
→ See [ANDROID_STUDIO_SETUP.md - Troubleshooting](ANDROID_STUDIO_SETUP.md#troubleshooting)

### If Build Fails
1. Clean build: `./gradlew clean`
2. Rebuild: Build → Rebuild Project
3. Check JDK version: File → Project Structure → SDK Location

### If Tests Fail
1. Check test output for specific failure
2. Verify all dependencies downloaded
3. Run with: `./gradlew clean test --stacktrace`

### If App Doesn't Launch
1. Check emulator API level (must be 26+)
2. Check LogCat for crash details
3. Try: Build → Clean Project → Rebuild

### If Import Errors
1. File → Invalidate Caches → Invalidate and Restart
2. File → Sync Project with Gradle Files
3. Close and reopen project

## 📚 Additional Resources

- **Setup Guide:** [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md)
- **Quick Start:** [QUICK_START.md](QUICK_START.md)
- **Testing Guide:** [TESTING.md](TESTING.md)
- **Project README:** [README.md](README.md)

---

**Last Updated:** December 3, 2024  
**Branch:** copilot/fix-gradle-sync-issues  
**Target:** Android Studio Hedgehog+  
**Gradle:** 8.9  
**AGP:** 8.6.1
