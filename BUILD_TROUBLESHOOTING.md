# Build Troubleshooting Guide

## Common Build Issues

### Issue: Cannot Resolve Android Gradle Plugin

**Error Message:**
```
Plugin [id: 'com.android.application', version: '8.6.1', apply: false] was not found in any of the following sources
```

**Cause:**
The build environment blocks access to `dl.google.com`, which hosts the Android Gradle Plugin and other Android dependencies.

**Solution:**
This is a known limitation. Use pre-built APKs from GitHub Actions instead:
1. Run `./download-apk.sh` to automatically download the latest APK
2. Or manually download from the [Actions tab](https://github.com/Liamhigh/take2/actions)

See [GETTING_STARTED.md](GETTING_STARTED.md) for detailed instructions.

---

### Issue: Network Restrictions

**Blocked Domains:**
- `dl.google.com` - Google Maven repository (Android Gradle Plugin)
- `maven.google.com` - Google's Maven repository mirror
- Chinese mirrors (Tencent, Aliyun, etc.)

**Accessible Domains:**
- `github.com` - GitHub (repository access)
- `repo1.maven.org` - Maven Central
- `plugins.gradle.org` - Gradle Plugin Portal
- `services.gradle.org` - Gradle distribution server

**Why This Happens:**
The sandbox environment has restricted network access for security reasons. While Maven Central and Gradle repositories are accessible, Google's Maven repositories are not.

**Impact:**
- Cannot build Android applications locally
- Cannot download Android SDK components
- Cannot resolve Android dependencies

**Workaround:**
Use GitHub Actions to build APKs. The GitHub Actions environment has full network access and can build successfully.

---

### Issue: Gradle Fails to Download Dependencies

**Solution:**
The Gradle wrapper can download successfully, but Android-specific dependencies cannot. Use the pre-built APK approach instead of local builds.

---

### Issue: Want to Contribute Code Changes

**Solution:**
1. Make your code changes locally
2. Commit and push to GitHub
3. GitHub Actions will build the APK automatically
4. Download the artifact from the Actions tab to test
5. Open a pull request with your changes

---

### Issue: Need to Test Code Changes Quickly

**Solution:**
For rapid iteration:
1. Push your branch to GitHub
2. GitHub Actions will build automatically
3. Download the APK from artifacts (typically takes 5-10 minutes)

Alternatively, if you have access to an unrestricted environment:
1. Clone the repository on that environment
2. Build and test locally
3. Push your tested changes back to GitHub

---

## Build Configuration

### Gradle Version
- Gradle: 8.9
- Android Gradle Plugin: 8.6.1
- Kotlin: 2.0.21

### Repository Configuration
The project uses the following repositories (defined in `settings.gradle.kts`):
- Google Maven (blocked in restricted environments)
- Maven Central (accessible)
- Gradle Plugin Portal (accessible)

### Build Commands
When in an unrestricted environment:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# View all tasks
./gradlew tasks
```

Output location: `app/build/outputs/apk/`

---

## GitHub Actions Build

The repository includes a GitHub Actions workflow that:
1. Automatically builds APKs on every push
2. Runs on `ubuntu-latest` with full network access
3. Uploads APKs as artifacts
4. Supports both debug and release builds

### Accessing Build Artifacts
1. Go to [Actions](https://github.com/Liamhigh/take2/actions)
2. Click on the latest workflow run
3. Scroll to "Artifacts" section
4. Download the desired APK

---

## Need More Help?

If you encounter issues not covered here:
1. Check the [README.md](README.md) for project overview
2. Review the [GETTING_STARTED.md](GETTING_STARTED.md) guide
3. Open an issue on GitHub describing your problem
