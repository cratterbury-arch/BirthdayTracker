package com.chris.birthdaytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chris.birthdaytracker.MainActivity // ✅ ADD THIS

class BirthdayNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("NAME") ?: "Someone"
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "bday_channel",
            "Birthdays",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "bday_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Birthday Alert! 🎂")
            .setContentText("It is $name's birthday today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(name.hashCode(), notification)
    }
}
