# Deployment Guide - Verum Omnis Forensic Engine

> **Version**: 1.0.0  
> **Last Updated**: 2025-12-07  
> **Target Audience**: DevOps, System Administrators, Deployment Engineers

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Prerequisites](#prerequisites)
3. [Deployment Options](#deployment-options)
4. [Production Keystore Setup](#production-keystore-setup)
5. [Google Play Store Deployment](#google-play-store-deployment)
6. [Enterprise Distribution](#enterprise-distribution)
7. [Direct Distribution](#direct-distribution)
8. [Verification & Testing](#verification--testing)
9. [Troubleshooting](#troubleshooting)
10. [Rollback Procedures](#rollback-procedures)

---

## 🚀 Quick Start

### Get APKs (Immediate)

The fastest way to get production-ready APKs:

```bash
# Option 1: Use download script
./download-apk.sh

# Option 2: Download from GitHub Actions
# Visit: https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml
# Download latest successful run artifacts
```

**APKs are ready to install immediately** ✅

---

## 📦 Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| **JDK** | 17+ | Building from source |
| **Android SDK** | API 34 | Building from source |
| **Gradle** | 8.9 | Build automation |
| **Git** | 2.x | Version control |
| **keytool** | (included in JDK) | Keystore management |

### Optional Tools

| Tool | Purpose |
|------|---------|
| **Android Studio** | IDE for development |
| **apksigner** | APK signature verification |
| **GitHub CLI (gh)** | Downloading artifacts |
| **jq** | JSON processing for scripts |

### System Requirements

**Build Machine**:
- CPU: 4+ cores
- RAM: 8 GB minimum, 16 GB recommended
- Disk: 10 GB free space
- OS: Linux, macOS, or Windows
- Network: Access to maven.google.com and dl.google.com

**Android Devices** (for testing):
- Android 8.0 (API 26) or higher
- 100 MB free storage
- GPS capability (optional)
- Camera (optional)

---

## 🎯 Deployment Options

### Option 1: GitHub Actions (Recommended) ✅

**Best for**: Automated builds, CI/CD, team collaboration

**Advantages**:
- ✅ Zero local setup required
- ✅ Automatic builds on every commit
- ✅ Test execution included
- ✅ APK artifacts available for 30 days
- ✅ Consistent build environment

**How to use**:
1. Push code to GitHub
2. GitHub Actions automatically builds APKs
3. Download APKs from workflow artifacts
4. Install on devices

**Status**: ✅ **Already configured and working**

### Option 2: Local Build

**Best for**: Development, testing, customization

**Steps**:
```bash
# Clone repository
git clone https://github.com/Liamhigh/take2.git
cd take2

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# APKs will be in:
# - app/build/outputs/apk/debug/app-debug.apk
# - app/build/outputs/apk/release/app-release.apk
```

**Requirements**:
- JDK 17+
- Android SDK with API 34
- Network access to Google Maven

### Option 3: Android Studio

**Best for**: Development, debugging, visual UI editing

**Steps**:
1. Open Android Studio
2. File → Open → Select `take2` directory
3. Wait for Gradle sync
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. APKs generated in `app/build/outputs/apk/`

---

## 🔐 Production Keystore Setup

### Why You Need a Production Keystore

**Current state**: APKs are signed with debug keystore  
**Production requirement**: Must use production keystore for:
- Google Play Store submission
- Enterprise distribution
- Long-term app identity
- Update capability

### Generate Production Keystore

```bash
# Generate keystore
keytool -genkey -v \
  -keystore verum-omnis-production.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias verum-omnis-key

# You will be prompted for:
# - Keystore password (save this securely!)
# - Your name
# - Organization
# - City, State, Country

# Example inputs:
# CN=Liam Highcock, OU=Verum Omnis, O=Verum Global Foundation, 
# L=City, ST=State, C=US
```

**Important**: 
- ⚠️ **NEVER commit keystore to Git**
- ⚠️ **Store keystore password in password manager**
- ⚠️ **Backup keystore in multiple secure locations**
- ⚠️ **Losing keystore = cannot update app**

### Configure Keystore in Build

**Option A: Local Properties** (for local builds)

Create `keystore.properties`:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=verum-omnis-key
storeFile=/path/to/verum-omnis-production.jks
```

Update `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... existing release config
        }
    }
}
```

**Option B: GitHub Secrets** (for CI/CD)

1. Encode keystore to base64:
```bash
base64 -i verum-omnis-production.jks -o keystore.b64
```

2. Add secrets to GitHub:
   - Go to repository Settings → Secrets and variables → Actions
   - Add secrets:
     - `KEYSTORE_FILE`: (paste contents of keystore.b64)
     - `KEYSTORE_PASSWORD`: your keystore password
     - `KEY_ALIAS`: verum-omnis-key
     - `KEY_PASSWORD`: your key password

3. Update `.github/workflows/build-apk.yml`:
```yaml
- name: Decode keystore
  run: |
    echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks

- name: Build Release APK
  run: ./gradlew assembleRelease
  env:
    KEYSTORE_FILE: keystore.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

### Verify Keystore

```bash
# List keystore contents
keytool -list -v -keystore verum-omnis-production.jks

# Verify APK signature
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk

# Or use the provided script
./scripts/verify-apk-signature.sh app/build/outputs/apk/release/app-release.apk
```

---

## 🏪 Google Play Store Deployment

### Prerequisites Checklist

Before submitting to Play Store, you must have:

- [ ] **Production keystore** (generated and secured)
- [ ] **Google Play Console account** ($25 one-time fee)
- [ ] **Privacy policy** (hosted URL)
- [ ] **Terms of service** (hosted URL)
- [ ] **App screenshots** (5-8 images, various device sizes)
- [ ] **Feature graphic** (1024x500 px)
- [ ] **App icon** (512x512 px)
- [ ] **Short description** (80 characters max)
- [ ] **Full description** (4000 characters max)
- [ ] **Content rating questionnaire** (completed)
- [ ] **Target audience** (defined)
- [ ] **App category** (selected)

### Step 1: Create Play Console Account

1. Go to https://play.google.com/console
2. Sign in with Google account
3. Accept Developer Agreement
4. Pay $25 one-time registration fee
5. Complete account details

### Step 2: Create App

1. Click "Create app"
2. Fill in:
   - **App name**: Verum Omnis Forensic Engine
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free (or Paid, if applicable)
3. Accept declarations
4. Create app

### Step 3: Prepare Store Listing

**Required Screenshots** (5-8 images):
- Main screen (case list)
- Evidence upload screen
- Document scanner
- Report viewer
- Settings/about

**Recommended sizes**:
- Phone: 1080x1920 px (16:9)
- Tablet: 1536x2048 px (4:3)
- Tablet 10": 1920x1200 px (16:10)

**Feature Graphic** (required):
- Size: 1024x500 px
- Format: PNG or JPG
- Content: App logo + tagline

**App Icon** (required):
- Size: 512x512 px
- Format: PNG (32-bit with alpha)
- Design: High-resolution app icon

**Short Description** (80 chars):
```
Forensic evidence collection with cryptographic sealing and PDF reports
```

**Full Description** (up to 4000 chars):
```
Verum Omnis Forensic Engine - Professional Evidence Collection

FEATURES:
• Cryptographic evidence sealing with SHA-512
• GPS location and jurisdiction detection
• Court-admissible PDF reports
• Contradiction detection and analysis
• Chain of custody logging
• Multi-jurisdiction compliance
• Offline-first operation

SECURITY:
• Triple-hash cryptographic seal
• HMAC-SHA512 tamper detection
• No cloud logging or telemetry
• Airgap-ready design
• Screenshot protection

COMPLIANCE:
• UAE: Federal Evidence Law
• South Africa: ECT Act
• European Union: GDPR, eIDAS
• United States: Federal Rules of Evidence

PERFECT FOR:
• Legal professionals
• Forensic investigators
• Law enforcement
• Corporate compliance teams
• Private investigators

Download now and start collecting forensically sound evidence.

For support: https://github.com/Liamhigh/take2
```

### Step 4: Upload APK

1. Go to "Release" → "Production"
2. Click "Create new release"
3. Upload signed APK:
   - `app/build/outputs/apk/release/app-release.apk`
4. Fill in release notes:
```
Initial release of Verum Omnis Forensic Engine

Features:
- Cryptographic evidence sealing
- GPS jurisdiction detection
- PDF report generation
- Contradiction analysis
- Chain of custody logging
- Multi-jurisdiction compliance

Version: 1.0.0
```

### Step 5: Content Rating

1. Go to "Content rating"
2. Fill in questionnaire:
   - App type: Utility
   - Violence: None
   - Sexual content: None
   - Profanity: None
   - Controlled substances: None
   - User-generated content: No
3. Calculate rating
4. Apply rating

### Step 6: Pricing & Distribution

1. Go to "Pricing & distribution"
2. Select countries/regions:
   - Recommended: Worldwide (or UAE, SA, EU, US)
3. Content guidelines: Accept
4. US export laws: Yes (standard app)
5. Primarily child-directed: No

### Step 7: Submit for Review

1. Review all sections (ensure no errors)
2. Click "Send for review"
3. Wait for Google's review (1-7 days typically)

### Step 8: Monitor Review Status

- Check Play Console dashboard daily
- Respond to any review comments
- Address any policy violations promptly

---

## 🏢 Enterprise Distribution

### Option A: Google Play Enterprise

**Requirements**:
- Google Play Console account
- Managed Google Play setup
- EMM (Enterprise Mobility Management) provider

**Steps**:
1. Upload APK to Play Store (internal/alpha track)
2. Configure Managed Google Play
3. Distribute via EMM console

### Option B: MDM (Mobile Device Management)

**Popular MDM Solutions**:
- Microsoft Intune
- VMware Workspace ONE
- MobileIron
- Jamf (for iOS, but supports Android)

**General Steps**:
1. Sign APK with enterprise certificate
2. Upload to MDM console
3. Create deployment profile
4. Assign to user groups
5. Push to managed devices

### Option C: Direct Enterprise Distribution

**Requirements**:
- Internal file server or portal
- Device management policy
- User training materials

**Steps**:
1. Build and sign release APK
2. Host on internal server (HTTPS)
3. Provide download link to employees
4. Users enable "Install from unknown sources"
5. Users download and install APK

**Security Considerations**:
- Require VPN for download
- Implement device attestation
- Enable remote wipe capability
- Monitor installation compliance

---

## 📱 Direct Distribution

### Via GitHub Releases

**Best for**: Open source, public distribution, beta testing

**Steps**:

1. Create GitHub Release:
```bash
# Tag version
git tag v1.0.0
git push origin v1.0.0

# Or via GitHub web UI
# Releases → Create new release
```

2. Upload APKs as release assets
3. Write release notes
4. Publish release

**Download URL structure**:
```
https://github.com/Liamhigh/take2/releases/download/v1.0.0/app-release.apk
```

### Via Direct Link

**Best for**: Testing, limited distribution

**Steps**:
1. Host APK on web server (HTTPS required)
2. Share download link
3. Users download and install

**Example nginx config**:
```nginx
server {
    listen 443 ssl;
    server_name downloads.verumomnis.org;
    
    location /apk/ {
        alias /var/www/apk/;
        types {
            application/vnd.android.package-archive apk;
        }
        add_header Content-Disposition 'attachment; filename="verum-omnis.apk"';
    }
}
```

### Via QR Code

**Best for**: Conferences, demos, quick sharing

**Steps**:
1. Generate QR code for download URL
2. Print or display QR code
3. Users scan and download

**QR Code Generator**:
```bash
# Using qrencode (install first)
qrencode -o verum-omnis-download-qr.png \
  "https://github.com/Liamhigh/take2/releases/latest/download/app-release.apk"
```

---

## ✅ Verification & Testing

### Post-Deployment Verification

**After deploying to any channel, verify**:

1. **APK Signature**:
```bash
./scripts/verify-apk-signature.sh /path/to/app-release.apk
```

2. **Installation Test**:
- Install on clean test device
- Grant all permissions
- Create test case
- Add evidence
- Generate report
- Verify PDF output

3. **Security Test**:
- Verify FLAG_SECURE (screenshot blocked)
- Check no cloud connections (airplane mode)
- Verify cryptographic seals
- Test tamper detection

4. **Compliance Test**:
- Test in different GPS locations (if possible)
- Verify jurisdiction detection
- Check timestamp formatting
- Verify legal disclaimers

### Smoke Test Checklist

Run these tests after deployment:

- [ ] App launches successfully
- [ ] Create new case
- [ ] Add text evidence
- [ ] Capture photo evidence
- [ ] Scan document
- [ ] Generate PDF report
- [ ] View report in viewer
- [ ] Verify cryptographic seal
- [ ] Check GPS location (if available)
- [ ] Verify jurisdiction (if GPS available)
- [ ] Export report
- [ ] Verify offline operation (airplane mode)

### Performance Benchmarks

**Expected performance**:
- Cold start: <2 seconds
- Case creation: <500ms
- Evidence capture: <2 seconds
- PDF generation: 2-5 seconds
- Report viewing: <1 second

**If performance degrades**:
- Check device specs (should meet min requirements)
- Clear app cache
- Reinstall app
- Check for background apps

---

## 🔧 Troubleshooting

### Build Issues

**Problem**: Gradle build fails with network errors  
**Solution**: 
- Check internet connection
- Use GitHub Actions (has network access)
- Configure Gradle proxy if behind firewall

**Problem**: "Android SDK not found"  
**Solution**:
```bash
export ANDROID_HOME=/path/to/android-sdk
# Or create local.properties:
echo "sdk.dir=/path/to/android-sdk" > local.properties
```

**Problem**: "Unsupported class file major version 61"  
**Solution**: Ensure JDK 17 is installed and active

### Installation Issues

**Problem**: "App not installed"  
**Solution**:
- Enable "Install from unknown sources"
- Ensure sufficient storage space
- Uninstall previous version if exists

**Problem**: "App appears to be corrupt"  
**Solution**:
- Re-download APK
- Verify APK signature
- Try debug APK instead

**Problem**: Google Play Protect warning  
**Solution**:
- Tap "Install anyway"
- APK is safe (verified)
- Warning is normal for non-Play Store apps

### Runtime Issues

**Problem**: App crashes on launch  
**Solution**:
- Check Android version (must be 8.0+)
- Clear app data
- Reinstall app
- Check logcat for details

**Problem**: "Permission denied" for camera/GPS  
**Solution**:
- Settings → Apps → Verum Omnis → Permissions
- Enable Camera, Location, Storage

**Problem**: PDF generation fails  
**Solution**:
- Ensure sufficient storage
- Grant storage permission
- Check app logs

---

## ↩️ Rollback Procedures

### Scenario 1: Critical Bug in Production

**Immediate Action**:
1. Remove app from Play Store (if published)
2. Notify users via support channel
3. Provide link to previous stable version

**Steps**:
```bash
# Revert to previous tag
git checkout v0.9.0  # or last stable version

# Rebuild APKs
./gradlew clean assembleRelease

# Re-upload to distribution channels
# Update release notes with rollback information
```

### Scenario 2: Failed Play Store Review

**Action**:
- Review rejection reasons
- Fix issues
- Resubmit

**Common rejection reasons**:
- Privacy policy missing/incomplete
- Content rating incorrect
- Permissions not justified
- Metadata violations

### Scenario 3: APK Signature Mismatch

**Problem**: Cannot update app (keystore lost/mismatch)  
**Solution**:
- Unfortunately, no solution if keystore is lost
- Must publish as new app with new package name
- Notify users to uninstall and reinstall

**Prevention**:
- ✅ Backup keystore in multiple secure locations
- ✅ Use password manager for credentials
- ✅ Document keystore location

---

## 📊 Deployment Metrics

### Track These Metrics

**Installation Metrics**:
- Total installs
- Active devices
- Uninstalls
- Crash rate

**Usage Metrics** (if analytics implemented):
- Cases created
- Evidence items uploaded
- Reports generated
- Feature usage

**Performance Metrics**:
- App start time
- PDF generation time
- Crash-free rate
- ANR (App Not Responding) rate

**Tools**:
- Google Play Console (built-in analytics)
- Firebase Analytics (optional)
- Custom logging (must respect privacy)

---

## 📞 Support

**Documentation**:
- README.md - Project overview
- TESTING.md - Testing guide
- APK_SIGNING.md - Signing details
- This file - Deployment guide

**Issues**:
- GitHub Issues: https://github.com/Liamhigh/take2/issues

**Contact**:
- Repository: https://github.com/Liamhigh/take2
- Creator: Liam Highcock

---

## ✅ Deployment Checklist

### Pre-Deployment

- [ ] All tests passing
- [ ] Code review complete
- [ ] Security audit complete
- [ ] Documentation updated
- [ ] Version number bumped
- [ ] Changelog updated
- [ ] Production keystore generated
- [ ] Keystore backed up securely

### Play Store Deployment

- [ ] Screenshots created
- [ ] Feature graphic designed
- [ ] Privacy policy published
- [ ] Terms of service published
- [ ] Content rating completed
- [ ] APK signed with production keystore
- [ ] APK uploaded to Play Console
- [ ] Release notes written
- [ ] Submitted for review

### Enterprise Deployment

- [ ] MDM profile created
- [ ] Deployment groups defined
- [ ] User training materials prepared
- [ ] IT staff briefed
- [ ] Rollout schedule defined
- [ ] Support process established

### Direct Distribution

- [ ] Download location set up (HTTPS)
- [ ] Installation guide published
- [ ] QR code generated (if needed)
- [ ] Support channel established
- [ ] Monitoring in place

### Post-Deployment

- [ ] Smoke tests completed
- [ ] User feedback collected
- [ ] Metrics monitoring started
- [ ] Support tickets tracked
- [ ] Performance benchmarked
- [ ] Documentation updated based on issues

---

*Last updated: December 7, 2025*  
*Document version: 1.0*  
*Deployment status: Ready for production*
