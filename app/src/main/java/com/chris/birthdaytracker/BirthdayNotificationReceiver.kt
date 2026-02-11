package com.chris.birthdaytracker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class BirthdayNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Ensure the channel exists before trying to post a notification.
        createBirthdayNotificationChannel(context)

        val title = intent.getStringExtra("title") ?: "Birthday Tracker"
        val text = intent.getStringExtra("text") ?: return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, com.chris.birthdaytracker.BIRTHDAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        manager.notify(text.hashCode(), notification)
    }
}
