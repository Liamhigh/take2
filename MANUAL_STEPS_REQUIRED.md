# Manual Steps Required (GitHub Repository Owner)

This document lists the steps that **require GitHub repository owner permissions** to complete the consolidation. These steps cannot be performed by the automated agent due to authentication restrictions.

## ⚠️ Important: Read Before Proceeding

The consolidation work is **complete** in the `copilot/unified-verum-logic` branch. This branch contains:
- ✅ All 15 Kotlin source files (5,277 lines of code)
- ✅ Complete forensic app functionality
- ✅ Documentation (CONSOLIDATION_SUMMARY.md, CONSOLIDATION_GUIDE.md)
- ✅ Branch cleanup script (cleanup-branches.sh)

## Step 1: Create the unified-verum-logic Branch

Since the work was done on `copilot/unified-verum-logic`, you need to create a proper `unified-verum-logic` branch on GitHub.

**Option A: Via GitHub Web Interface**
1. Go to https://github.com/Liamhigh/take2
2. Click the branch dropdown (currently shows "main" or current branch)
3. Type: `unified-verum-logic`
4. Click "Create branch: unified-verum-logic from copilot/unified-verum-logic"

**Option B: Via Command Line**
```bash
git clone https://github.com/Liamhigh/take2.git
cd take2
git checkout copilot/unified-verum-logic
git checkout -b unified-verum-logic
git push origin unified-verum-logic
```

## Step 2: Verify the Consolidation

Before proceeding with destructive operations, verify that `unified-verum-logic` has all the code you need:

```bash
git checkout unified-verum-logic

# Check files exist
ls -la CONSOLIDATION*.md cleanup-branches.sh
find app/src/main/java -name "*.kt" | wc -l  # Should show: 15

# Read the consolidation summary
cat CONSOLIDATION_SUMMARY.md

# Check that all features are present (see the guide)
cat CONSOLIDATION_GUIDE.md
```

## Step 3: Replace Main Branch with unified-verum-logic

**⚠️  WARNING: This is destructive! Back up first!**

### Backup First
```bash
# Create a backup branch of current main
git checkout main
git checkout -b main-backup-$(date +%Y%m%d)
git push origin main-backup-$(date +%Y%m%d)
```

### Option A: Via GitHub Web Interface (Recommended)

1. **Change default branch:**
   - Go to https://github.com/Liamhigh/take2/settings/branches
   - Under "Default branch", click the switch icon
   - Select `unified-verum-logic`
   - Click "Update"
   - Confirm the change

2. **Delete old main:**
   - Go to https://github.com/Liamhigh/take2/branches
   - Find the `main` branch
   - Click the delete (trash) icon
   - Confirm deletion

3. **Rename unified-verum-logic to main:**
   - This must be done via command line (see Option B step 3)

### Option B: Via Command Line (Advanced)

```bash
# Make sure you're on unified-verum-logic
git checkout unified-verum-logic

# Option 1: Force push over main (⚠️  destructive!)
git push origin unified-verum-logic:main --force

# Option 2: Rename branches
git branch -m main main-old  # Rename local main
git branch -m unified-verum-logic main  # Rename unified-verum-logic to main
git push origin :main-old  # Delete remote old main
git push origin main  # Push new main
git push -u origin main  # Set upstream
```

## Step 4: Clean Up Old Branches

