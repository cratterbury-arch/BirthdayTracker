package com.chris.birthdaytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.net.toUri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

const val BIRTHDAY_CHANNEL_ID = "birthday_notifications"

fun createBirthdayNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Need to recreate channel if sound changes
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val soundUriString = runBlocking { SettingsStore.notificationSoundUri(context).first() }
        val soundUri = soundUriString?.toUri() ?: "android.resource://${context.packageName}/${R.raw.notification_sound}".toUri()
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        // Delete existing channel to force sound update (optional, but standard way to change sound on O+)
        manager.deleteNotificationChannel(BIRTHDAY_CHANNEL_ID)

        val channel = NotificationChannel(
            BIRTHDAY_CHANNEL_ID,
            "Birthdays",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Birthday reminders and alerts"
            setSound(soundUri, audioAttributes)
        }

        manager.createNotificationChannel(channel)
    }
}
