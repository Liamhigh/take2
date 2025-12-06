# Four-Layer Forensic Engine Documentation

## Overview

The Verum Omnis forensic engine is a **deterministic, rule-based, court-admissible** system for detecting contradictions in evidence and classifying them into legal categories. The engine consists of exactly four layers that always run in a fixed order.

## Engine Architecture

```
Raw Evidence Text
       ↓
[1] NARRATIVE ENGINE ─────→ Structured Sentences
       ↓
[2] CONTRADICTION ENGINE ──→ Contradiction Results
       ↓
[3] CLASSIFICATION ENGINE ─→ Legal Findings
       ↓
[4] REPORT ENGINE ─────────→ Final Forensic Report
```

## Layer 1: Narrative Engine

**Purpose**: Convert raw text evidence into clean, indexed, timestamp-aware sentences.

**File**: `NarrativeEngine.kt`

**Key Features**:
- Splits text by sentence delimiters (`.`, `!`, `?`)
- Extracts timestamps from multiple formats:
  - YYYY-MM-DD
  - DD/MM/YYYY
  - MM/DD/YYYY
- Assigns index to preserve order
- Trims whitespace and removes empty entries

**Methods**:
```kotlin
fun ingest(rawText: String)
fun tokenize(): List<Sentence>
fun extractTimestamp(sentence: String): Long?
fun normalize(): List<Sentence>
```

**Output**: `List<Sentence>` where each sentence contains:
- `text: String` - The sentence content
- `index: Int` - Position in narrative
- `timestamp: Long?` - Extracted date (milliseconds since epoch)

## Layer 2: Contradiction Engine

**Purpose**: Detect conflicting statements between any two sentences using 7 deterministic rules.

**File**: `ContradictionEngine.kt`

**The 7 Contradiction Rules**:

### RULE 1: Direct Negation
Detects when one sentence contains negation keywords (`never`, `did not`, `didn't`, `no`) while the other affirms the same topic.

**Example**:
- A: "I never met him"
- B: "We met on Monday"
- Result: ✅ Contradiction detected

### RULE 2: Denial vs Evidence
Specific denial phrases contradicted by evidence.

**Denial Phrases**:
- "no payment" vs "payment"
- "never met" vs "met"
- "did not receive" vs "received"
- "no contact" vs "contact"

**Example**:
- A: "No payment was made"
- B: "Payment received on Jan 15"
- Result: ✅ Contradiction detected

### RULE 3: Timeline Conflicts
Same event mentioned with different dates or months.

**Event Keywords**: `met`, `meeting`, `invoice`, `payment`, `call`, `email`, `sent`

**Example**:
- A: "The meeting was in January 2024-01-15"
- B: "The meeting was in March 2024-03-20"
- Result: ✅ Timeline conflict

### RULE 4: Quantity Conflicts
Different numbers for the same subject.

**Example**:
- A: "We had one meeting about the deal"
- B: "We had three meetings about the deal"
- Result: ✅ Quantity conflict (1 vs 3)

### RULE 5: Admission vs Later Denial
Admission phrases contradicted by denial.

**Admission Phrases**: `I agreed`, `I accepted`, `I said`, `I confirmed`, `I promised`
**Denial Phrases**: `never agreed`, `didn't agree`, `never accepted`, `never said`

**Example**:
- A: "I agreed to the terms"
- B: "I never agreed to anything"
- Result: ✅ Admission contradicts denial

### RULE 6: Action vs Outcome Conflict
Action denial conflicts with outcome evidence.

**Action Denials**: `sent nothing`, `no email`, `no attachment`, `didn't send`
**Outcomes**: `email`, `attached`, `sent`, `received`

**Example**:
- A: "I sent nothing"
- B: "Email was attached to the message"
- Result: ✅ Action denial conflicts with outcome

### RULE 7: Data Access Claim Conflicts
Access denial contradicted by access evidence.

**Access Denials**: `did not access`, `never accessed`, `no access`, `didn't login`
**Access Evidence**: `logged in`, `accessed`, `login attempt`, `password`, `entered`

