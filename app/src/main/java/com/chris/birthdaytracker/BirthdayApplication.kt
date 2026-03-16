package com.chris.birthdaytracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BirthdayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
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
