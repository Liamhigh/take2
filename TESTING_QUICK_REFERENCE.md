# Quick Reference: Running Tests in Android Studio

## 🎯 Quick Start

### Option 1: Use Pre-Configured Run Configurations
1. Click the dropdown in the toolbar (next to the ▶ run button)
2. Select a test configuration:
   - **All Unit Tests** ← Run everything
   - **CryptographicSealingEngineTest**
   - **ChainOfCustodyLoggerTest**
   - **LevelerEngineTest**
   - **ForensicNarrativeGeneratorTest**
   - **OfflineVerificationEngineTest**
3. Click **Run** ▶ or **Debug** 🐛

### Option 2: Right-Click in Project Explorer
1. Navigate to: `app/src/test/java/org/verumomnis/forensic/`
2. Right-click on:
   - A test file → **Run '[TestName]'**
   - The package → **Run 'Tests in org.verumomnis.forensic'**
   - A specific test method → **Run '[methodName]'**

### Option 3: Click Gutter Icons in Code
1. Open any test file
2. Click the green ▶ icon next to:
   - Class name → Run all tests in class
   - Test method → Run single test

## 📊 View Test Results

After running tests, check the **Run** panel (bottom of IDE):
- ✅ Green checkmark = Passed
- ❌ Red X = Failed
- ⚠️ Yellow = Skipped

Click on any test to see:
- Execution time
- Stack traces (for failures)
- Assertion details

## 📈 Generate Coverage Report

**In IDE:**
1. Select test configuration
2. Click **Run → Run with Coverage** (or coverage icon ⚡)
3. View results in **Coverage** panel

**Via Terminal:**
```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## 🧪 Test Structure

```
app/src/test/java/org/verumomnis/forensic/
├── CryptographicSealingEngineTest.kt     (35 tests)
├── ChainOfCustodyLoggerTest.kt
├── LevelerEngineTest.kt
├── ForensicNarrativeGeneratorTest.kt
└── OfflineVerificationEngineTest.kt
```

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Check internet connection to dl.google.com |
| Tests don't appear | Invalidate caches: **File → Invalidate Caches and Restart** |
| JDK version error | Set to JDK 17 in **File → Project Structure** |
| Out of memory | Increase heap: **File → Settings → Compiler** |

## 📚 Full Documentation

For complete details, see:
- **[ANDROID_STUDIO_TESTING.md](ANDROID_STUDIO_TESTING.md)** - Complete testing guide
- **[TESTING.md](TESTING.md)** - APK testing on devices
- **[README.md](README.md)** - Project overview

## 🚀 Command Line Testing

```bash
# Run all tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "org.verumomnis.forensic.CryptographicSealingEngineTest"

# Generate coverage
./gradlew jacocoTestReport
```

## ✨ What's Tested

- ✅ **Cryptography**: SHA-512 hashing, HMAC sealing, tamper detection
- ✅ **Chain of Custody**: Event logging, timestamp integrity
- ✅ **Fairness Engine**: Bias detection, vulnerable party identification
- ✅ **PDF Generation**: Forensic reports, legal compliance
- ✅ **Offline Verification**: No network dependency validation
