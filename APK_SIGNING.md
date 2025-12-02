# APK Signing Status

## ✅ YES - All APKs Are Signed and Ready for Testing!

**Quick Answer:** All APKs built by this project are properly signed and can be installed on any Android device. See **[TESTING.md](TESTING.md)** for download and installation instructions.

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
**Status: ⚠️ SIGNED WITH DEBUG KEY**

Currently, release APKs are also signed with the Android debug keystore because no explicit `signingConfig` is defined in the `release` build type.

**This is appropriate for:**
- Open-source projects
- Development builds
- Internal testing
- Pre-release distributions

**This is NOT appropriate for:**
- Google Play Store distribution
- Production releases
- Public app stores
- Enterprise distribution requiring verified publisher identity

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

If you need to distribute this app to production users or publish it on app stores, you should:

### 1. Create a Release Keystore
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

### 2. Store Keystore Securely
- **DO NOT** commit the keystore file to the repository
- Add `*.jks` and `*.keystore` to `.gitignore`
- Store keystore in a secure location with backups
- **CRITICAL**: If you lose the keystore, you cannot update the app on Google Play

### 3. Add Signing Configuration

Create a `keystore.properties` file (DO NOT commit this):
```properties
storeFile=/path/to/release-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=release-key
keyPassword=YOUR_KEY_PASSWORD
```

Update `app/build.gradle.kts`:
```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing configuration ...
    
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 4. GitHub Actions / CI Signing

For automated builds in CI/CD:

1. **Encode keystore as base64**:
   ```bash
   base64 release-keystore.jks > keystore.b64
   ```

2. **Add GitHub Secrets**:
   - `RELEASE_KEYSTORE_B64`: Base64 encoded keystore file
   - `RELEASE_KEYSTORE_PASSWORD`: Keystore password
   - `RELEASE_KEY_ALIAS`: Key alias
   - `RELEASE_KEY_PASSWORD`: Key password

3. **Update GitHub Actions workflow** (`.github/workflows/build-apk.yml`):
   ```yaml
   - name: Decode Keystore
     if: github.event_name == 'push' && github.ref == 'refs/heads/main'
     env:
       KEYSTORE_B64: ${{ secrets.RELEASE_KEYSTORE_B64 }}
     run: echo "$KEYSTORE_B64" | base64 -d > release-keystore.jks

   - name: Build Release APK
     if: github.event_name == 'push' && github.ref == 'refs/heads/main'
     env:
       KEYSTORE_FILE: release-keystore.jks
       KEYSTORE_PASSWORD: ${{ secrets.RELEASE_KEYSTORE_PASSWORD }}
       KEY_ALIAS: ${{ secrets.RELEASE_KEY_ALIAS }}
       KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
     run: ./gradlew assembleRelease
   ```

## Current GitHub Actions Builds

The current GitHub Actions workflow (`.github/workflows/build-apk.yml`) builds both debug and release APKs. Both are currently signed with the debug keystore.

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
- **Release APKs**: Currently signed with Android debug keystore (automatic, no explicit signingConfig)
- **All APKs are signed**: Android requires all APKs to be signed; unsigned APKs cannot be installed

**For Production Use**: If you need production-ready releases, follow the production signing configuration steps above to sign with a release keystore.

## References

- [Android Documentation: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android Documentation: Configure Gradle to sign your app](https://developer.android.com/studio/publish/app-signing#gradle-sign)
- [Android Documentation: App Signing (Play Console)](https://support.google.com/googleplay/android-developer/answer/9842756)
