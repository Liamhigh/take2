#!/usr/bin/env python3
"""
Generate rule assets from Verum Omnis templates

This script generates the JSON rule files for the Verum Omnis Forensic Engine.
Run this script to regenerate or update the rule assets.

Usage:
    python scripts/generate-assets.py
"""

import json
import os
from pathlib import Path


def create_rule_assets():
    """Creates all rule asset files for the forensic engine."""
    assets_dir = Path("app/src/main/assets/rules")
    assets_dir.mkdir(parents=True, exist_ok=True)

    # Verum Rules (main configuration)
    verum_rules = {
        "version": "5.1.1",
        "description": "Verum Omnis Forensic Analysis Rules",
        "hash_standard": "SHA-512",
        "legal_subjects": [
            {
                "name": "Shareholder Oppression",
                "keywords": ["denied meeting", "withheld financial", "exclusion", "minority shareholder", "board exclusion"],
                "severity": "HIGH",
                "category": "corporate"
            },
            {
                "name": "Breach of Fiduciary Duty",
                "keywords": ["self-dealing", "conflict of interest", "misappropriation", "breach of trust", "negligence"],
                "severity": "HIGH",
                "category": "fiduciary"
            },
            {
                "name": "Cybercrime",
                "keywords": ["unauthorized access", "Gmail", "device logs", "hacking", "data breach", "phishing"],
                "severity": "CRITICAL",
                "category": "criminal"
            },
            {
                "name": "Fraud",
                "keywords": ["forged", "falsified", "misrepresentation", "deceit", "fraudulent"],
                "severity": "CRITICAL",
                "category": "criminal"
            },
            {
                "name": "Contract Breach",
                "keywords": ["breach of contract", "non-performance", "default", "violation"],
                "severity": "MEDIUM",
                "category": "civil"
            },
            {
                "name": "Defamation",
                "keywords": ["defamation", "libel", "slander", "false statement", "reputation damage"],
                "severity": "MEDIUM",
                "category": "civil"
            },
            {
                "name": "Harassment",
                "keywords": ["harassment", "intimidation", "threats", "bullying", "coercion"],
                "severity": "HIGH",
                "category": "criminal"
            }
        ],
        "dishonesty_matrix": {
            "contradictions": {
                "weight": 3,
                "description": "Statements that directly contradict each other or evidence",
                "patterns": [
                    "no deal.*invoice",
                    "denied.*admitted",
                    "refused.*accepted",
                    "never.*always",
                    "no contact.*communication"
                ]
            },
            "omissions": {
                "weight": 2,
                "description": "Evidence of intentional exclusion of relevant information",
                "patterns": [
                    "cropped screenshot",
                    "selective editing",
                    "missing context",
                    "partial disclosure",
                    "redacted without explanation"
                ]
            },
            "fabrications": {
                "weight": 4,
                "description": "Evidence of manufactured or false information",
                "patterns": [
                    "forged document",
                    "fabricated evidence",
                    "false testimony",
                    "manufactured record",
                    "backdated"
                ]
            },
            "deflections": {
                "weight": 2,
                "description": "Attempts to redirect blame or avoid accountability",
                "patterns": [
                    "not my responsibility",
                    "someone else",
                    "I was not aware",
                    "miscommunication"
                ]
            }
        },
        "extraction_protocol": {
            "step1_keywords": [
                "admin", "deny", "forged", "access", "delete", "refuse", "invoice",
                "profit", "payment", "transfer", "meeting", "resolution", "agreement"
            ],
            "step2_tags": [
                "#Cybercrime", "#Fraud", "#Oppression", "#FiduciaryBreach",
                "#ContractBreach", "#Harassment", "#Defamation"
            ],
            "step3_scoring": {
                "low": {"weight": 1, "threshold": 2},
                "medium": {"weight": 2, "threshold": 5},
                "high": {"weight": 3, "threshold": 10}
            }
        },
        "narrative_template": {
            "sections": [
                "executive_summary",
                "timeline_analysis",
                "evidence_facts",
                "contradiction_analysis",
                "legal_subject_assessment",
                "integrity_assessment",
                "recommendations"
            ],
            "format": "human_and_machine_readable"
        }
    }

    with open(assets_dir / "verum_rules.json", "w") as f:
        json.dump(verum_rules, f, indent=2)

    # Dishonesty Matrix
    dishonesty_matrix = {
        "version": "1.0",
        "description": "Dishonesty Detection Matrix for Verum Omnis Forensic Analysis",
        "contradictions": {
            "weight": 3,
            "examples": ["Opposing statements vs evidence", "Timeline inconsistencies"],
            "patterns": [
                {
                    "id": "deal_invoice_contradiction",
                    "regex": "no deal.*invoice|invoice.*no deal",
                    "description": "Contradiction between denying a deal and having an invoice"
                },
                {
                    "id": "denial_admission",
                    "regex": "denied.*admitted|admitted.*denied",
                    "description": "Subject both denied and admitted same fact"
                },
                {
                    "id": "refusal_acceptance",
                    "regex": "refused.*accepted|accepted.*refused",
                    "description": "Contradiction in acceptance/refusal of terms"
                },
                {
                    "id": "presence_absence",
                    "regex": "was present.*was not present|was not present.*was present",
                    "description": "Contradictory claims about presence at event"
                },
                {
                    "id": "timeline_conflict",
                    "regex": "before.*after.*same event",
                    "description": "Conflicting timeline claims"
                }
            ]
        },
        "omissions": {
            "weight": 2,
            "examples": ["Cropped screenshots", "Missing pages", "Selective quoting"],
            "patterns": [
                {
                    "id": "cropped_evidence",
                    "regex": "cropped|cut off|partial|incomplete",
                    "description": "Evidence appears to have been selectively edited"
                },
                {
                    "id": "selective_editing",
                    "regex": "selective.*edit|edited.*version|modified.*original",
                    "description": "Evidence of intentional editing to change meaning"
                },
                {
                    "id": "missing_context",
                    "regex": "missing context|out of context|without context",
                    "description": "Information presented without necessary context"
                },
                {
                    "id": "redaction_unexplained",
                    "regex": "redacted|blacked out|removed|hidden",
                    "description": "Unexplained redactions that may hide relevant information"
                }
            ]
        },
        "fabrications": {
            "weight": 4,
            "examples": ["Forged signatures", "Created documents", "False statements"],
            "patterns": [
                {
                    "id": "forged_document",
                    "regex": "forged|fake|counterfeit|fabricated",
                    "description": "Document appears to be manufactured"
                },
                {
                    "id": "false_signature",
                    "regex": "signature.*mismatch|unauthorized.*signature|forged.*signature",
                    "description": "Signature appears to be unauthorized or forged"
                },
                {
                    "id": "backdated",
                    "regex": "backdated|antedated|false date|wrong date",
                    "description": "Document date appears to be falsified"
                },
                {
                    "id": "metadata_mismatch",
                    "regex": "metadata.*inconsistent|creation date.*mismatch",
                    "description": "Document metadata does not match claimed date"
                }
            ]
        },
        "deflections": {
            "weight": 2,
            "examples": ["Blaming others", "Claiming ignorance", "Diversion tactics"],
            "patterns": [
                {
                    "id": "responsibility_shift",
                    "regex": "not my (responsibility|fault)|someone else|third party",
                    "description": "Attempt to shift responsibility to others"
                },
                {
                    "id": "claimed_ignorance",
                    "regex": "I was not aware|didn't know|wasn't informed",
                    "description": "Claims of ignorance that may be suspicious"
                },
                {
                    "id": "miscommunication",
                    "regex": "misunderstood|miscommunication|wrong interpretation",
                    "description": "Attempting to attribute issues to miscommunication"
                }
            ]
        },
        "scoring": {
            "threshold_low": 3,
            "threshold_medium": 6,
            "threshold_high": 10,
            "max_score": 100
        }
    }

    with open(assets_dir / "dishonesty_matrix.json", "w") as f:
        json.dump(dishonesty_matrix, f, indent=2)

    # Legal Subjects
    legal_subjects = {
        "version": "1.0",
        "description": "Legal Subject Classification for Verum Omnis Forensic Analysis",
        "categories": {
            "criminal": {
                "description": "Criminal law matters requiring potential police/prosecution involvement",
                "subjects": [
                    {
                        "id": "cybercrime",
                        "name": "Cybercrime",
                        "severity": "CRITICAL",
                        "keywords": [
                            "unauthorized access", "hacking", "data breach", "computer fraud",
                            "phishing", "malware", "ransomware", "identity theft",
                            "Gmail access", "account takeover"
                        ]
                    },
                    {
                        "id": "fraud",
                        "name": "Fraud",
                        "severity": "CRITICAL",
                        "keywords": [
                            "fraud", "fraudulent", "forged", "falsified",
                            "misrepresentation", "deceit", "embezzlement", "theft", "larceny"
                        ]
                    },
                    {
                        "id": "harassment",
                        "name": "Criminal Harassment",
                        "severity": "HIGH",
                        "keywords": [
                            "harassment", "stalking", "threats", "intimidation",
                            "coercion", "extortion", "blackmail"
                        ]
                    }
                ]
            },
            "corporate": {
                "description": "Corporate law and governance matters",
                "subjects": [
                    {
                        "id": "shareholder_oppression",
                        "name": "Shareholder Oppression",
                        "severity": "HIGH",
                        "keywords": [
                            "denied meeting", "withheld financial", "exclusion",
                            "minority shareholder", "board exclusion", "dividend denial",
                            "information withholding"
                        ]
                    },
                    {
                        "id": "fiduciary_breach",
                        "name": "Breach of Fiduciary Duty",
                        "severity": "HIGH",
                        "keywords": [
                            "self-dealing", "conflict of interest", "misappropriation",
                            "breach of trust", "negligence", "director misconduct"
                        ]
                    }
                ]
            },
            "civil": {
                "description": "Civil law matters for private legal action",
                "subjects": [
                    {
                        "id": "contract_breach",
                        "name": "Breach of Contract",
                        "severity": "MEDIUM",
                        "keywords": [
                            "breach of contract", "non-performance", "default",
                            "violation", "failed to deliver", "broken agreement"
                        ]
                    },
                    {
                        "id": "defamation",
                        "name": "Defamation",
                        "severity": "MEDIUM",
                        "keywords": [
                            "defamation", "libel", "slander", "false statement",
                            "reputation damage", "malicious publication"
                        ]
                    },
                    {
                        "id": "negligence",
                        "name": "Negligence",
                        "severity": "MEDIUM",
                        "keywords": [
                            "negligence", "duty of care", "breach of duty",
                            "damages", "causation"
                        ]
                    }
                ]
            }
        },
        "severity_levels": {
            "CRITICAL": {
                "weight": 4,
                "description": "Requires immediate attention, potential criminal liability",
                "action": "Recommend immediate legal consultation and potential law enforcement referral"
            },
            "HIGH": {
                "weight": 3,
                "description": "Significant legal exposure, urgent attention needed",
                "action": "Recommend legal consultation within 48 hours"
            },
            "MEDIUM": {
                "weight": 2,
                "description": "Moderate legal concern, should be addressed",
                "action": "Recommend legal review within reasonable timeframe"
            },
            "LOW": {
                "weight": 1,
                "description": "Minor concern, may not require immediate action",
                "action": "Monitor and document for potential future use"
            }
        }
    }

    with open(assets_dir / "legal_subjects.json", "w") as f:
        json.dump(legal_subjects, f, indent=2)

    # Extraction Protocol
    extraction_protocol = {
        "version": "1.0",
        "description": "Evidence Extraction Protocol for Verum Omnis Forensic Analysis",
        "step1_keywords": {
            "description": "Primary keywords to scan for during initial evidence analysis",
            "administrative": [
                "admin", "administrator", "access", "permission", "authorization", "credentials"
            ],
            "actions": [
                "deny", "refuse", "delete", "remove", "block", "restrict", "suspend"
            ],
            "documents": [
                "forged", "fake", "falsified", "invoice", "contract", "agreement", "receipt"
            ],
            "financial": [
                "payment", "transfer", "profit", "dividend", "transaction", "account", "balance"
            ],
            "corporate": [
                "meeting", "resolution", "board", "shareholder", "director", "company"
            ]
        },
        "step2_tags": {
            "description": "Forensic tags to apply based on keyword matches",
            "criminal": [
                "#Cybercrime", "#Fraud", "#Harassment", "#Theft", "#Forgery"
            ],
            "corporate": [
                "#Oppression", "#FiduciaryBreach", "#Mismanagement", "#Exclusion"
            ],
            "civil": [
                "#ContractBreach", "#Negligence", "#Defamation"
            ],
            "procedural": [
                "#EvidenceTampering", "#ChainOfCustody", "#TimelineInconsistency"
            ]
        },
        "step3_scoring": {
            "description": "Scoring weights for evidence relevance and severity",
            "low": {
                "weight": 1,
                "min_matches": 1,
                "max_matches": 2,
                "color": "#4CAF50",
                "label": "Minor Concern"
            },
            "medium": {
                "weight": 2,
                "min_matches": 3,
                "max_matches": 5,
                "color": "#FF9800",
                "label": "Moderate Concern"
            },
            "high": {
                "weight": 3,
                "min_matches": 6,
                "max_matches": None,
                "color": "#F44336",
                "label": "Critical Concern"
            }
        },
        "extraction_settings": {
            "case_sensitive": False,
            "whole_word_match": False,
            "include_synonyms": True,
            "max_context_chars": 200,
            "highlight_matches": True
        },
        "output_format": {
            "include_timestamp": True,
            "include_location": True,
            "include_context": True,
            "include_score": True,
            "include_tags": True
        }
    }

    with open(assets_dir / "extraction_protocol.json", "w") as f:
        json.dump(extraction_protocol, f, indent=2)

    print("✅ Rule assets generated successfully")
    print(f"   Generated files in: {assets_dir.absolute()}")
    print("   - verum_rules.json")
    print("   - dishonesty_matrix.json")
    print("   - legal_subjects.json")
    print("   - extraction_protocol.json")


def validate_assets():
    """Validates the generated assets."""
    assets_dir = Path("app/src/main/assets/rules")
    
    required_files = [
        "verum_rules.json",
        "dishonesty_matrix.json",
        "legal_subjects.json",
        "extraction_protocol.json"
    ]
    
    all_valid = True
    for filename in required_files:
        filepath = assets_dir / filename
        if filepath.exists():
            try:
                with open(filepath) as f:
                    json.load(f)
                print(f"✅ {filename} - Valid JSON")
            except json.JSONDecodeError as e:
                print(f"❌ {filename} - Invalid JSON: {e}")
                all_valid = False
        else:
            print(f"❌ {filename} - File not found")
            all_valid = False
    
    return all_valid


if __name__ == "__main__":
    print("Verum Omnis Asset Generator")
    print("=" * 40)
    create_rule_assets()
    print()
    print("Validating generated assets...")
    print("-" * 40)
    if validate_assets():
        print()
        print("✅ All assets generated and validated successfully")
    else:
        print()
        print("❌ Some assets failed validation")
        exit(1)
