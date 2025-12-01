# Verum Omnis Forensic Engine Proguard Rules

# Keep all forensic data classes
-keep class org.verumomnis.forensic.core.** { *; }
-keep class org.verumomnis.forensic.crypto.** { *; }
-keep class org.verumomnis.forensic.report.** { *; }

# Keep cryptographic classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# iText PDF
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# ZXing QR
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Location services
-keep class com.google.android.gms.location.** { *; }
