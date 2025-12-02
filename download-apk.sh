#!/bin/bash
# download-apk.sh - Download latest signed APKs from GitHub Actions
# This script downloads the most recent debug and release APKs built by CI

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}\n"
}

# Check for required tools
check_requirements() {
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed"
        echo ""
        echo "Please install it from: https://cli.github.com/"
        echo ""
        echo "Installation instructions:"
        echo "  macOS:   brew install gh"
        echo "  Ubuntu:  sudo apt install gh"
        echo "  Windows: winget install GitHub.cli"
        echo ""
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed"
        echo ""
        echo "Please install it from: https://stedolan.github.io/jq/"
        echo ""
        echo "Installation instructions:"
        echo "  macOS:   brew install jq"
        echo "  Ubuntu:  sudo apt install jq"
        echo "  Windows: winget install jqlang.jq"
        echo ""
        exit 1
    fi
}

# Get the repository name
get_repo() {
    if git rev-parse --git-dir > /dev/null 2>&1; then
        git config --get remote.origin.url | sed 's/.*github\.com[:/]\(.*\)\(\.git\)\?$/\1/'
    else
        echo "Liamhigh/take2"
    fi
}

# Main function
main() {
    print_header "Verum Omnis APK Downloader"
    
    # Check requirements
    check_requirements
    
    # Get repository
    REPO=$(get_repo)
    print_info "Repository: $REPO"
    
    # Check if authenticated
    if ! gh auth status &> /dev/null; then
        print_warning "You are not authenticated with GitHub CLI"
        print_info "Running: gh auth login"
        gh auth login
    fi
    
    # Get latest successful workflow run
    print_info "Fetching latest successful workflow run..."
    
    RUN_ID=$(gh run list \
        --repo "$REPO" \
        --workflow build-apk.yml \
        --status success \
        --limit 1 \
        --json databaseId \
        --jq '.[0].databaseId')
    
    if [ -z "$RUN_ID" ]; then
        print_error "No successful workflow runs found"
        print_info "Visit https://github.com/$REPO/actions/workflows/build-apk.yml"
        exit 1
    fi
    
    print_success "Found workflow run: #$RUN_ID"
    
    # Get run details
    RUN_INFO=$(gh run view "$RUN_ID" --repo "$REPO" --json headBranch,headSha,displayTitle,createdAt)
    BRANCH=$(echo "$RUN_INFO" | jq -r '.headBranch')
    COMMIT=$(echo "$RUN_INFO" | jq -r '.headSha' | cut -c1-7)
    TITLE=$(echo "$RUN_INFO" | jq -r '.displayTitle')
    DATE=$(echo "$RUN_INFO" | jq -r '.createdAt')
    
    echo ""
    print_info "Branch: $BRANCH"
    print_info "Commit: $COMMIT"
    print_info "Title: $TITLE"
    print_info "Date: $DATE"
    echo ""
    
    # Create download directory
    DOWNLOAD_DIR="downloaded-apks"
    mkdir -p "$DOWNLOAD_DIR"
    # Artifact names (must match workflow configuration)
    DEBUG_ARTIFACT="verum-omnis-debug-apk"
    RELEASE_ARTIFACT="verum-omnis-release-apk"
    
    print_info "Downloading APKs to ./$DOWNLOAD_DIR/"
    echo ""
    
    # Download debug APK
    print_info "Downloading debug APK..."
    if gh run download "$RUN_ID" \
        --repo "$REPO" \
        --name "$DEBUG_ARTIFACT" \
        --dir "$DOWNLOAD_DIR/debug"; then
        print_success "Debug APK downloaded successfully"
        # Find APK files more robustly
        mapfile -t DEBUG_APKS < <(find "$DOWNLOAD_DIR/debug" -name "*.apk" -type f)
        if [ "${#DEBUG_APKS[@]}" -gt 0 ]; then
            DEBUG_SIZE=$(du -h "${DEBUG_APKS[0]}" | cut -f1)
            print_info "  Location: ${DEBUG_APKS[0]}"
            print_info "  Size: $DEBUG_SIZE"
            if [ "${#DEBUG_APKS[@]}" -gt 1 ]; then
                print_warning "  Found ${#DEBUG_APKS[@]} APK files (showing first)"
            fi
        else
            print_warning "  No APK files found in downloaded artifact"
        fi
    else
        print_error "Failed to download debug APK (artifact name: $DEBUG_ARTIFACT)"
    fi
    
    echo ""
    
    # Download release APK
    print_info "Downloading release APK..."
    if gh run download "$RUN_ID" \
        --repo "$REPO" \
        --name "$RELEASE_ARTIFACT" \
        --dir "$DOWNLOAD_DIR/release"; then
        print_success "Release APK downloaded successfully"
        # Find APK files more robustly
        mapfile -t RELEASE_APKS < <(find "$DOWNLOAD_DIR/release" -name "*.apk" -type f)
        if [ "${#RELEASE_APKS[@]}" -gt 0 ]; then
            RELEASE_SIZE=$(du -h "${RELEASE_APKS[0]}" | cut -f1)
            print_info "  Location: ${RELEASE_APKS[0]}"
            print_info "  Size: $RELEASE_SIZE"
            if [ "${#RELEASE_APKS[@]}" -gt 1 ]; then
                print_warning "  Found ${#RELEASE_APKS[@]} APK files (showing first)"
            fi
        else
            print_warning "  No APK files found in downloaded artifact"
        fi
    else
        print_error "Failed to download release APK (artifact name: $RELEASE_ARTIFACT)"
    fi
    
    echo ""
    print_header "Download Complete"
    
    echo "APKs are ready for installation on your Android device!"
    echo ""
    print_info "Next steps:"
    echo "  1. Transfer the APK to your Android device"
    echo "  2. Enable 'Install from unknown sources' in Settings → Security"
    echo "  3. Open the APK file to install"
    echo ""
    print_info "For detailed installation instructions, see TESTING.md"
    echo ""
    print_info "To verify APK signatures:"
    echo "  ./scripts/verify-apk-signature.sh $DOWNLOAD_DIR/debug/*.apk"
    echo "  ./scripts/verify-apk-signature.sh $DOWNLOAD_DIR/release/*.apk"
    echo ""
}

# Run main function
main "$@"
