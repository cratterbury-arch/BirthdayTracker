package com.chris.birthdaytracker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import androidx.work.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BirthdayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        instance = this

        // Apply theme early
        val scope = MainScope()
        scope.launch {
            val theme = SettingsStore.getTheme(this@BirthdayApplication).first()
            applyTheme(theme)
        }
        
        scheduleBirthdayNotifications()
    }

    private fun scheduleBirthdayNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<BirthdayNotificationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "birthday_notification_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private var instance: BirthdayApplication? = null
        private var database: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return database ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "birthday_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                database = db
                db
            }
        }

        fun applyTheme(theme: String) {
            val mode = when (theme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
