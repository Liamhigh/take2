#!/bin/bash
#
# Verum Omnis Repository - Branch Cleanup Script
# Date: December 3, 2025
# Purpose: Delete 43+ old development branches from GitHub
#

set -e

echo "=============================================="
echo "Verum Omnis - Branch Cleanup Script"
echo "=============================================="
echo ""
echo "This script will delete 43+ old development branches"
echo "that are no longer needed after repository consolidation."
echo ""
echo "WARNING: This action cannot be undone!"
echo ""

# Check if git is available
if ! command -v git &> /dev/null; then
    echo "ERROR: git command not found"
    echo "Please install git and try again"
    exit 1
fi

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "ERROR: Not in a git repository"
    echo "Please run this script from the repository root"
    exit 1
fi

# Fetch latest from remote
echo "Fetching latest from remote..."
git fetch --all --prune

# Define branches to keep
KEEP_BRANCHES=(
    "main"
    "copilot/implement-forensic-enhancements"
    "copilot/unified-verum-logic-again"
    "copilot/prepare-safe-pull-request"
)

# Define branches to delete (43+ branches)
DELETE_BRANCHES=(
    # Build fix branches (16)
    "copilot/fix-build-error"
    "copilot/fix-build-errors"
    "copilot/fix-build-errors-again"
    "copilot/fix-build-errors-take2"
    "copilot/fix-android-build-errors"
    "copilot/fix-android-build-errors-again"
    "copilot/fix-build-failure-issue"
    "copilot/fix-failing-build-android-app"
    "copilot/fix-gradle-sync-issues"
    "copilot/fix-gradle-sync-issues-again"
    "copilot/fix-simple-build-issue"
    "copilot/fix-invalid-release-apk"
    "copilot/fixr8-slf4j-signing"
    "copilot/fix-apk-signing-issue"
    "copilot/fix-apk-signing-issues"
    "copilot/fix-app-issues"
    
    # Feature addition branches (3)
    "copilot/add-apk-integrity-checker"
    "copilot/add-forensic-engine-architecture"
    "copilot/add-logos-to-repository"
    
    # Status check branches (7)
    "copilot/check-apk-build-readiness"
    "copilot/check-apk-signing-status"
    "copilot/check-build-signing-and-secrets"
    "copilot/check-build-status"
    "copilot/find-verum-app-logic"
    "copilot/find-verum-app-logic-branch"
    "copilot/find-verum-app-logic-branch-again"
    "copilot/find-verum-app-logic-branches"
    
    # Build branches (4)
    "copilot/build-ak"
    "copilot/build-apk-with-git-actions"
    "copilot/build-offline-forensic-engine"
    "copilot/build-stateless-forensic-engine"
    
    # Other development branches (13+)
    "copilot/create-firebase-setup-guide"
    "copilot/fix-app-access-issues"
    "copilot/fix-app-download-issue"
    "copilot/list-project-structure"
    "copilot/manage-pdfs-in-repository"
    "copilot/remove-repository"
    "copilot/start-firebase-build-process"
    "copilot/start-firebase-deployment-build"
    "copilot/test-apk-forensic-engine"
    "copilot/update-android-apk-workflow"
    "copilot/update-readme-and-contributing"
    "copilot/vscode1760783093633"
    "copilot/vscode1760785529957"
    "Liamhigh-patch-1"
    "chore/bootstrap-project"
    "chore/update-readme-add-dotenv-contributing"
)

echo ""
echo "Branches to KEEP (${#KEEP_BRANCHES[@]}):"
for branch in "${KEEP_BRANCHES[@]}"; do
    echo "  ✅ $branch"
done

echo ""
echo "Branches to DELETE (${#DELETE_BRANCHES[@]}):"
for branch in "${DELETE_BRANCHES[@]}"; do
    echo "  ❌ $branch"
done

echo ""
echo "=============================================="
read -p "Do you want to proceed with deletion? (yes/no): " confirm
echo ""

if [ "$confirm" != "yes" ]; then
    echo "Aborting. No branches were deleted."
    exit 0
fi

echo "Starting branch deletion..."
echo ""

deleted_count=0
failed_count=0
not_found_count=0

for branch in "${DELETE_BRANCHES[@]}"; do
    echo -n "Deleting $branch... "
    
    # Check if branch exists on remote
    if git ls-remote --exit-code --heads origin "$branch" > /dev/null 2>&1; then
        # Try to delete the branch
        if git push origin --delete "$branch" 2>&1; then
            echo "✅ DELETED"
            ((deleted_count++))
        else
            echo "❌ FAILED"
            ((failed_count++))
        fi
    else
        echo "⚠️  NOT FOUND"
        ((not_found_count++))
    fi
done

echo ""
echo "=============================================="
echo "Branch Deletion Summary"
echo "=============================================="
echo "Successfully deleted: $deleted_count"
echo "Failed to delete: $failed_count"
echo "Not found (already deleted): $not_found_count"
echo "Total processed: ${#DELETE_BRANCHES[@]}"
echo ""

if [ $failed_count -gt 0 ]; then
    echo "⚠️  Some branches failed to delete."
    echo "You may need to delete them manually via GitHub UI."
    echo ""
fi

if [ $deleted_count -gt 0 ]; then
    echo "✅ Cleanup complete! Repository is now cleaner."
    echo ""
    echo "Remaining branches:"
    git branch -r | grep -v HEAD | grep origin/ || echo "  (none)"
else
    echo "ℹ️  No branches were deleted."
fi

echo ""
echo "=============================================="
