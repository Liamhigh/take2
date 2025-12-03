#!/bin/bash
# cleanup-branches.sh
# Script to delete all temporary copilot and feature branches
# Run this AFTER verifying that unified-verum-logic has all needed code

set -e

echo "🧹 Verum Omnis Branch Cleanup Script"
echo "===================================="
echo ""
echo "This script will delete all copilot/* and chore/* branches"
echo "that have been consolidated into unified-verum-logic"
echo ""
echo "⚠️  WARNING: This is destructive. Make sure unified-verum-logic"
echo "   contains all the code you need before proceeding!"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Cancelled."
    exit 0
fi

echo ""
echo "📋 Branches to delete:"
echo ""

# List of all copilot branches to delete
branches=(
    "copilot/add-apk-integrity-checker"
    "copilot/add-forensic-engine-architecture"
    "copilot/add-logos-to-repository"
    "copilot/build-ak"
    "copilot/build-apk-with-git-actions"
    "copilot/build-offline-forensic-engine"
    "copilot/build-stateless-forensic-engine"
    "copilot/check-apk-build-readiness"
    "copilot/check-apk-signing-status"
    "copilot/check-build-signing-and-secrets"
    "copilot/check-build-status"
    "copilot/create-firebase-setup-guide"
    "copilot/fix-android-build-errors"
    "copilot/fix-android-build-errors-again"
    "copilot/fix-apk-signing-issue"
    "copilot/fix-apk-signing-issues"
    "copilot/fix-app-access-issues"
    "copilot/fix-app-download-issue"
    "copilot/fix-app-issues"
    "copilot/fix-build-error"
    "copilot/fix-build-errors"
    "copilot/fix-build-errors-again"
    "copilot/fix-build-errors-take2"
    "copilot/fix-build-failure-issue"
    "copilot/fix-failing-build-android-app"
    "copilot/fix-invalid-release-apk"
    "copilot/fix-simple-build-issue"
    "copilot/fixr8-slf4j-signing"
    "copilot/implement-forensic-enhancements"
    "copilot/manage-pdfs-in-repository"
    "copilot/remove-repository"
    "copilot/start-firebase-build-process"
    "copilot/start-firebase-deployment-build"
    "copilot/test-apk-forensic-engine"
    "copilot/unified-verum-logic"
    "copilot/unified-verum-logic-again"
    "copilot/update-android-apk-workflow"
    "copilot/update-readme-and-contributing"
    "copilot/vscode1760783093633"
    "copilot/vscode1760785529957"
    "chore/bootstrap-project"
    "chore/update-readme-add-dotenv-contributing"
    "Liamhigh-patch-1"
)

for branch in "${branches[@]}"; do
    echo "  - $branch"
done

echo ""
echo "📊 Total branches to delete: ${#branches[@]}"
echo ""
read -p "Proceed with deletion? (yes/no): " confirm2

if [ "$confirm2" != "yes" ]; then
    echo "❌ Cancelled."
    exit 0
fi

echo ""
echo "🗑️  Deleting branches..."
echo ""

deleted_count=0
failed_count=0

for branch in "${branches[@]}"; do
    echo -n "Deleting $branch... "
    
    # Try to delete using gh CLI
    if gh api repos/Liamhigh/take2/git/refs/heads/$branch -X DELETE >/dev/null 2>&1; then
        echo "✅ Deleted"
        ((deleted_count++))
    else
        echo "❌ Failed (may not exist or no permission)"
        ((failed_count++))
    fi
done

echo ""
echo "✅ Cleanup complete!"
echo "   Deleted: $deleted_count"
echo "   Failed: $failed_count"
echo ""
echo "📁 Remaining branches should be:"
echo "   - main (or unified-verum-logic renamed to main)"
echo ""
echo "Next steps:"
echo "1. Verify unified-verum-logic has all code"
echo "2. Rename unified-verum-logic to main (or merge)"
echo "3. Set main as default branch in GitHub settings"
echo ""
