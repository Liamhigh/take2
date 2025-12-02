#!/bin/bash
# verify-apk-signature.sh - Verify APK signatures
# This script checks if APKs are properly signed and displays certificate information

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
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

# Check if apksigner is available
check_apksigner() {
    if command -v apksigner &> /dev/null; then
        return 0
    elif [ -n "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/build-tools/"*/apksigner ]; then
        return 0
    else
        return 1
    fi
}

# Get apksigner path
get_apksigner() {
    if command -v apksigner &> /dev/null; then
        echo "apksigner"
    elif [ -n "$ANDROID_HOME" ]; then
        # Find the latest build-tools version
        LATEST_BUILD_TOOLS=$(ls -v "$ANDROID_HOME/build-tools" 2>/dev/null | tail -1)
        if [ -n "$LATEST_BUILD_TOOLS" ]; then
            echo "$ANDROID_HOME/build-tools/$LATEST_BUILD_TOOLS/apksigner"
        fi
    fi
}

# Verify APK signature using apksigner
verify_with_apksigner() {
    local apk_path=$1
    local apksigner=$(get_apksigner)
    
    print_info "Using apksigner: $apksigner"
    
    echo -e "\n${BLUE}Verification Results:${NC}"
    if $apksigner verify --verbose "$apk_path" 2>&1; then
        print_success "APK signature is VALID"
    else
        print_error "APK signature verification FAILED"
        return 1
    fi
    
    echo -e "\n${BLUE}Certificate Information:${NC}"
    $apksigner verify --print-certs "$apk_path" 2>&1
}

# Verify APK signature using jarsigner
verify_with_jarsigner() {
    local apk_path=$1
    
    print_info "Using jarsigner (fallback)"
    
    echo -e "\n${BLUE}Verification Results:${NC}"
    if jarsigner -verify -verbose "$apk_path" 2>&1; then
        print_success "APK signature is VALID"
    else
        print_error "APK signature verification FAILED"
        return 1
    fi
    
    echo -e "\n${BLUE}Certificate Details:${NC}"
    jarsigner -verify -verbose -certs "$apk_path" 2>&1 | grep -A 10 "Signer #1"
}

# Check if APK is debug-signed
check_debug_signature() {
    local apk_path=$1
    
    # Try multiple certificate file extensions (RSA, DSA, EC)
    for cert_file in $(unzip -l "$apk_path" 2>/dev/null | grep "META-INF/.*\.(RSA\|DSA\|EC)" | awk '{print $4}'); do
        if unzip -p "$apk_path" "$cert_file" 2>/dev/null | keytool -printcert 2>&1 | grep -q "CN=Android Debug"; then
            print_warning "This APK is signed with the DEBUG keystore"
            print_info "Debug signatures are suitable for development but NOT for production"
            return 0
        fi
    done
    
    print_success "This APK is signed with a RELEASE keystore"
    return 1
}

# Main verification function
verify_apk() {
    local apk_path=$1
    
    print_header "Verifying APK: $(basename "$apk_path")"
    
    # Check if file exists
    if [ ! -f "$apk_path" ]; then
        print_error "APK file not found: $apk_path"
        return 1
    fi
    
    print_info "APK Size: $(du -h "$apk_path" | cut -f1)"
    
    # Verify signature
    if check_apksigner; then
        verify_with_apksigner "$apk_path"
    elif command -v jarsigner &> /dev/null; then
        verify_with_jarsigner "$apk_path"
    else
        print_error "Neither apksigner nor jarsigner found"
        print_info "Please install Android SDK build-tools or JDK"
        return 1
    fi
    
    # Check if debug signed
    echo ""
    check_debug_signature "$apk_path"
    
    return 0
}

# Main script
main() {
    print_header "APK Signature Verification Tool"
    
    # If no arguments, look for APKs in standard locations
    if [ $# -eq 0 ]; then
        print_info "No APK specified, searching for built APKs..."
        
        APK_DIR="app/build/outputs/apk"
        
        if [ ! -d "$APK_DIR" ]; then
            print_error "APK output directory not found: $APK_DIR"
            print_info "Usage: $0 <path-to-apk>"
            print_info "   or: $0  (to verify all APKs in app/build/outputs/apk/)"
            exit 1
        fi
        
        # Find all APKs
        mapfile -t APKS < <(find "$APK_DIR" -name "*.apk" -type f 2>/dev/null)
        
        if [ ${#APKS[@]} -eq 0 ]; then
            print_error "No APK files found in $APK_DIR"
            print_info "Build the project first with: ./gradlew assembleDebug assembleRelease"
            exit 1
        fi
        
        # Verify each APK
        SUCCESS_COUNT=0
        TOTAL_COUNT=0
        
        for apk in "${APKS[@]}"; do
            TOTAL_COUNT=$((TOTAL_COUNT + 1))
            if verify_apk "$apk"; then
                SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            fi
        done
        
        print_header "Summary"
        echo "Total APKs verified: $TOTAL_COUNT"
        echo "Successful: $SUCCESS_COUNT"
        echo "Failed: $((TOTAL_COUNT - SUCCESS_COUNT))"
        
        if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ]; then
            print_success "All APKs are properly signed"
            exit 0
        else
            print_error "Some APKs failed verification"
            exit 1
        fi
    else
        # Verify specified APK(s)
        SUCCESS_COUNT=0
        TOTAL_COUNT=$#
        
        for apk_path in "$@"; do
            if verify_apk "$apk_path"; then
                SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            fi
        done
        
        if [ $TOTAL_COUNT -gt 1 ]; then
            print_header "Summary"
            echo "Total APKs verified: $TOTAL_COUNT"
            echo "Successful: $SUCCESS_COUNT"
            echo "Failed: $((TOTAL_COUNT - SUCCESS_COUNT))"
        fi
        
        if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ]; then
            exit 0
        else
            exit 1
        fi
    fi
}

# Run main function
main "$@"
