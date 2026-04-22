# Birthday Tracker ProGuard Rules

# 1. Keep data models and enums
# This ensures that your ContactModel and Source enum aren't renamed,
# which prevents database issues and runtime crashes when passing data.
-keep class com.chris.birthdaytracker.ContactModel { *; }
-keep class com.chris.birthdaytracker.ContactSource { *; }

# 2. Room Rules
# Required to prevent Room from renaming the generated classes.
-keep class * extends androidx.room.RoomDatabase
-keep class com.chris.birthdaytracker.ContactDao { *; }
-keep class com.chris.birthdaytracker.ContactEntity { *; }

# 3. WorkManager Rules
# Required for background tasks (notifications).
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# 4. Glance (Widgets) Rules
# Keeps the widget receiver and callback classes.
-keep class * extends androidx.glance.appwidget.action.ActionCallback { void <init>(); }
-keep class com.chris.birthdaytracker.BirthdayWidgetReceiver { *; }

# 5. Generic Fixes for R8 Warnings
-keep public class com.google.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.android.vending.licensing.ILicensingService { void <init>(); }
-keep public class com.google.android.vending.licensing.ILicensingService { void <init>(); }
-keep class android.support.annotation.Keep { void <init>(); }
-keep,allowshrinking class * extends androidx.compose.ui.node.ModifierNodeElement { void <init>(); }
-keep class androidx.work.WorkerParameters { void <init>(); }
-keep class * implements androidx.versionedparcelable.VersionedParcelable { void <init>(); }
-keep public class androidx.versionedparcelable.ParcelImpl { void <init>(); }
-keep,allowshrinking class okhttp3.internal.publicsuffix.PublicSuffixDatabase { void <init>(); }
-keep,allowshrinking class * extends androidx.startup.Initializer { void <init>(); }
-keep,allowobfuscation @interface androidx.annotation.Keep { void <init>(); }
