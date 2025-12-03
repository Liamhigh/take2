# Running Tests in Android Studio

This guide explains how to run the Verum Omnis unit tests in Android Studio IDE.

## 🎯 TL;DR - Quick Start

**The project now includes 6 pre-configured test runners!**

Just open the project in Android Studio and:
1. Click the **Run Configuration** dropdown (top-right toolbar)
2. Select **"All Unit Tests"** or any specific test class
3. Click **Run** ▶

No configuration needed - everything is ready to go!

---

## Prerequisites

Before you can run tests in Android Studio, you need:

1. **Android Studio** installed (Hedgehog 2023.1.1 or later recommended)
2. **JDK 17** configured in Android Studio
3. **Android SDK** with API level 34 installed
4. The project properly synced with Gradle

## Opening the Project

1. Launch Android Studio
2. Click **File → Open**
3. Navigate to the project directory and select it
4. Click **OK**
5. Wait for Gradle sync to complete

> **Note:** If you encounter network issues during Gradle sync (blocked access to dl.google.com), you may need to configure a proxy or use an environment where Google's Maven repository is accessible. The tests run successfully in GitHub Actions CI/CD.

## Running Tests

### Method 1: Using Pre-configured Run Configurations

The project includes pre-configured run configurations for all test classes:

1. Click the **Run/Debug Configuration** dropdown in the toolbar (top-right)
2. Select one of the available test configurations:
   - **All Unit Tests** - Runs all unit tests in the project
   - **CryptographicSealingEngineTest** - Tests for cryptographic sealing
   - **ChainOfCustodyLoggerTest** - Tests for chain of custody logging
   - **LevelerEngineTest** - Tests for fairness leveling engine
   - **ForensicNarrativeGeneratorTest** - Tests for PDF report generation
   - **OfflineVerificationEngineTest** - Tests for offline verification
3. Click the **Run** (▶) or **Debug** (🐛) button

### Method 2: Right-Click on Test Files

You can run tests directly from the Project view:

1. In the **Project** panel (left side), navigate to:
   ```
   app/src/test/java/org/verumomnis/forensic/
   ```
2. Right-click on any test file (e.g., `CryptographicSealingEngineTest.kt`)
3. Select **Run 'CryptographicSealingEngineTest'**

### Method 3: Run All Tests in a Package

To run all tests at once:

1. In the **Project** panel, right-click on:
   ```
   app/src/test/java/org/verumomnis/forensic/
   ```
2. Select **Run 'Tests in 'org.verumomnis.forensic''**

### Method 4: Run Individual Test Methods

To run a specific test method:

1. Open any test file in the editor
2. Click the green **▶** icon in the gutter next to the test method
3. Select **Run** or **Debug**

## Viewing Test Results

After running tests, Android Studio displays results in the **Run** panel:

- **Green checkmark (✓)**: Test passed
- **Red X (✗)**: Test failed
- **Yellow exclamation (!!)**: Test was skipped or ignored

Click on any test to see:
- Execution time
- Stack traces for failures
- Test output and assertions

## Running Tests with Coverage

To see code coverage:

1. Select a test configuration
2. Click **Run → Run 'ConfigurationName' with Coverage** (or use the coverage icon)
3. View coverage results in the **Coverage** panel

The project is configured to generate JaCoCo coverage reports.

## Test Coverage Report

Generate an HTML coverage report:

1. Open the **Terminal** in Android Studio (bottom panel)
2. Run:
   ```bash
   ./gradlew jacocoTestReport
   ```
3. View the report at:
   ```
   app/build/reports/jacoco/jacocoTestReport/html/index.html
   ```

## Available Tests

The project includes comprehensive unit tests:

### CryptographicSealingEngineTest (35 tests)
- SHA-512 hashing validation
- Cryptographic seal creation and verification
- Triple hash layer forensic sealing
- Tamper detection
- HMAC-SHA512 signatures

### ChainOfCustodyLoggerTest
- Chain of custody event logging
- Timestamp accuracy
- Event ordering and integrity

### LevelerEngineTest
- Fairness detection algorithms
- Vulnerable party identification
- Bias detection in evidence

### ForensicNarrativeGeneratorTest
- PDF report generation
- Structured narrative formatting
- Legal admissibility compliance

### OfflineVerificationEngineTest
- Offline verification workflows
- Evidence integrity validation
- No network dependency verification

## Troubleshooting

### Gradle Sync Fails

If Gradle sync fails with network errors:

1. Check your internet connection
2. Verify you can access maven.google.com
3. Try using a VPN or corporate proxy if needed
4. Alternatively, wait for the GitHub Actions CI/CD to run tests automatically

### Tests Don't Appear

If tests don't show up in the IDE:

1. Ensure you're in the **Project** view (not **Android** view)
2. Right-click on the test directory and select **Mark Directory as → Test Sources Root**
3. Invalidate caches: **File → Invalidate Caches / Restart**

### JDK Version Issues

If you get JDK-related errors:

1. Go to **File → Project Structure → SDK Location**
2. Ensure **JDK location** points to JDK 17
3. Go to **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
4. Set **Gradle JDK** to version 17

### Out of Memory Errors

If tests fail with out of memory errors:

1. Go to **File → Settings → Build, Execution, Deployment → Compiler**
2. Increase **Build process heap size** to 2048 MB or higher
3. Or edit `gradle.properties` and add:
   ```properties
   org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
   ```

## Running Tests from Command Line

You can also run tests using Gradle from the terminal:

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run with detailed output
./gradlew testDebugUnitTest --info

# Run specific test class
./gradlew testDebugUnitTest --tests "org.verumomnis.forensic.CryptographicSealingEngineTest"

# Run specific test method
./gradlew testDebugUnitTest --tests "org.verumomnis.forensic.CryptographicSealingEngineTest.computeHash returns consistent SHA-512 hash"

# Generate coverage report
./gradlew jacocoTestReport
```

## Continuous Integration

All tests run automatically on every push via GitHub Actions. View test results:

1. Go to the [Actions tab](https://github.com/Liamhigh/take2/actions)
2. Click on the latest workflow run
3. Download the **test-results** artifact to see detailed HTML reports

## Test Configuration

The test configuration is defined in `app/build.gradle.kts`:

```kotlin
testOptions {
    unitTests.all {
        // Run tests in parallel for faster execution
        it.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        // Fork a new JVM for each 50 test classes to prevent memory issues
        it.setForkEvery(50)
    }
    unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}
```

This ensures:
- Tests run in parallel for better performance
- Android resources are available during testing
- Mock Android framework methods return default values

## Best Practices

1. **Run tests frequently** - Validate changes as you code
2. **Use descriptive test names** - Follow the backtick naming convention
3. **Test edge cases** - Include null, empty, and boundary values
4. **Check coverage** - Aim for high code coverage in critical modules
5. **Keep tests fast** - Unit tests should run in seconds, not minutes

## Next Steps

- Read the main [TESTING.md](TESTING.md) for APK testing on devices
- Review test results in [GitHub Actions](https://github.com/Liamhigh/take2/actions)
- Contribute new tests for uncovered code paths
