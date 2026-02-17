package com.chris.birthdaytracker

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class BirthdayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
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
}
