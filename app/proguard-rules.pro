# Add project specific ProGuard rules here.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#keep ALL but com.soapp
-keep class !com.soapp.** { *; }
-keep interface !com.soapp.** { *; }

#for google
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#for soappcompressor
-dontwarn com.googlecode.mp4parser.**

#keep all soapp models
-keep class com.soapp.SoappModel.**
-keepclassmembers class com.soapp.SoappModel.** { *; }
-keep class com.soapp.SoappApi.ApiModel.**
-keepclassmembers class com.soapp.SoappApi.ApiModel.** { *; }

#test keep incoming listener
#-keep class com.soapp.xmpp.SingleChatHelper.IncomingMessageListener

#okhttp + retrofit
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.OkHttpCall
-dontwarn retrofit2.OkHttpCall
-dontwarn com.squareup.okhttp3.**
-dontwarn org.conscrypt.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }

#smack
-keep class org.jivesoftware.smack.** { *; }
-keep class org.jivesoftware.smackx.** { *; }

#webrtc (calling)
-keep class org.webrtc.** { *; }

#[JAY] systemjob - API28
-dontwarn android.app.job.JobParameters

#[JAY] deep linking
-dontwarn io.branch.**

#[JAY] searchview - soappTab > contact
-keep class androidx.appcompat.widget.SearchView
-keep interface androidx.appcompat.widget.SearchView

#[JAY] For crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-dontwarn com.crashlytics.**