**Example**:
- A: "I did not access the system"
- B: "Login attempt recorded at 14:35"
- Result: ✅ Data access conflict

**Methods**:
```kotlin
suspend fun analyze(sentences: List<Sentence>): List<ContradictionResult>
private fun contradictionRule(a: Sentence, b: Sentence): String?
```

**Output**: `List<ContradictionResult>` where each result contains:
- `a: Sentence` - First contradicting sentence
- `b: Sentence` - Second contradicting sentence
- `reason: String` - Which rule detected the contradiction

## Layer 3: Classification Engine

**Purpose**: Map contradictions to legal subject categories.

**File**: `ClassificationEngine.kt`

**The 5 Legal Subjects**:

### 1. ShareholderOppression
**Trigger Keywords**: `profit`, `agreement`, `deal`, `decision`, `responsibility`, `ownership`, `shareholder`, `dividend`, `equity`, `shares`, `business`

**Use Case**: Corporate and business conflicts

### 2. FraudulentEvidence
**Trigger Keywords**: `delete`, `removed`, `cropped`, `missing`, `screenshot`, `edited`, `altered`, `modified`, `tampered`, `fabricated`

**Use Case**: Evidence tampering detection

### 3. Cybercrime
**Trigger Keywords**: `access`, `login`, `password`, `device`, `breach`, `unauthorized`, `hacked`, `account`, `credentials`, `system`

**Use Case**: Device and account access violations

### 4. BreachOfFiduciaryDuty
**Trigger Keywords**: `managing`, `accounting`, `decision-making`, `duty`, `lied`, `fiduciary`, `trust`, `responsibility`, `obligation`, `director`

**Use Case**: Trust and duty conflicts

### 5. EmotionalExploitation
**Trigger Keywords**: `gaslight`, `you said`, `you did`, `never happened`, `emotional`, `manipulate`, `abuse`, `control`, `deny`, `twist`

**Use Case**: Manipulation and denial patterns

**Classification Rules**:
- Each contradiction is analyzed for keyword matches
- A contradiction can map to **multiple categories**
- Only categories with contradictions are included in output

**Methods**:
```kotlin
fun classify(results: List<ContradictionResult>): List<LegalFinding>
```

**Output**: `List<LegalFinding>` where each finding contains:
- `subject: LegalSubject` - The legal category
- `contradictions: List<ContradictionResult>` - Supporting contradictions

## Layer 4: Report Engine

**Purpose**: Generate final structured forensic report.

**File**: `ReportEngine.kt`

**Report Sections** (in exact order):

### 1. Pre-Analysis Declaration
```
VERUM OMNIS FORENSIC ANALYSIS REPORT
======================================================================
Report Generated: 2024-12-06 01:55:00

PRE-ANALYSIS DECLARATION:
This is a deterministic forensic report.
No AI interpretation included.
All findings are based on rule-based analysis.
```

### 2. Narrative Structure
```
SECTION 1: NARRATIVE STRUCTURE
======================================================================
Total Sentences Analyzed: 5

Index  | Timestamp            | Sentence
----------------------------------------------------------------------
1      | 2024-01-15           | Payment was made on 2024-01-15
2      | N/A                  | I never received any payment
...
```

### 3. Contradictions Detected
```
SECTION 2: CONTRADICTIONS DETECTED
======================================================================
Total Contradictions Found: 2

CONTRADICTION #1
----------------------------------------------------------------------
Statement A (Line 1):
  "Payment was made on 2024-01-15"

Statement B (Line 2):
  "I never received any payment"

Reason: RULE 2: Denial contradicts evidence - 'no payment' vs 'payment'
```

### 4. Legal Classification
```
SECTION 3: LEGAL CLASSIFICATION
======================================================================

Subject: ShareholderOppression
----------------------------------------------------------------------
Evidence Count: 1

  - Contradiction #1
```

