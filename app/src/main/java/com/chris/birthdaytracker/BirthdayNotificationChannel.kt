package com.chris.birthdaytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val BIRTHDAY_CHANNEL_ID = "birthday_notifications"

fun createBirthdayNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            BIRTHDAY_CHANNEL_ID,
            "Birthdays",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Birthday reminders and alerts"
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