There are **43 old branches** to delete:
- 40 copilot/* branches
- 2 chore/* branches  
- 1 patch branch

### Option A: Use the Cleanup Script

The repository includes `cleanup-branches.sh` which automates this:

```bash
# Review what will be deleted
cat cleanup-branches.sh

# Run the script (requires gh CLI)
./cleanup-branches.sh

# Follow the prompts
```

### Option B: Manual Deletion via GitHub Web Interface

1. Go to https://github.com/Liamhigh/take2/branches
2. For each branch to delete:
   - Click the delete (trash) icon next to the branch name
   - Confirm deletion
3. Delete these branches:
   ```
   copilot/add-apk-integrity-checker
   copilot/add-forensic-engine-architecture
   copilot/add-logos-to-repository
   copilot/build-ak
   copilot/build-apk-with-git-actions
   copilot/build-offline-forensic-engine
   copilot/build-stateless-forensic-engine
   copilot/check-apk-build-readiness
   copilot/check-apk-signing-status
   copilot/check-build-signing-and-secrets
   copilot/check-build-status
   copilot/create-firebase-setup-guide
   copilot/fix-android-build-errors
   copilot/fix-android-build-errors-again
   copilot/fix-apk-signing-issue
   copilot/fix-apk-signing-issues
   copilot/fix-app-access-issues
   copilot/fix-app-download-issue
   copilot/fix-app-issues
   copilot/fix-build-error
   copilot/fix-build-errors
   copilot/fix-build-errors-again
   copilot/fix-build-errors-take2
   copilot/fix-build-failure-issue
   copilot/fix-failing-build-android-app
   copilot/fix-invalid-release-apk
   copilot/fix-simple-build-issue
   copilot/fixr8-slf4j-signing
   copilot/implement-forensic-enhancements
   copilot/manage-pdfs-in-repository
   copilot/remove-repository
   copilot/start-firebase-build-process
   copilot/start-firebase-deployment-build
   copilot/test-apk-forensic-engine
   copilot/unified-verum-logic  # After creating unified-verum-logic
   copilot/unified-verum-logic-again
   copilot/update-android-apk-workflow
   copilot/update-readme-and-contributing
   copilot/vscode1760783093633
   copilot/vscode1760785529957
   chore/bootstrap-project
   chore/update-readme-add-dotenv-contributing
   Liamhigh-patch-1
   ```

### Option C: Bulk Delete via gh CLI

```bash
# Delete all copilot/* branches
gh api repos/Liamhigh/take2/branches --paginate | \
  jq -r '.[].name | select(startswith("copilot/"))' | \
  while read branch; do
    echo "Deleting $branch..."
    gh api repos/Liamhigh/take2/git/refs/heads/$branch -X DELETE
  done

# Delete chore/* branches
gh api repos/Liamhigh/take2/git/refs/heads/chore/bootstrap-project -X DELETE
gh api repos/Liamhigh/take2/git/refs/heads/chore/update-readme-add-dotenv-contributing -X DELETE

# Delete patch branch
gh api repos/Liamhigh/take2/git/refs/heads/Liamhigh-patch-1 -X DELETE
```

## Step 5: Verify Final State

After completing steps 1-4, verify the repository is clean:

```bash
# Clone fresh
git clone https://github.com/Liamhigh/take2.git
cd take2

# Check default branch
git branch --show-current  # Should show: main

# List all branches
gh api repos/Liamhigh/take2/branches --paginate | jq -r '.[].name'
# Should only show: main

# Verify code
find app/src/main/java -name "*.kt" | wc -l  # Should show: 15
cat CONSOLIDATION_SUMMARY.md  # Review features
```

## Step 6: Test the APK

Final verification that everything works:

```bash
# Option A: Download pre-built APK from GitHub Actions
./download-apk.sh

# Option B: Build locally (requires internet access to dl.google.com)
./gradlew assembleDebug
# APK location: app/build/outputs/apk/debug/app-debug.apk

# Install on device
adb install app-debug.apk

# Test the app
# - Launch "Verum Omnis"
# - Create a case
# - Scan evidence
# - Generate PDF report
# - Verify hash
```

## Troubleshooting

### "Permission denied" when pushing
**Cause:** Not repository owner or lacking write access  
**Solution:** Ask repository owner to perform these steps

### "Branch already exists" when creating unified-verum-logic
**Cause:** A previous attempt created the branch  
**Solution:** Delete the existing branch first or use it if it has the right content

### "Cannot delete main branch"
**Cause:** Main is set as the default branch  
**Solution:** Change default branch to unified-verum-logic first (Step 3, Option A)

### cleanup-branches.sh fails
**Cause:** Missing gh CLI or insufficient permissions  
**Solution:** Install gh CLI or use manual deletion (Option B)

## Final Repository Structure

After completing all steps:

```
Liamhigh/take2/
├── main (default branch)
│   ├── app/src/main/java/ (15 Kotlin files, 5,277 lines)
│   ├── CONSOLIDATION_SUMMARY.md
│   ├── CONSOLIDATION_GUIDE.md
│   ├── cleanup-branches.sh
│   └── ... (all project files)
└── (no other branches)
```

## Summary Checklist

- [ ] Step 1: Create unified-verum-logic branch from copilot/unified-verum-logic
- [ ] Step 2: Verify consolidation (check files, read docs)
- [ ] Step 3: Backup current main branch
- [ ] Step 3: Replace main with unified-verum-logic
- [ ] Step 4: Delete 43 old branches (copilot/*, chore/*, patch)
- [ ] Step 5: Verify final repository state
- [ ] Step 6: Build and test APK on Android device

## Questions?

See these documents for more information:
- **CONSOLIDATION_SUMMARY.md** - Detailed technical summary
- **CONSOLIDATION_GUIDE.md** - User-friendly consolidation guide
- **TESTING.md** - APK testing procedures
- **BUILD_STATUS.md** - CI/CD build information

---

**Note:** These steps require GitHub repository owner/admin permissions. If you don't have these permissions, contact the repository owner and share this document with them.
