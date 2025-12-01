# Getting Started with Verum Omnis Forensic Engine

## Important: Build Restrictions

**⚠️ This repository cannot be built locally due to network restrictions.**

The build environment blocks access to `dl.google.com` (Google's Maven repository), which is required to download the Android Gradle Plugin and other Android dependencies. This is a known limitation of the current environment.

## Solution: Use Pre-Built APKs

Instead of building locally, you can download pre-built APKs from GitHub Actions:

### Option 1: Download APK Script (Recommended)

Use the provided script to automatically download the latest APK:

```bash
./download-apk.sh
```

This script requires:
- `gh` (GitHub CLI) - Install from https://cli.github.com/
- `jq` - JSON processor (usually available via package manager)

### Option 2: Manual Download

1. Go to the [Actions tab](https://github.com/Liamhigh/take2/actions)
2. Click on the latest successful "Build APK" workflow run
3. Scroll down to "Artifacts"
4. Download the APK artifact for your desired build type (debug or release)

## Installation

Once you have downloaded the APK:

1. Enable "Install from Unknown Sources" on your Android device
2. Transfer the APK to your device
3. Install the APK

## Build Types

- **Debug APK**: For development and testing (signed with debug key)
- **Release APK**: For production use (signed with debug key for open-source distribution)

## For Contributors

If you need to modify the code:

1. Make your changes to the source code
2. Push your changes to GitHub
3. The GitHub Actions workflow will automatically build the APK
4. Download the APK from the Actions artifacts

## Alternative: Build on Different Environment

If you have access to an environment without network restrictions:

1. Clone this repository
2. Ensure you have:
   - Android Studio Hedgehog or later
   - JDK 17
   - Android SDK 34
3. Run `./gradlew assembleDebug` or `./gradlew assembleRelease`

## Questions?

See [BUILD_TROUBLESHOOTING.md](BUILD_TROUBLESHOOTING.md) for detailed troubleshooting steps and common issues.
