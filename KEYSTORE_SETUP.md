# Keystore Setup Guide

This guide explains how to set up release signing for the Verum Omnis Forensic Engine APK.

## Overview

The project now supports **proper release APK signing** using a production keystore. This is essential for:
- Google Play Store distribution
- Production releases
- Enterprise distribution
- Legal/forensic evidence integrity requirements

## Current Status

- **Debug APKs**: Automatically signed with Android debug keystore
- **Release APKs**: Can be signed with production keystore OR falls back to debug keystore if not configured

## Quick Start

### For GitHub Actions (Recommended for Production)

1. **Create a Release Keystore** (one time only):
   ```bash
   keytool -genkey -v -keystore release-keystore.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias release-key
   ```
   
   You'll be prompted to enter:
   - Keystore password
   - Key password
   - Your name, organization, city, state, country
   
   **CRITICAL**: Keep these passwords safe! If you lose them, you cannot update your app.

2. **Encode the Keystore to Base64**:
   ```bash
   base64 release-keystore.jks > keystore.b64
   ```

3. **Add GitHub Secrets**:
   
   Go to your repository → Settings → Secrets and variables → Actions → New repository secret
   
   Add these four secrets:
   
   | Secret Name | Value | Description |
   |-------------|-------|-------------|
   | `RELEASE_KEYSTORE_B64` | Contents of `keystore.b64` file | Base64-encoded keystore |
   | `RELEASE_KEYSTORE_PASSWORD` | Your keystore password | Password you entered in step 1 |
   | `RELEASE_KEY_ALIAS` | `release-key` | The alias you used (or your custom alias) |
   | `RELEASE_KEY_PASSWORD` | Your key password | Key password from step 1 |

4. **Verify the Setup**:
   - Push a commit to the `main` branch
   - GitHub Actions will automatically decode the keystore and sign the release APK
   - Download the release APK artifact and verify it's signed with your keystore

### For Local Builds (Optional)

If you want to build signed release APKs locally:

1. **Create a Release Keystore** (same as above):
   ```bash
   keytool -genkey -v -keystore release-keystore.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias release-key
   ```

2. **Create `keystore.properties` file** in the project root:
   ```properties
   storeFile=release-keystore.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=release-key
   keyPassword=YOUR_KEY_PASSWORD
   ```
   
   Replace `YOUR_STORE_PASSWORD` and `YOUR_KEY_PASSWORD` with your actual passwords.

3. **Build the Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
   
   The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

**Note**: The `keystore.properties` file and `*.jks` files are automatically excluded by `.gitignore` and will never be committed.

## Security Best Practices

### DO:
✅ Store keystore files in a secure location with backups  
✅ Use strong passwords (minimum 8 characters, mixed case, numbers, symbols)  
✅ Keep keystore passwords in a password manager  
✅ Create multiple backups of the keystore file in secure locations  
✅ Document the keystore details (but NOT the passwords) for your records  
✅ Use GitHub Secrets for CI/CD signing  

### DO NOT:
❌ **NEVER** commit keystore files to git  
❌ **NEVER** commit keystore passwords to git  
❌ **NEVER** share keystore passwords in plain text  
❌ **NEVER** lose the keystore file (you cannot update your app without it)  
❌ **NEVER** use the debug keystore for production releases  

## Verifying APK Signatures

After building, verify that your APK is properly signed:

### Using apksigner (Android SDK):
```bash
# Verify signature
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Print certificate information
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

### Using jarsigner (JDK):
```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

### Expected Output for Release Keystore:
You should see your organization details (CN, O, L, ST, C) that you entered when creating the keystore.

### Expected Output for Debug Keystore:
```
CN=Android Debug, O=Android, C=US
```

## Troubleshooting

### "Keystore not found" error
- Ensure the keystore file path is correct in `keystore.properties`
- Check that the file exists and has the correct name

### "Wrong password" error
- Verify you're using the correct keystore password
- Verify you're using the correct key password
- Check for typos in `keystore.properties` or GitHub Secrets

### "Key alias not found" error
- Verify the alias name matches what you used when creating the keystore
- List aliases in your keystore:
  ```bash
  keytool -list -v -keystore release-keystore.jks
  ```

### GitHub Actions not using release keystore
- Verify all four secrets are set in GitHub repository settings
- Check the Actions workflow run logs for "Building release APK with release keystore"
- The keystore is only used when pushing to the `main` branch

### APK still signed with debug keystore
- Verify the release keystore file exists and is valid
- Check that environment variables or `keystore.properties` are set correctly
- Review the build output for signing configuration messages

## Migration from Debug to Release Signing

If you've already published an APK signed with the debug keystore:

**WARNING**: You **cannot** update an app with an APK signed by a different keystore.

Options:
1. **For development/testing**: Continue using debug signing (no changes needed)
2. **For new production app**: Use release keystore from the start
3. **For existing production app**: You must continue using the same keystore you originally used

## Forensic Chain of Trust

For the Verum Omnis Forensic Engine, proper APK signing is critical:

1. **Evidence Integrity**: The APK signature ensures the application hasn't been tampered with
2. **Chain of Custody**: Document the keystore creation date and process
3. **Legal Admissibility**: Production-signed APKs establish authenticity in court
4. **Update Security**: Prevents unauthorized updates to the forensic application

### Recommended for Legal/Forensic Use:
- Use a production release keystore (not debug)
- Document the keystore creation process
- Maintain secure backups of the keystore
- Implement APK signature verification in your deployment process
- Include APK signature information in evidence reports

## Additional Resources

- [Android: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android: Configure Gradle to sign your app](https://developer.android.com/studio/publish/app-signing#gradle-sign)
- [Google Play: App signing](https://support.google.com/googleplay/android-developer/answer/9842756)

## Summary

✅ **Release keystore signing is now configured**  
✅ **GitHub Actions workflow supports keystore secrets**  
✅ **Keystore files are protected by `.gitignore`**  
✅ **Falls back to debug signing if release keystore not available**  

To enable production signing, simply add the four GitHub Secrets as described above!
