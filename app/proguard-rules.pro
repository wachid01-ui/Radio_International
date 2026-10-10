# Keep Kotlinx Serialization models
-keepclassmembers class com.magic.music.stream.data.model.** { *; }
-keepclassmembers class com.magic.music.stream.data.api.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Media3 classes
-keep class androidx.media3.** { *; }
-keep class androidx.media.** { *; }
