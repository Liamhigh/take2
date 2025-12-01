# How to Get the Verum Omnis App

Since the build environment has restricted network access (Google Maven repository is blocked), there are two ways to get the APK:

## Option 1: Download Pre-built APK from GitHub Actions (RECOMMENDED)

### Quick Method (Using Helper Script):

We provide a helper script that automates the download:

```bash
./download-apk.sh
```

This requires the [GitHub CLI](https://cli.github.com/) to be installed.

### Manual Method:

1. **Go to the GitHub Actions page:**
   https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml

2. **Find a successful build** (green checkmark ✓)

3. **Click on the build run**

4. **Scroll down to "Artifacts" section**

5. **Download one of:**
   - `verum-omnis-debug-apk.zip` - Debug version (for testing, with debugging enabled)
   - `verum-omnis-release-apk.zip` - Release version (optimized, recommended for use)

6. **Extract the ZIP file** to get the APK

7. **Install the APK** on your Android device

### Latest Successful Build
**Note:** Build information below may be outdated. Always check the [GitHub Actions page](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml) for the most recent successful build.

- **Build Run:** #96 (as of 2025-12-01)
- **Commit:** f4e76a2708801b27dde3b1bef2ac6db95833a3ea
- **APKs Retained:** 30 days from build date

## Option 2: Build Locally (Requires Network Access)

Building locally requires access to `dl.google.com` (Google's Maven repository).

If you have the required network access:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease
```

The APKs will be output to `app/build/outputs/apk/`

### Network Requirements
- ✅ `github.com` - Repository access
- ✅ `repo1.maven.org` - Maven Central
- ✅ `plugins.gradle.org` - Gradle plugins
- ⚠️ `dl.google.com` - **REQUIRED** Google Maven (Android dependencies)

If `dl.google.com` is blocked, see [BUILD_TROUBLESHOOTING.md](BUILD_TROUBLESHOOTING.md) for more details.

## Installation Instructions

### From Windows/Mac/Linux Computer:
1. Download the APK file
2. Connect your Android device via USB
3. Enable "Developer Options" and "USB Debugging" on your Android device
4. Use `adb install <apk-file>` to install

### Directly on Android Device:
1. Download the APK file to your device
2. Enable "Install from Unknown Sources" in Settings → Security
3. Open the APK file to install
4. Grant any requested permissions

## Verification

After installation, the app should appear as "Verum Omnis" with the forensic shield icon.

**First Launch:**
- The app will request permissions for:
  - Camera (for document scanning)
  - Location (for GPS evidence tagging)
  - Storage (for saving evidence and reports)
- These permissions are required for forensic evidence collection

## Troubleshooting

**"Can't get into the app":**
- Make sure you downloaded and extracted the APK from GitHub Actions
- Check that your Android version is 8.0 (API 26) or higher
- Ensure you granted the necessary permissions

**Build fails with plugin errors:**
- This is expected if `dl.google.com` is blocked
- Use the pre-built APK from GitHub Actions instead
- See [BUILD_TROUBLESHOOTING.md](BUILD_TROUBLESHOOTING.md) for details

## App Information

- **Package ID:** org.verumomnis.forensic
- **Min Android:** 8.0 (API 26)
- **Target Android:** 14 (API 34)
- **Version:** 1.0.0
- **Architecture:** Universal (all architectures)

## Security Note

This is a forensic evidence application that operates with:
- Offline-first design (no cloud logging)
- Screenshot prevention (FLAG_SECURE)
- Cryptographic evidence sealing (SHA-512 + HMAC)
- GPS location capture
- Chain of custody logging

For more information, see the main [README.md](README.md).
