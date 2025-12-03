# Clean Clone and Build Steps for Android Studio

This document provides the exact steps to clone this branch and open it in Android Studio for a clean build.

## 🎯 Goal

Get the Verum Omnis Forensic app running in Android Studio with zero Gradle sync issues, no missing imports, and no manifest conflicts.

## ✅ Prerequisites

Before you begin, ensure you have:

1. **Android Studio Hedgehog (2023.1.1) or later**
   - Download: https://developer.android.com/studio
   
2. **Git** (for cloning the repository)
   - Download: https://git-scm.com/downloads
   
3. **Internet connection** (for downloading Gradle and dependencies)

4. **Minimum 8GB RAM** and **4GB free disk space**

## 📋 Step-by-Step Instructions

### Step 1: Clone the Repository

Open a terminal and run:

```bash
# Clone the repository
git clone https://github.com/Liamhigh/take2.git

# Navigate to the project directory
cd take2

# Checkout this branch
git checkout copilot/fix-gradle-sync-issues
```

**Verification:**
```bash
# Verify you're on the correct branch
git branch
# Should show: * copilot/fix-gradle-sync-issues

# Verify files are present
ls -la
# Should see: build.gradle.kts, settings.gradle.kts, app/, gradle/, etc.
```

### Step 2: Open in Android Studio

1. **Launch Android Studio**
   - If this is first launch, complete the setup wizard

2. **Open the Project**
   - Click **"Open"** on the welcome screen
   - Or: File → Open (if Android Studio is already open)

3. **Select Project Directory**
   - Navigate to the `take2` folder you just cloned
   - Select the folder (should contain `build.gradle.kts`)
   - Click **"OK"**

4. **Trust the Project**
   - When prompted "Trust and Open Project?", click **"Trust Project"**

### Step 3: Initial Gradle Sync (Automatic)

Android Studio will automatically start syncing. You'll see:

**Progress indicators:**
- Bottom status bar: "Gradle Build Running..."
- Event Log (bottom right): Sync progress
- Build output window: Download progress

**What's happening:**
1. Downloading Gradle 8.9 (~100 MB)
2. Downloading Android Gradle Plugin 8.6.1
3. Downloading Kotlin 2.0.21
4. Downloading all dependencies (~500 MB total)
5. Indexing project files

**Expected duration:**
- First time: 3-7 minutes (depending on internet speed)
- Subsequent syncs: 10-30 seconds

**Success indicators:**
✅ Status bar shows "Gradle sync finished"  
✅ Build window shows "BUILD SUCCESSFUL"  
✅ No errors in "Problems" tab  
✅ Project structure appears in left sidebar  
✅ Green Run button (▶️) becomes active

### Step 4: Verify Successful Sync

Check the following to confirm everything is working:

#### 4.1 Project Structure Visible
- Left sidebar shows expanded project tree
- `app` module is visible
- `java/org/verumomnis/forensic/` packages are visible

#### 4.2 No Sync Errors
- View → Tool Windows → Build
- Should show "BUILD SUCCESSFUL"
- No red error messages

#### 4.3 Dependencies Downloaded
- Expand "External Libraries" in Project view
- Should see hundreds of libraries including:
  - androidx.core:core-ktx
  - androidx.compose.ui
  - com.google.android.gms:play-services-location
  - com.itextpdf:itext7-core

#### 4.4 No Import Errors
- Open `app/src/main/java/org/verumomnis/forensic/ui/MainActivity.kt`
- No red underlines on imports
- No "Cannot resolve symbol" errors

### Step 5: Build the App

#### Option A: Build via Android Studio GUI

1. **Select Build Type:**
   - View → Tool Windows → Build Variants
   - Ensure "debug" is selected for the app module

2. **Build APK:**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for build (30-60 seconds)
   - Success notification appears

3. **Locate APK:**
   - Click "locate" in success notification
   - Or navigate to: `app/build/outputs/apk/debug/app-debug.apk`

#### Option B: Build via Command Line

Open the Terminal in Android Studio (View → Tool Windows → Terminal):

```bash
# Build debug APK
./gradlew assembleDebug

# Or build release APK
./gradlew assembleRelease
```

**Expected output:**
```
BUILD SUCCESSFUL in 45s
48 actionable tasks: 48 executed
```

**APK Location:**
- Debug: `app/build/outputs/apk/debug/app-debug.apk` (~36 MB)
- Release: `app/build/outputs/apk/release/app-release.apk` (~24 MB)

### Step 6: Run on Device or Emulator

#### On Physical Device:

1. **Prepare Device:**
   - Enable Developer Options (tap Build Number 7 times in Settings)
   - Enable USB Debugging in Developer Options
   - Connect device via USB
   - Allow debugging when prompted on device

2. **Run App:**
   - Click green Run button (▶️) in toolbar
   - Select your device from dropdown
   - Wait for installation and launch

#### On Emulator:

1. **Create Emulator:**
   - Tools → Device Manager
   - Click "Create Device"
   - Select "Pixel 6" (or any device)
   - Select "Tiramisu" (API 33) or "UpsideDownCake" (API 34)
   - Click "Finish"

2. **Run App:**
   - Click green Run button (▶️)
   - Select the emulator
   - Wait for emulator to boot and app to launch

#### Verify App Runs:

When app launches, you should see:
- ✅ "VERUM OMNIS" title at top
- ✅ "Forensic Evidence Engine" subtitle
- ✅ "Constitution Mode: ACTIVE" badge
- ✅ "Create New Case" button
- ✅ Forensic standards information at bottom
- ✅ Version number displayed

