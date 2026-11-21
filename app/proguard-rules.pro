# MapLibre Native rules
-keep class org.maplibre.android.** { *; }
-keep interface org.maplibre.android.** { *; }

# Retrofit 2 & Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
