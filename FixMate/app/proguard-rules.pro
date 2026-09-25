# Add project specific ProGuard rules here.
# By default the flags in this file are appended to flags specified
# in {sdk}/tools/proguard/proguard-android-optimize.txt

# Keep model classes intact (they are parsed reflectively from JSON by name).
-keep class com.fixmate.model.** { *; }
