# Quick Troubleshooting Guide - Android Studio Build Issues

This guide provides quick solutions for common build issues when opening this branch in Android Studio.

## Table of Contents

- [Gradle Sync Issues](#gradle-sync-issues)
- [Missing SDK](#missing-sdk)
- [Import Errors](#import-errors)
- [Manifest Conflicts](#manifest-conflicts)
- [Quick Fixes](#quick-fixes)

---

## Gradle Sync Issues

### Issue: "Plugin [id: 'com.android.application'] was not found"

**Cause:** Android Gradle Plugin cannot be downloaded

**Solution:**
1. Check internet connection
2. Verify you're not behind a restrictive firewall
3. In Android Studio: File → Settings → Appearance & Behavior → System Settings → HTTP Proxy
4. Configure proxy if needed, or select "No proxy" if you have direct internet access

**Alternative:**
- Download pre-built APKs from GitHub Actions (see [TESTING.md](TESTING.md))

### Issue: "Could not resolve dependencies"

**Solution:**
```
File → Invalidate Caches → Invalidate and Restart
```

Then sync again:
```
File → Sync Project with Gradle Files
```

---

## Missing SDK

### Issue: "SDK location not found"

**Solution 1: Let Android Studio detect it**
1. File → Settings → Appearance & Behavior → System Settings → Android SDK
2. Note the SDK location
3. Click "Apply"

**Solution 2: Create local.properties manually**
1. Copy `local.properties.template` to `local.properties`
2. Edit `local.properties` and set your SDK path:

**macOS:**
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

**Linux:**
```properties
sdk.dir=/home/YOUR_USERNAME/Android/Sdk
```

**Windows:**
```properties
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

### Issue: "Android SDK Platform 34 not found"

**Solution:**
1. File → Settings → Appearance & Behavior → System Settings → Android SDK
2. Check "SDK Platforms" tab
3. Check "Android 14.0 (API 34)"
4. Click "Apply" to download

---

## Import Errors

### Issue: "Cannot resolve symbol" after sync

**Solution:**
```
1. Build → Clean Project
2. Build → Rebuild Project
3. File → Sync Project with Gradle Files
```

If still failing:
```
File → Invalidate Caches → Invalidate and Restart
```

### Issue: Kotlin imports not resolving

**Solution:**
1. File → Settings → Plugins
2. Verify "Kotlin" plugin is enabled
3. If disabled, enable it and restart Android Studio

---

## Manifest Conflicts

### Issue: "Manifest merger failed"

**This project should NOT have manifest conflicts.** All required resources are present:

✅ `app/src/main/AndroidManifest.xml`  
✅ All referenced resources exist:
  - `@string/app_name` → `app/src/main/res/values/strings.xml`
  - `@style/Theme.VerumOmnisForensic` → `app/src/main/res/values/themes.xml`
  - `@mipmap/ic_launcher` → `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - `@xml/data_extraction_rules` → `app/src/main/res/xml/data_extraction_rules.xml`
  - `@xml/network_security_config` → `app/src/main/res/xml/network_security_config.xml`
  - `@xml/file_paths` → `app/src/main/res/xml/file_paths.xml`

**If you see manifest conflicts:**
1. Check you're on the correct branch: `copilot/fix-gradle-sync-issues-again`
2. Try: `Build → Clean Project`
3. Verify no local changes: `git status`

---

## Quick Fixes

### Clean Build
```
Build → Clean Project
Build → Rebuild Project
```

### Reset Gradle
```
File → Invalidate Caches → Invalidate and Restart
```

Then:
```
File → Sync Project with Gradle Files
```

### Check Java/JDK Version
```
File → Settings → Build, Execution, Deployment → Build Tools → Gradle
```

Ensure "Gradle JDK" is set to **JDK 17** (required for this project)

### Verify Gradle Wrapper
The project includes a Gradle wrapper, so you don't need to install Gradle separately.

Verify it's present:
- `gradlew` (Linux/macOS)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

### Re-download Dependencies
If Gradle dependencies are corrupted:

**Option 1: Via Android Studio**
```
File → Invalidate Caches → Invalidate and Restart
```

**Option 2: Via Command Line**
```bash
./gradlew clean build --refresh-dependencies
```

---

## Still Having Issues?

### Check Project Configuration

**Expected configuration:**
- **Gradle:** 8.9 (auto-downloaded by wrapper)
- **Android Gradle Plugin:** 8.6.1
- **Kotlin:** 2.0.21
- **Compile SDK:** 34
- **Min SDK:** 26
- **Target SDK:** 34
- **JDK:** 17

### Verify Files

Run this in your project directory:

```bash
# Check all key files exist
ls -l build.gradle.kts
ls -l settings.gradle.kts
ls -l app/build.gradle.kts
ls -l app/src/main/AndroidManifest.xml
ls -l gradle/wrapper/gradle-wrapper.properties
```

All should exist without errors.

### Get Pre-built APKs

If you cannot resolve build issues:

1. Visit: https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml
2. Click the latest successful run (green checkmark)
3. Download artifacts from the bottom of the page
4. See [TESTING.md](TESTING.md) for installation instructions

Or use the download script:
```bash
./download-apk.sh
```

---

## Useful Commands

### Check Current Branch
```bash
git branch --show-current
```
Should show: `copilot/fix-gradle-sync-issues-again`

### View Gradle Tasks
```bash
./gradlew tasks
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
```

---

## Getting Help

For comprehensive setup instructions, see:
- **[ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md)** - Complete setup guide
- **[README.md](README.md)** - Project overview
- **[TESTING.md](TESTING.md)** - Testing and installation guide

**Android Studio Documentation:**
- https://developer.android.com/studio/intro
- https://developer.android.com/studio/build/building-cmdline

---

## Environment Note

⚠️ **Important:** This project builds successfully in standard Android Studio installations. The CI/CD sandbox environment has network restrictions that do not apply to local development.

**In Android Studio, you should have:**
- ✅ Full access to Google Maven repository
- ✅ Full access to Maven Central
- ✅ Full access to Gradle Plugin Portal
- ✅ No build issues related to dependency downloads
