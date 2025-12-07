# Quick Answer: Which Branch Has the Full Verum App?

## TL;DR

**Use the `main` branch.**

Out of 52 branches analyzed, only **2 branches** contain the Verum Omnis forensic application:

1. ⭐ **`main`** - The stable production branch (Score: 11/19)
2. **`copilot/find-verum-app-logic`** - Current working branch (identical to main)

## What's Included

The `main` branch contains:

✅ **3 Activities:**
- MainActivity
- ReportViewerActivity  
- ScannerActivity

✅ **10 Forensic Modules:**
- Core (ForensicEngine, ForensicEvidence)
- Crypto (SHA-512 hashing)
- Custody (Chain of custody logging)
- Jurisdiction (Compliance engine)
- Leveler
- Location (Forensic location service)
- PDF (PDF generation)
- Report (Narrative generator)
- UI (Activities and theme)
- Verification (Offline verification)

✅ **SHA-512 Hashing:** Confirmed in CryptographicSealingEngine.kt

✅ **PDF Generation:** ForensicPdfGenerator.kt

## What's Missing

The `main` branch is missing some requested features:

❌ **Fragments** - Uses Activities only, no Fragments found
❌ **Case Creation/Saving Logic** - No dedicated Case classes
❌ **Navigation Graph** - No navigation.xml files (uses imperative navigation)
❌ **Timeline Analysis** - No dedicated timeline classes
❌ **UI Layouts** - No XML layouts (likely uses Jetpack Compose)

## All Other Branches

The other **50 branches** contain NO Verum app logic:
- Only documentation updates
- Build/CI configuration changes  
- README modifications
- No source code

**Note:** Branches like `copilot/unified-verum-logic`, `copilot/unified-verum-logic-again`, and `copilot/implement-forensic-enhancements` (mentioned in repository memories) were found to be **empty** of actual code - they only contain documentation.

## Recommendation

Use **`main`** branch for the Verum Omnis application.

See [VERUM_APP_BRANCH_ANALYSIS.md](./VERUM_APP_BRANCH_ANALYSIS.md) for the complete analysis.
