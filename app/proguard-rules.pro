# Proguard rules for Rohit's ToolKit
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-dontwarn com.tom_roush.pdfbox.**
