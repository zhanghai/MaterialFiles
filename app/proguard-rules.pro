# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# Apache Commons Compress
-dontwarn org.apache.commons.compress.compressors.**
-dontwarn org.apache.commons.compress.archivers.**

# EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
# For DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

# RecyclerView FastScroll
# https://github.com/timusus/RecyclerView-FastScroll/pull/96
-dontwarn com.simplecityapps.recyclerview_fastscroll.views.FastScrollPopup

# Stetho No-op
# This library includes the no-op for stetho-okhttp3 which requires okhttp3, but we never used it.
-dontwarn com.facebook.stetho.okhttp3.StethoInterceptor
