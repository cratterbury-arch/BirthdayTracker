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

# Fix for R8 warnings regarding missing default constructor patterns
# Using the recommended { void <init>(); } pattern for all reported classes

-keep public class com.google.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.android.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.google.android.vending.licensing.ILicensingService { void <init>(); }
-keep class android.support.annotation.Keep { void <init>(); }
-keep public class * extends androidx.glance.appwidget.action.ActionCallback { void <init>(); }
-keep,allowshrinking class * extends androidx.compose.ui.node.ModifierNodeElement { void <init>(); }
-keep class * extends androidx.work.Worker { void <init>(); }
-keep class * extends androidx.work.InputMerger { void <init>(); }
-keep class androidx.work.WorkerParameters { void <init>(); }
-keep class * extends androidx.room.RoomDatabase { void <init>(); }
-keep class * implements androidx.versionedparcelable.VersionedParcelable { void <init>(); }
-keep public class androidx.versionedparcelable.ParcelImpl { void <init>(); }
-keep,allowshrinking class okhttp3.internal.publicsuffix.PublicSuffixDatabase { void <init>(); }
-keep,allowshrinking class * extends androidx.startup.Initializer { void <init>(); }
-keep,allowobfuscation @interface androidx.annotation.Keep { void <init>(); }
