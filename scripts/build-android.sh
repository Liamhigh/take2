#!/bin/bash
# Verum Omnis Android Build Script
#
# This script builds the Forensic Engine APK for Android.
# Prerequisites:
#   - JDK 17
#   - Android SDK (via Android Studio or command line tools)
#   - Gradle wrapper (included in project)

set -e

echo "🔨 Verum Omnis Forensic Engine - Build Script"
echo "=============================================="
echo ""

# Check for required tools
check_java() {
    if command -v java &> /dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VER" -ge 17 ]; then
            echo "✅ Java $JAVA_VER detected"
            return 0
        else
            echo "❌ Java 17+ required (found Java $JAVA_VER)"
            return 1
        fi
    else
        echo "❌ Java not found"
        return 1
    fi
}

check_android_sdk() {
    if [ -n "$ANDROID_HOME" ] || [ -n "$ANDROID_SDK_ROOT" ]; then
        echo "✅ Android SDK found"
        return 0
    else
        echo "⚠️  ANDROID_HOME not set - build may fail"
        return 0
    fi
}

# Clean previous builds
clean_build() {
    echo ""
    echo "🧹 Cleaning previous builds..."
    ./gradlew clean --quiet
    echo "✅ Clean complete"
}

# Generate rule assets
generate_assets() {
    echo ""
    echo "📦 Generating rule assets..."
    if command -v python3 &> /dev/null; then
        python3 scripts/generate-assets.py
    elif command -v python &> /dev/null; then
        python scripts/generate-assets.py
    else
        echo "⚠️  Python not found - skipping asset generation"
        echo "   Assets should already exist in app/src/main/assets/rules/"
    fi
}

# Build debug APK
build_debug() {
    echo ""
    echo "🔧 Building Debug APK..."
    ./gradlew assembleDebug --stacktrace
    
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        echo ""
        echo "✅ Debug build successful!"
        echo "   APK: app/build/outputs/apk/debug/app-debug.apk"
        ls -lh app/build/outputs/apk/debug/app-debug.apk
    else
        echo "❌ Debug build failed - APK not found"
        exit 1
    fi
}

# Build release APK
build_release() {
    echo ""
    echo "🔧 Building Release APK..."
    ./gradlew assembleRelease --stacktrace
    
    if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
        echo ""
        echo "✅ Release build successful!"
        echo "   APK: app/build/outputs/apk/release/app-release-unsigned.apk"
        ls -lh app/build/outputs/apk/release/app-release-unsigned.apk
    else
        echo "❌ Release build failed - APK not found"
        exit 1
    fi
}

# Run unit tests
run_tests() {
    echo ""
    echo "🧪 Running unit tests..."
    ./gradlew testDebugUnitTest --stacktrace
    echo "✅ Tests complete"
}

# Main build process
main() {
    # Parse arguments
    BUILD_TYPE="${1:-debug}"
    SKIP_TESTS="${2:-false}"
    
    echo "Build type: $BUILD_TYPE"
    echo "Skip tests: $SKIP_TESTS"
    echo ""
    
    # Check prerequisites
    echo "Checking prerequisites..."
    echo "-----------------------"
    check_java
    check_android_sdk
    
    # Generate assets
    generate_assets
    
    # Clean
    clean_build
    
    # Build
    case "$BUILD_TYPE" in
        "debug")
            build_debug
            ;;
        "release")
            build_release
            ;;
        "both")
            build_debug
            build_release
            ;;
        *)
            echo "❌ Unknown build type: $BUILD_TYPE"
            echo "   Usage: ./scripts/build-android.sh [debug|release|both] [skip-tests]"
            exit 1
            ;;
    esac
    
    # Run tests (unless skipped)
    if [ "$SKIP_TESTS" != "skip-tests" ]; then
        run_tests
    fi
    
    echo ""
    echo "=============================================="
    echo "✅ Build completed successfully!"
    echo ""
    echo "Output locations:"
    echo "  Debug APK:   app/build/outputs/apk/debug/"
    echo "  Release APK: app/build/outputs/apk/release/"
    echo ""
}

# Run main function with arguments
main "$@"
