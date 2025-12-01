#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
REPO="Liamhigh/take2"
WORKFLOW_NAME="Build Android APK"
OUTPUT_DIR="downloaded-apks"
ARTIFACT_DEBUG="verum-omnis-debug-apk"
ARTIFACT_RELEASE="verum-omnis-release-apk"

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check for required tools
check_requirements() {
    print_info "Checking requirements..."
    
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed."
        echo "Please install it from: https://cli.github.com/"
        echo ""
        echo "Installation instructions:"
        echo "  macOS:   brew install gh"
        echo "  Ubuntu:  sudo apt install gh"
        echo "  Windows: winget install GitHub.cli"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed."
        echo "Please install it from your package manager:"
        echo "  macOS:   brew install jq"
        echo "  Ubuntu:  sudo apt install jq"
        echo "  Windows: winget install jqlang.jq"
        exit 1
    fi
    
    print_info "All requirements satisfied!"
}

# Check if user is authenticated with GitHub CLI
check_auth() {
    print_info "Checking GitHub authentication..."
    
    if ! gh auth status &> /dev/null; then
        print_error "Not authenticated with GitHub CLI."
        echo "Please run: gh auth login"
        exit 1
    fi
    
    print_info "GitHub authentication verified!"
}

# Get the latest successful workflow run
get_latest_run() {
    print_info "Fetching latest successful workflow run..."
    
    local run_id=$(gh run list \
        --repo "$REPO" \
        --workflow "$WORKFLOW_NAME" \
        --status success \
        --limit 1 \
        --json databaseId \
        --jq '.[0].databaseId')
    
    if [ -z "$run_id" ]; then
        print_error "No successful workflow runs found."
        echo "Please check: https://github.com/$REPO/actions"
        exit 1
    fi
    
    print_info "Found workflow run: $run_id"
    echo "$run_id"
}

# List and download artifacts
download_artifacts() {
    local run_id=$1
    
    print_info "Listing artifacts for run $run_id..."
    
    # Create output directory
    mkdir -p "$OUTPUT_DIR"
    
    # Get artifact list
    local artifacts=$(gh api \
        "/repos/$REPO/actions/runs/$run_id/artifacts" \
        --jq '.artifacts | map({name: .name, id: .id})')
    
    if [ "$artifacts" = "[]" ]; then
        print_warn "No artifacts found for this run."
        echo "The workflow may not have uploaded any artifacts."
        exit 1
    fi
    
    print_info "Available artifacts:"
    echo "$artifacts" | jq -r '.[] | .name'
    echo ""
    
    # Ask user which artifact to download
    echo "Which artifact would you like to download?"
    echo "1) Debug APK"
    echo "2) Release APK"
    echo "3) Both"
    echo "4) Cancel"
    echo ""
    read -p "Enter your choice (1-4): " choice
    
    case $choice in
        1)
            download_artifact "$run_id" "$ARTIFACT_DEBUG"
            ;;
        2)
            download_artifact "$run_id" "$ARTIFACT_RELEASE"
            ;;
        3)
            download_artifact "$run_id" "$ARTIFACT_DEBUG"
            download_artifact "$run_id" "$ARTIFACT_RELEASE"
            ;;
        4)
            print_info "Download cancelled."
            exit 0
            ;;
        *)
            print_error "Invalid choice."
            exit 1
            ;;
    esac
}

# Download a specific artifact
download_artifact() {
    local run_id=$1
    local artifact_name=$2
    
    print_info "Downloading $artifact_name..."
    
    # Download using gh CLI
    if gh run download "$run_id" \
        --repo "$REPO" \
        --name "$artifact_name" \
        --dir "$OUTPUT_DIR"; then
        print_info "Successfully downloaded to: $OUTPUT_DIR/"
    else
        print_error "Failed to download $artifact_name"
        exit 1
    fi
}

# Main function
main() {
    echo "========================================"
    echo "  Verum Omnis APK Download Script"
    echo "========================================"
    echo ""
    
    check_requirements
    check_auth
    
    local run_id=$(get_latest_run)
    download_artifacts "$run_id"
    
    echo ""
    print_info "Download complete!"
    echo ""
    echo "APK files are located in: $OUTPUT_DIR/"
    echo ""
    echo "To install on your Android device:"
    echo "  1. Transfer the APK to your device"
    echo "  2. Enable 'Install from Unknown Sources'"
    echo "  3. Open the APK file to install"
    echo ""
}

# Run main function
main

