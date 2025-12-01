# Verum Omnis Forensic App Proguard Rules
# Protect cryptographic seal functionality

# Keep crypto classes
-keep class com.verumomnis.forensic.crypto.** { *; }
-keep class com.verumomnis.forensic.evidence.** { *; }

# Keep seal data models
-keepclassmembers class com.verumomnis.forensic.core.ForensicSeal { *; }
-keepclassmembers class com.verumomnis.forensic.core.EvidencePackage { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Security classes
-keep class androidx.security.** { *; }
