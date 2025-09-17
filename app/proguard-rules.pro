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

# Tuya/Thingclips SDK Proguard rules

-keep class com.tuya.** { *; }
-keep class com.thingclips.** { *; }
-keep class com.tutk.** { *; }
-keep class com.alibaba.fastjson.** { *; }
-keep class com.facebook.** { *; }
-keep class com.lzy.okgo.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okio.** { *; }
-keep class javax.** { *; }
-keep class org.** { *; }
-keep class com.google.zxing.** { *; }
-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-keep class com.huawei.** { *; }
-keep class com.hikvision.** { *; }
-keep class com.qihoo.** { *; }
-keep class com.tencent.** { *; }
-keep class com.alibaba.sdk.android.** { *; }
-keep class com.taobao.** { *; }

# Gson
# -keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Fastjson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Tuya Security
-keep class com.thingclips.smart.android.sweeper.** { *; }
-keep class com.thingclips.smart.android.ble.api.** { *; }
-keep class com.thingclips.smart.android.ble.bean.** { *; }
-keep class com.thingclips.smart.android.ble.parser.** { *; }

# Prevent removing classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all annotations
-keepattributes *Annotation*

# (Optional) Keep your Application class if referenced via manifest
-keep class com.cliffgor.mysmartlock.LockTuyaApplication { *; }