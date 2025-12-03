# Quick Start Guide - Verum Omnis Forensic

## 🚀 TL;DR - Get Started in 5 Minutes

### For Developers (Building from Source)

```bash
# 1. Clone the repository
git clone https://github.com/Liamhigh/take2.git
cd take2
git checkout copilot/fix-gradle-sync-issues

# 2. Open in Android Studio
# File → Open → Select the 'take2' folder

# 3. Wait for Gradle sync (2-5 minutes)
# Android Studio will automatically download dependencies

# 4. Build & Run
# Click the green Run button (▶️) or press Shift+F10
```

**Need help?** See [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md) for detailed setup instructions.

### For Testers (Using Pre-built APKs)

```bash
# Download the latest pre-built APK
./download-apk.sh

# Or manually from GitHub Actions:
# https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml
```

**Installation help?** See [TESTING.md](TESTING.md) for detailed installation instructions.

## 📋 Prerequisites Checklist

**For building from source:**
- [ ] Android Studio Hedgehog (2023.1.1) or later
- [ ] JDK 17 (included with Android Studio)
- [ ] Internet connection (for first build only)
- [ ] 4GB+ free disk space
- [ ] 8GB+ RAM recommended

**For testing APKs:**
- [ ] Android device with Android 8.0 (API 26) or higher
- [ ] USB cable (for device installation)
- [ ] "Install from unknown sources" enabled

## 🎯 What This App Does

Verum Omnis is a forensic evidence collection and reporting tool that:

1. **Captures Evidence** - Documents, photos, locations with cryptographic sealing
2. **Maintains Chain of Custody** - Immutable logging of all forensic actions
3. **Generates Reports** - Court-ready PDF reports with SHA-512 verification
4. **Works Offline** - No cloud, no telemetry, airgap ready

## 🏗️ Project Structure

```
app/src/main/java/org/verumomnis/forensic/
├── core/              # ForensicEngine, ForensicEvidence, Application
├── crypto/            # SHA-512 hashing, HMAC-SHA512 sealing
├── location/          # GPS forensic location capture
├── pdf/               # PDF/A-3B report generation
├── report/            # Forensic narrative generation
├── custody/           # Chain of custody logging
├── verification/      # Offline verification tools
├── leveler/           # Contradiction detection engine
├── jurisdiction/      # Multi-jurisdiction compliance
└── ui/                # Jetpack Compose UI (MainActivity, etc.)
```

## 🔧 Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Generate test coverage report
./gradlew testDebugUnitTest jacocoTestReport

# Clean build
./gradlew clean build
```

## 📱 Running the App

### On Physical Device
1. Enable Developer Options on your device
2. Enable USB Debugging
3. Connect via USB
4. Click Run (▶️) in Android Studio
5. Select your device

### On Emulator
1. Tools → Device Manager
2. Create/Start an emulator (API 26+)
3. Click Run (▶️) in Android Studio
4. Select the emulator

## 🧪 Testing

```bash
# Run all unit tests
./gradlew test

# Run specific test
./gradlew test --tests CryptographicSealingEngineTest

# View results
open app/build/reports/tests/testDebugUnitTest/index.html
```

## 📊 Key Features

### Cryptographic Sealing
- **SHA-512** content hashing
- **HMAC-SHA512** seal generation
- **Triple Hash Layer** for forensic integrity

### Chain of Custody
- Immutable append-only logging
- Hash chain for tamper detection
- Compliant with ISO 27037

### PDF Reports
- **PDF/A-3B** archival format
- Watermarked with "VERUM OMNIS FORENSIC SEAL"
- QR code for hash verification
- Court-ready formatting

### Offline-First
- No cloud logging
- No telemetry
- Airgap ready
- 100% offline operation

## 🛠️ Common Issues

### "Plugin not found" error
**Fix:** Check internet connection, wait for Gradle sync to complete

### "SDK location not found"
**Fix:** File → Project Structure → SDK Location → Set Android SDK path

### Build fails
**Fix:** Build → Clean Project, then Build → Rebuild Project

### Missing dependencies
**Fix:** File → Sync Project with Gradle Files

**More help:** See [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md#troubleshooting)

## 📚 Documentation

- **[ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md)** - Detailed Android Studio setup
- **[README.md](README.md)** - Project overview and features
- **[TESTING.md](TESTING.md)** - APK installation and testing
- **[APK_SIGNING.md](APK_SIGNING.md)** - APK signing information

## 🎓 Learning the Codebase

**Start here:**
1. `MainActivity.kt` - Main UI and app entry point
2. `ForensicEngine.kt` - Core forensic logic
3. `CryptographicSealingEngine.kt` - Cryptographic sealing
4. `ForensicPdfGenerator.kt` - PDF report generation

**Key concepts:**
- Evidence sealing with SHA-512 and HMAC
- Chain of custody logging
- Offline verification
- Multi-jurisdiction compliance

## 🔐 Security Principles

This app follows strict security principles:

- ✅ **Offline-First** - No internet required
- ✅ **Stateless** - No persistent user data beyond cases
- ✅ **No Cloud Logging** - Everything stays local
- ✅ **No Telemetry** - No tracking or analytics
- ✅ **Airgap Ready** - Works in isolated environments
- ✅ **Screenshot Protection** - FLAG_SECURE prevents screenshots

## 📞 Getting Help

1. **Build issues?** → [ANDROID_STUDIO_SETUP.md](ANDROID_STUDIO_SETUP.md#troubleshooting)
2. **Installation issues?** → [TESTING.md](TESTING.md)
3. **Can't build locally?** → Use pre-built APKs from GitHub Actions
4. **Need source code help?** → Check inline documentation in source files

## ✅ Quick Verification

After setup, verify everything works:

```bash
# 1. Clean build
./gradlew clean

# 2. Run tests
./gradlew test

# 3. Build debug APK
./gradlew assembleDebug

# 4. Check output
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

If all commands succeed, you're ready to develop! 🎉

## 🚀 Next Steps

1. **Read the README** - Understand the forensic principles
2. **Explore the code** - Start with MainActivity.kt
3. **Run tests** - See how components are tested
4. **Build APK** - Create your first build
5. **Test on device** - See the app in action

---

**Questions?** Check the detailed guides linked above or review the inline code documentation.
