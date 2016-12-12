-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod




-keep class com.google.android.gms.ads.identifier.** { *; }

# joda time
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# jackson
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.**
-keepclassmembers public final enum
org.codehaus.jackson.annotate.JsonAutoDetect$Visibility { public
static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility
*; }
-dontwarn okhttp3.**
-dontwarn okio.**

# gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# Retrofit 2.X
## https://square.github.io/retrofit/ ##
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
@retrofit2.http.* <methods>;
}

# Twilio Video SDK
-keep class org.webrtc.** { *; }
-keep class com.twilio.video.** { *; }

# Pubnub
-dontwarn com.pubnub.**
-keep class com.pubnub.** { *; }
