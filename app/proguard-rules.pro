# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep forensic engine classes
-keep class org.verumomnis.forensic.** { *; }

# iText PDF rules
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# SLF4J logging rules (required by iTextPDF)
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# ZXing QR code rules
-keep class com.google.zxing.** { *; }

# Keep cryptographic classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
