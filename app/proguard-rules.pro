# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
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

#Native
-keepclasseswithmembernames class * {
    native <methods>;
}

-dontwarn org.apache.**

-keep class org.apache.commons.logging.**

-keep class org.gdg.frisbee.android.api.** { *; }
-keep class org.gdg.frisbee.android.cache.** { *; }
-keep class org.gdg.frisbee.android.utils.** { *; }
-keep class com.google.api.** { *; }

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-dontnote com.google.vending.licensing.ILicensingService
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe
