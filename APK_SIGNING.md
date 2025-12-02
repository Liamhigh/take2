# APK Signing Status

## Current Signing Configuration

### Debug APKs
**Status: ✅ SIGNED (Automatically)**

Debug APKs built by this project are **automatically signed** using the Android debug keystore. This is the default behavior for all Android applications.

- **Keystore**: Android debug keystore (`~/.android/debug.keystore`)
- **Alias**: `androiddebugkey`
- **Password**: `android`
- **Certificate DN**: `CN=Android Debug, O=Android, C=US`

**Note**: Debug signatures are only suitable for development and testing. They should **NEVER** be used for production releases or distribution outside of development teams.

### Release APKs
**Status: ✅ CONFIGURABLE SIGNING**

Release APKs can now be signed with either:
1. **Production Release Keystore** (recommended for production)
2. **Debug Keystore** (fallback if release keystore not configured)

The build system automatically detects and uses the release keystore if configured via:
- GitHub Actions: Environment variables from GitHub Secrets
- Local builds: `keystore.properties` file

**For production use**, see [KEYSTORE_SETUP.md](./KEYSTORE_SETUP.md) for complete setup instructions.

## How Android APK Signing Works

All Android APKs **must** be signed before they can be installed on a device. The Android build system automatically signs APKs during the build process:

1. **Debug builds**: Automatically signed with the debug keystore
2. **Release builds without signingConfig**: Automatically signed with the debug keystore
3. **Release builds with signingConfig**: Signed with the specified production keystore

## Verification

You can verify APK signatures using these methods:

### Method 1: Using apksigner (Android SDK tool)
```bash
# Verify signature
apksigner verify --verbose app/build/outputs/apk/debug/app-debug.apk
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Print certificate information
apksigner verify --print-certs app/build/outputs/apk/debug/app-debug.apk
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

### Method 2: Using jarsigner (JDK tool)
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/debug/app-debug.apk
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### Method 3: Check during installation
Android will refuse to install unsigned APKs. If an APK installs successfully, it is signed.

## Production Signing Configuration

✅ **Release signing is now fully configured!**

The project supports production keystore signing through:

### GitHub Actions (Automated CI/CD)
Release APKs are automatically signed when you configure these GitHub Secrets:
- `RELEASE_KEYSTORE_B64`: Base64-encoded keystore file
- `RELEASE_KEYSTORE_PASSWORD`: Keystore password  
- `RELEASE_KEY_ALIAS`: Key alias name
- `RELEASE_KEY_PASSWORD`: Key password

### Local Builds
Release APKs can be signed locally using a `keystore.properties` file:
```properties
storeFile=release-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=release-key
keyPassword=YOUR_KEY_PASSWORD
```

**See [KEYSTORE_SETUP.md](./KEYSTORE_SETUP.md) for complete setup instructions.**

## Quick Setup

### 1. Create a Release Keystore
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

### 2. For GitHub Actions: Add Secrets
Encode your keystore:
```bash
base64 release-keystore.jks > keystore.b64
```

Add these secrets in GitHub: Settings → Secrets and variables → Actions
- `RELEASE_KEYSTORE_B64` (content of keystore.b64)
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS` (e.g., "release-key")
- `RELEASE_KEY_PASSWORD`

### 3. For Local Builds: Create keystore.properties
Create `keystore.properties` in project root with your keystore details (see above).

**That's it!** The build system will automatically use your release keystore.

## Current GitHub Actions Builds

The GitHub Actions workflow (`.github/workflows/build-apk.yml`) now:
- ✅ Decodes release keystore from GitHub Secrets (when available)
- ✅ Uses release keystore for signing release APKs (when configured)
- ✅ Falls back to debug keystore if release keystore not available
- ✅ Cleans up keystore file after build for security

To enable release signing in CI/CD, add the four required secrets to your GitHub repository.

## Forensic Evidence Integrity

For the Verum Omnis Forensic Engine, the APK signature is part of the chain of trust:

1. **APK Integrity**: The APK is signed, ensuring it hasn't been tampered with after building
2. **Code Authenticity**: The signature verifies the code comes from the expected developer
3. **Update Security**: Android only allows updates from APKs signed with the same key

The current debug-signed APKs are suitable for:
- Internal forensic use
- Testing and validation
- Development deployments
- Open-source distribution where users build from source

For production forensic work requiring legal admissibility, consider:
- Using a production keystore with verified identity
- Documenting the keystore creation and chain of custody
- Implementing APK signature verification in the app's integrity checks

## Summary

**Question: Are the APKs signed?**

**Answer: YES** ✅

- **Debug APKs**: Signed with Android debug keystore (automatic)
- **Release APKs**: Signed with production release keystore (if configured) OR debug keystore (fallback)
- **All APKs are signed**: Android requires all APKs to be signed; unsigned APKs cannot be installed

**For Production Use**: 
✅ Release keystore configuration is **already implemented**  
✅ GitHub Actions workflow is **ready to use your secrets**  
✅ Local builds support is **already configured**  

**Next Step**: Follow [KEYSTORE_SETUP.md](./KEYSTORE_SETUP.md) to add your keystore secrets!

## References

- [Android Documentation: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android Documentation: Configure Gradle to sign your app](https://developer.android.com/studio/publish/app-signing#gradle-sign)
- [Android Documentation: App Signing (Play Console)](https://support.google.com/googleplay/android-developer/answer/9842756)
