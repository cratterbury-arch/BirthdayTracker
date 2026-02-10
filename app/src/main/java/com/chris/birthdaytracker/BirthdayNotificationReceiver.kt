package com.chris.birthdaytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class BirthdayNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Birthday Tracker"
        val text = intent.getStringExtra("text") ?: return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "birthday_channel",
            "Birthday reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "birthday_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        manager.notify(text.hashCode(), notification)
    }
}
