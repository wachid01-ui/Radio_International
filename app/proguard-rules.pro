# Keep Kotlinx Serialization models
-keepclassmembers class com.magic.music.stream.data.model.** { *; }
-keepclassmembers class com.magic.music.stream.data.api.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Media3 classes
-keep class androidx.media3.** { *; }
-keep class androidx.media.** { *; }

# OkHttp & Ktor
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# Google Play Services Ads & UMP
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.**
