# GPS Jurisdiction Law Integration

## Overview

This document describes the integration of GPS-based jurisdiction detection with the Verum Omnis forensic evidence system, ensuring compliance with jurisdiction-specific legal standards for evidence handling, timestamps, and PDF reporting.

## Problem Statement

The system needed to:
1. **GPS for jurisdiction law** - Use GPS location to automatically detect and apply jurisdiction-specific legal standards
2. **Accurate timestamps** - Format timestamps according to jurisdiction requirements (timezone, format)
3. **Reliable PDF engine** - Generate jurisdiction-compliant forensic PDF reports
4. **Best narrative** - Create jurisdiction-aware forensic narratives

## Implementation

### 1. GPS-Based Jurisdiction Detection

**File**: `app/src/main/java/org/verumomnis/forensic/core/ForensicEngine.kt`

Added `detectJurisdiction()` method that uses GPS coordinates to determine the applicable legal jurisdiction:

```kotlin
fun detectJurisdiction(location: ForensicLocation?): Jurisdiction {
    // Uses latitude/longitude bounds to detect:
    // - UAE: 22.5°-26.0°N, 51.0°-56.5°E
    // - South Africa: -35.0° to -22.0°S, 16.0°-33.0°E
    // - European Union: 35.0°-71.0°N, -10.0°-40.0°E
    // - United States: Default for other locations
}
```

**Integration Point**: When evidence is added to a case, if GPS location is available, the jurisdiction is automatically detected and stored in the `ForensicCase.jurisdiction` field.

### 2. Jurisdiction-Specific Timestamps

**File**: `app/src/main/java/org/verumomnis/forensic/jurisdiction/JurisdictionComplianceEngine.kt`

Each jurisdiction has its own timestamp formatter:

- **UAE**: Dubai timezone (Asia/Dubai) - `yyyy-MM-dd'T'HH:mm:ssXXX`
- **South Africa**: Johannesburg timezone (Africa/Johannesburg) - `yyyy-MM-dd'T'HH:mm:ssXXX`
- **European Union**: Brussels timezone (Europe/Brussels) - `yyyy-MM-dd'T'HH:mm:ssXXX`
- **United States**: Eastern timezone (America/New_York) - `yyyy-MM-dd'T'HH:mm:ssXXX`

All timestamps in reports are formatted according to the detected jurisdiction's requirements.

### 3. Jurisdiction-Compliant PDF Reports

**File**: `app/src/main/java/org/verumomnis/forensic/pdf/ForensicPdfGenerator.kt`

PDF reports now include:

1. **Cover Page** - Shows jurisdiction name and code
2. **Jurisdiction Compliance Section** - Lists applicable legal standards:
   - UAE: Federal Evidence Law, Electronic Transactions Law
   - South Africa: ECT Act, SAPS Digital Evidence Guidelines
   - European Union: GDPR, eIDAS standards
   - United States: Federal Rules of Evidence, Daubert Standard

3. **Jurisdiction-Specific Footer** - Includes compliance statements and legal references

4. **Accurate Timestamps** - All timestamps use jurisdiction-specific formatting

### 4. Jurisdiction-Aware Narratives

**File**: `app/src/main/java/org/verumomnis/forensic/report/ForensicNarrativeGenerator.kt`

The narrative generator now:

1. Accepts an optional `Jurisdiction` parameter
2. Formats all timestamps using jurisdiction-specific formatters
3. Includes a "JURISDICTION COMPLIANCE" section with:
   - Applicable legal framework
   - Data protection requirements
   - Legal disclaimers
   - Timestamp format information

## API Changes

### ForensicCase

Added optional `jurisdiction` field:

```kotlin
data class ForensicCase(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val directory: File,
    val evidenceItems: MutableList<ForensicEvidence>,
    var jurisdiction: Jurisdiction? = null  // NEW
)
```

### ForensicEngine

New method:

```kotlin
fun detectJurisdiction(location: ForensicLocation?): Jurisdiction
```

Updated method:

```kotlin
suspend fun addEvidence(...): ForensicEvidence {
    // Now auto-detects jurisdiction from GPS location
}

suspend fun generateForensicReport(
    case: ForensicCase,
    tripleHashSeal: ForensicTripleHashSeal? = null
): File {
    // Now passes jurisdiction to PDF generator and narrative generator
}
```

### ForensicNarrativeGenerator

Updated signature:

```kotlin
fun generateNarrative(
    case: ForensicCase, 
    jurisdiction: Jurisdiction? = null
): String
```

### ForensicPdfGenerator

Updated signature:

```kotlin
fun generateForensicReport(
    case: ForensicCase,
    narrative: String,
    tripleHashSeal: ForensicTripleHashSeal?,
    custodyLogger: ChainOfCustodyLogger?,
    jurisdiction: Jurisdiction = Jurisdiction.UNITED_STATES  // NEW
): File
```

## Supported Jurisdictions

1. **UAE** (United Arab Emirates)
   - Standards: Federal Evidence Law, Electronic Transactions Law
   - Timezone: Asia/Dubai
   - Languages: Arabic (RTL), English
   - Data Protection: Federal Decree-Law No. 45/2021

2. **South Africa**
   - Standards: ECT Act Section 15, SAPS Guidelines
   - Timezone: Africa/Johannesburg
   - Languages: English
   - Data Protection: POPIA

3. **European Union**
   - Standards: GDPR, eIDAS
   - Timezone: Europe/Brussels
   - Languages: English
   - Data Protection: GDPR

4. **United States**
   - Standards: Federal Rules of Evidence, Daubert Standard
   - Timezone: America/New_York
   - Languages: English
   - Data Protection: State Privacy Laws (CCPA, etc.)

## Testing

Added new test cases:

```kotlin
@Test
fun `generateNarrative includes jurisdiction compliance section`()

@Test
fun `generateNarrative uses jurisdiction-specific information`()
```

## Benefits

1. **Legal Compliance**: Automatic compliance with jurisdiction-specific evidence handling standards
2. **Accurate Timestamps**: All timestamps use the correct timezone and format for the jurisdiction
3. **Court Admissibility**: Reports include all required legal disclosures and standards references
4. **GPS Integration**: Jurisdiction is automatically detected from evidence collection location
5. **Multi-Jurisdiction Support**: Single system can generate legally compliant reports for multiple jurisdictions

## Usage Example

```kotlin
val forensicEngine = ForensicEngine(context)

// Create case
val case = forensicEngine.createNewCase("Investigation Case 2024")

// Add evidence with GPS location - jurisdiction auto-detected
val evidence = forensicEngine.addEvidence(
    case = case,
    evidenceType = EvidenceType.DOCUMENT,
    description = "Contract document",
    data = documentBytes
)

// Generate jurisdiction-compliant report
val report = forensicEngine.generateForensicReport(case)

// Report will include:
// - Jurisdiction-specific timestamps (e.g., Dubai time for UAE)
// - Applicable legal standards (e.g., UAE Federal Evidence Law)
// - Proper legal disclaimers
// - Jurisdiction-specific footer
```

## Future Enhancements

1. **More Jurisdictions**: Add support for additional jurisdictions (Australia, Canada, etc.)
2. **Manual Override**: Allow manual jurisdiction selection
3. **Multi-Language Support**: Full translations for non-English jurisdictions
4. **Jurisdiction-Specific Watermarks**: Custom watermarks per jurisdiction
5. **Legal Template Updates**: Regular updates to legal standards and requirements

## Compliance Notes

- All jurisdiction detection is based on GPS coordinates and is approximate
- Users should verify jurisdiction applicability for border cases
- Legal disclaimers are included in all reports
- Timestamp accuracy depends on device clock synchronization
- Reports should be reviewed by qualified legal professionals before court submission
