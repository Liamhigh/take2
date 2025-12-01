# Verum Omnis Documentation

This directory contains the core documentation and templates for the Verum Omnis forensic engine.

## PDF Documents

### Verum_Omnis_Full_Template_v5.1.1.pdf
The complete template containing the full logic and source code for the Verum Omnis forensic engine. This document defines:
- Forensic evidence processing workflows
- Cryptographic sealing procedures (SHA-512)
- Report generation templates
- Chain of custody tracking

### Verum_Omnis_Ideal_Logic.pdf
The ideal logic framework document that outlines the reasoning and decision-making processes for AI-readable forensic reports. This includes:
- Contradiction detection algorithms
- Timeline analysis methodology
- Evidence mapping standards
- Legal-grade documentation requirements

## Relationship to Constitution

These PDFs implement the standards defined in [`verum-constitution.json`](../verum-constitution.json), which specifies:

- **Hash Standard**: SHA-512 for cryptographic operations
- **PDF Standard**: PDF 1.7 format
- **Security Requirements**: Offline-first, stateless operation
- **Forensic Rules**: Mandatory seal, tamper detection, QR code inclusion
- **Output Requirements**: Machine and human readable, audit trail enabled

## Usage

These documents serve as the reference implementation for building the Android forensic engine. The source code embedded within the PDFs, combined with the constitutional governance layer, enables:

1. **Offline Processing**: All forensic analysis performed locally on device
2. **Cryptographic Sealing**: Evidence cryptographically sealed with SHA-512
3. **Legal-Grade Reports**: AI-readable narratives for legal consultation
4. **Jurisdiction Awareness**: Location and timestamp metadata for legal context