### 5. Summary Findings
```
SECTION 4: SUMMARY FINDINGS
======================================================================

Analysis Summary:
  - Total Sentences: 5
  - Total Contradictions: 2
  - Legal Categories Triggered: 1

Primary Legal Concerns:
  - ShareholderOppression: 1 contradiction(s)

Most Severe Contradiction:
  RULE 2: Denial contradicts evidence
  Between: "Payment was made on 2024-01-15"
      and: "I never received any payment"
```

### 6. Post-Analysis Declaration
```
POST-ANALYSIS DECLARATION
======================================================================

End of deterministic evaluation.
All findings are reproducible and rule-based.
This report complies with forensic standards for court admissibility.
```

**Methods**:
```kotlin
fun build(
    sentences: List<Sentence>,
    contradictions: List<ContradictionResult>,
    legal: List<LegalFinding>
): String
```

## Complete Usage Example

```kotlin
// In CaseDetailActivity.kt
lifecycleScope.launch {
    // LAYER 1: Narrative Engine
    val narrativeEngine = NarrativeEngine()
    narrativeEngine.ingest(allEvidence.toString())
    val sentences = narrativeEngine.normalize()
    
    // LAYER 2: Contradiction Engine
    val contradictionEngine = ContradictionEngine()
    val contradictions = contradictionEngine.analyze(sentences)
    
    // LAYER 3: Classification Engine
    val classificationEngine = ClassificationEngine()
    val legalFindings = classificationEngine.classify(contradictions)
    
    // LAYER 4: Report Engine
    val reportEngine = ReportEngine()
    val report = reportEngine.build(sentences, contradictions, legalFindings)
    
    // Save the report
    FileUtils.saveReport(this@CaseDetailActivity, caseId, report)
}
```

## Engine Immutability Rule

**CRITICAL**: The four engines **MUST ALWAYS** run in this exact order:

1. **Narrative** → 2. **Contradiction** → 3. **Classification** → 4. **Report**

**Why?**
- **Deterministic**: Same input always produces same output
- **Reproducible**: Can be re-run for verification
- **Court-admissible**: Logic never varies between cases
- **Non-AI**: Rule-based, no machine learning

**Violation**: Changing the order or skipping layers invalidates the forensic integrity of the report.

## Key Characteristics

✅ **Deterministic**: Logic never changes based on evidence
✅ **Rule-based**: No AI or machine learning
✅ **Reproducible**: Can be re-run with identical results
✅ **Offline**: No external API calls
✅ **Court-safe**: Compliant with forensic standards
✅ **Immutable**: Engine order is fixed

## File Summary

| Layer | File | Lines | Purpose |
|-------|------|-------|---------|
| 1 | `NarrativeEngine.kt` | 115 | Text normalization |
| 2 | `ContradictionEngine.kt` | 287 | Contradiction detection |
| 3 | `ClassificationEngine.kt` | 109 | Legal classification |
| 4 | `ReportEngine.kt` | 198 | Report generation |
| - | `Sentence.kt` | 13 | Data model |
| - | `ContradictionResult.kt` | 7 | Data model |
| - | `LegalSubject.kt` | 9 | Enum |
| - | `LegalFinding.kt` | 8 | Data model |

**Total**: 8 files, ~750 lines of deterministic forensic logic

## Testing

To test the complete pipeline:

1. Create a case in MainActivity
2. Add evidence with contradictions:
   ```
   I never met John Smith.
   We met on January 15th to discuss the deal.
   There was one meeting about the payment.
   We had three meetings about the payment.
   I did not access the system.
   Login attempt recorded at 14:35.
   ```
3. Generate report
4. View structured forensic output with:
   - All 6 sentences indexed
   - 3 contradictions detected (RULE 1, RULE 4, RULE 7)
   - Legal classifications applied
   - Severity analysis included

## Output Path

Reports are saved to: `/cases/{caseId}/contradictions.txt`

## Court Admissibility

This engine complies with forensic standards because:
- No AI interpretation
- Fully deterministic
- Reproducible results
- Rule-based analysis
- Timestamps preserved
- Chain of custody maintained via file system

---

**End of Documentation**

For implementation details, see the source files in `app/src/main/java/org/verumomnis/engine/`
