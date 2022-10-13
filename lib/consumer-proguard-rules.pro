# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in H:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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

# ———————— 微信 ————————

-keep class com.tencent.mm.opensdk.** { *; }
-keep class com.tencent.wxop.** { *; }
-keep class com.tencent.mm.sdk.** { *; }

# ———————— 微博 ————————

-keep class com.sina.weibo.sdk.api.* { *; }

# ———————— qq ————————

-keep class * extends android.app.Dialog { *; }
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* { *; }
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog { *; }
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* { *; }