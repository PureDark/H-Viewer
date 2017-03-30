# Add project specific ProGuard sites here.
# By default, the flags in this file are appended to flags specified
# in D:\Coding\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
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

#保留R下面的资源
-keep class **.R$* {*;}

-keep class org.apache.** { *; }
-dontwarn org.apache.**

-keep class com.sun.mail.** { *; }
-dontwarn com.sun.mail.**

-keep class java.beans.** { *; }
-dontwarn java.beans.**

-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

-keep class org.json.** { *; }
-dontwarn org.json.**

-keep class java.lang.invoke.** { *; }
-dontwarn java.lang.invoke.**

-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.Unsafe

-keep class ml.puredark.hviewer.beans.** { *; }
-dontwarn ml.puredark.hviewer.beans.**

-keep class com.umeng.** {*; }
-dontwarn com.umeng.**

# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.app.Notification

-dontwarn okhttp3.**


-keepclassmembers class * {
   public <init>(org.json.JSONObject);
    @android.webkit.JavascriptInterface <methods>;
}

-keep public class com.idea.fifaalarmclock.app.R$*{
    public static final int *;
}

-keep public class com.umeng.fb.ui.ThreadView {
}

-keep public class * extends com.umeng.**

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}

-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

-keep class com.facebook.stetho.** {
  *;
}

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep public class org.jsoup.** {
    public *;
}

-keep class tv.danmaku.ijk.media.player.** { *; }
-dontwarn tv.danmaku.ijk.media.player.*
-keep interface tv.danmaku.ijk.media.player.** { *; }
