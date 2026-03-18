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
-keep public class com.google.vending.licensing.ILicensingService { <init>(); }
-keep public class com.android.vending.licensing.ILicensingService { <init>(); }
-keep public class com.google.android.vending.licensing.ILicensingService { <init>(); }
-keep class android.support.annotation.Keep { <init>(); }
-keep public class * extends androidx.glance.appwidget.action.ActionCallback { <init>(); }
-keep,allowshrinking class * extends androidx.compose.ui.node.ModifierNodeElement { <init>(); }
-keep class * extends androidx.work.Worker { <init>(); }
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class androidx.work.WorkerParameters { <init>(); }
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * implements androidx.versionedparcelable.VersionedParcelable { <init>(); }
-keep public class androidx.versionedparcelable.ParcelImpl { <init>(); }
-keep,allowshrinking class okhttp3.internal.publicsuffix.PublicSuffixDatabase { <init>(); }
-keep,allowshrinking class * extends androidx.startup.Initializer { <init>(); }
-keep,allowobfuscation @interface androidx.annotation.Keep { <init>(); }
