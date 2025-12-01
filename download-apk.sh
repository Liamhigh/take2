#!/bin/bash
# Script to help users download the latest successful build APK from GitHub Actions
# This is a workaround for when local builds fail due to network restrictions

set -e

echo "================================================"
echo "Verum Omnis APK Download Helper"
echo "================================================"
echo ""
echo "This script helps you get the latest pre-built APK"
echo "when local builds fail due to network restrictions."
echo ""

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI (gh) is not installed."
    echo ""
    echo "Please install it from: https://cli.github.com/"
    echo ""
    echo "Or manually download the APK from:"
    echo "https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml"
    echo ""
    exit 1
fi

echo "✅ GitHub CLI found"
echo ""

# Set repository
REPO="Liamhigh/take2"
WORKFLOW="build-apk.yml"

echo "🔍 Finding latest successful build..."
echo ""

# Get the latest successful workflow run
RUN_ID=$(gh run list --repo "$REPO" --workflow "$WORKFLOW" --status success --limit 1 --json databaseId --jq '.[0].databaseId')

if [ -z "$RUN_ID" ]; then
    echo "❌ No successful builds found."
    echo ""
    echo "Please check: https://github.com/$REPO/actions/workflows/$WORKFLOW"
    exit 1
fi

echo "✅ Found successful build: Run #$RUN_ID"
echo ""

# List available artifacts
echo "📦 Available artifacts:"
gh run view "$RUN_ID" --repo "$REPO" --json artifacts --jq '.artifacts[] | "  - \(.name) (\(.sizeInBytes / 1024 / 1024 | floor)MB)"'
echo ""

# Ask user which to download
echo "Which APK would you like to download?"
echo "  1) Debug APK (larger, with debugging enabled)"
echo "  2) Release APK (smaller, optimized for production)"
echo ""
read -p "Enter choice [1 or 2]: " choice

case $choice in
    1)
        ARTIFACT="verum-omnis-debug-apk"
        ;;
    2)
        ARTIFACT="verum-omnis-release-apk"
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
echo "📥 Downloading $ARTIFACT..."
echo ""

# Download the artifact
gh run download "$RUN_ID" --repo "$REPO" --name "$ARTIFACT"

echo ""
echo "✅ Download complete!"
echo ""
echo "📁 APK extracted to: ./$ARTIFACT/"
ls -lh "$ARTIFACT"/*.apk
echo ""
echo "================================================"
echo "Installation Instructions:"
echo "================================================"
echo ""
echo "From Computer (via USB):"
echo "  1. Connect your Android device via USB"
echo "  2. Enable USB debugging on your device"
echo "  3. Run: adb install $ARTIFACT/*.apk"
echo ""
echo "From Android Device:"
echo "  1. Transfer the APK to your device"
echo "  2. Enable 'Install from Unknown Sources'"
echo "  3. Open the APK file to install"
echo ""
echo "For more details, see GETTING_STARTED.md"
echo "================================================"