### Step 7: Run Tests (Optional)

Verify code quality by running the test suite:

#### Via Android Studio:
1. Right-click on `app/src/test` in Project view
2. Select "Run 'Tests in 'test''"
3. Wait for tests to complete
4. All tests should pass (green checkmarks)

#### Via Command Line:
```bash
./gradlew test
```

**Expected output:**
```
BUILD SUCCESSFUL in 12s
5 tests completed, 0 failed, 0 skipped
```

**View test report:**
```bash
# HTML report
open app/build/reports/tests/testDebugUnitTest/index.html
```

## ✅ Success Checklist

Your setup is complete when you can check all these boxes:

- [x] Repository cloned successfully
- [x] Android Studio opened the project
- [x] Gradle sync completed with "BUILD SUCCESSFUL"
- [x] Project structure visible in sidebar
- [x] No errors in Build output
- [x] No import errors when opening Kotlin files
- [x] Debug APK builds successfully
- [x] App runs on emulator or device
- [x] All tests pass
- [x] App UI displays correctly

## 🚨 Troubleshooting

### Problem: "Plugin [id: 'com.android.application'] was not found"

**Cause:** Cannot reach Google Maven repository

**Solutions:**
1. Check internet connection
2. Check firewall/antivirus settings
3. Configure proxy if on corporate network:
   - File → Settings → HTTP Proxy
4. Retry: File → Sync Project with Gradle Files

### Problem: "SDK location not found"

**Cause:** Android SDK not installed or not configured

**Solutions:**
1. File → Project Structure → SDK Location
2. Set Android SDK location (usually `~/Library/Android/sdk` on Mac, `C:\Users\<user>\AppData\Local\Android\Sdk` on Windows)
3. If SDK not installed: Tools → SDK Manager → Install

### Problem: "Could not resolve dependencies"

**Cause:** Dependency download failed

**Solutions:**
1. Check internet connection
2. Try again: Build → Clean Project, then Build → Rebuild Project
3. Enable verbose mode: File → Settings → Build → Gradle → Command-line Options: `--stacktrace --info`

### Problem: "Manifest merger failed"

**Cause:** Should not happen with this project, but if it does:

**Solutions:**
1. Check you're on the correct branch: `git branch`
2. Ensure clean clone: No local modifications
3. Check Build output for specific conflict details
4. This branch has been verified to have no manifest conflicts

### Problem: Import errors in Kotlin files

**Cause:** Incomplete Gradle sync or corrupted cache

**Solutions:**
1. File → Sync Project with Gradle Files
2. File → Invalidate Caches → Invalidate and Restart
3. Build → Clean Project
4. If still failing, close Android Studio, delete `.idea` and `.gradle` folders, reopen

### Problem: Build succeeds but app crashes on launch

**Cause:** Emulator/device incompatibility

**Solutions:**
1. Ensure device/emulator is API 26 or higher
2. Check LogCat for crash details (View → Tool Windows → Logcat)
3. Try on a different emulator
4. Verify APK is signed: It should be automatically

## 📊 Expected Build Results

After successful build, you should see:

```
Project: VerumOmnisForensic
Module: app
Build Variant: debug (or release)

APK Details:
- Size: ~36 MB (debug) or ~24 MB (release)
- Min SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Signed: Yes (debug keystore)

Features:
- 15 Kotlin source files
- 5 unit test files
- 10 forensic modules
- 3 Activities
- Jetpack Compose UI
```

## 🎯 What You Can Do Now

With a successful build, you can:

1. **Develop:**
   - Modify Kotlin code
   - Add new features
   - Refactor existing code

2. **Test:**
   - Run unit tests
   - Write new tests
   - Generate coverage reports

3. **Debug:**
   - Set breakpoints
   - Step through code
   - Inspect variables

4. **Build APKs:**
   - Debug APKs for testing
   - Release APKs for distribution
   - Signed APKs for production

## 📚 Next Steps

Now that your build is working, explore:

1. **[QUICK_START.md](QUICK_START.md)** - Quick reference guide
2. **[BUILD_VERIFICATION_CHECKLIST.md](BUILD_VERIFICATION_CHECKLIST.md)** - Comprehensive verification
3. **[README.md](README.md)** - Project overview
4. **[TESTING.md](TESTING.md)** - APK installation and testing

## 📞 Additional Help

- **Build issues?** See [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md) for detailed troubleshooting
- **Can't build locally?** Download pre-built APKs from [GitHub Actions](https://github.com/Liamhigh/take2/actions)
- **Need APK verification?** See [APK_SIGNING.md](APK_SIGNING.md)

## 🎉 Summary

If you followed all steps above and everything worked, congratulations! 🎉

You now have:
- ✅ A clean clone of the repository
- ✅ Android Studio configured correctly
- ✅ All dependencies downloaded
- ✅ Zero Gradle sync issues
- ✅ No missing imports
- ✅ No manifest conflicts
- ✅ A working debug APK
- ✅ A fully functional development environment

**You're ready to develop, test, and build the Verum Omnis Forensic app!**

---

**Branch:** copilot/fix-gradle-sync-issues  
**Last Updated:** December 3, 2024  
**Gradle:** 8.9  
**Android Gradle Plugin:** 8.6.1  
**Kotlin:** 2.0.21  
**Min SDK:** 26  
**Target SDK:** 34
