package com.chris.birthdaytracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

object BirthdayNotificationScheduler {

    suspend fun scheduleBirthdayNotifications(
        context: Context,
        contacts: List<ContactModel>
    ) {
        val enabled = SettingsStore.notificationsEnabled(context).first()
        if (!enabled) return

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        contacts.forEach { contact ->
            val birthday = contact.birthday ?: return@forEach
            val today = LocalDate.now()

            val next = birthday.withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }

            // 🎂 On the day
            schedule(
                context,
                alarmManager,
                next,
                "It's ${contact.name}'s ${next.year - birthday.year} birthday today! 🎉"
            )

            // ⏰ One week before
            schedule(
                context,
                alarmManager,
                next.minusWeeks(1),
                "Reminder: ${contact.name}'s birthday is next week 🎂"
            )
        }
    }

    private fun schedule(
        context: Context,
        alarmManager: AlarmManager,
        date: LocalDate,
        message: String
    ) {
        val triggerTime = date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, BirthdayNotificationReceiver::class.java).apply {
            putExtra("title", "Birthday Tracker")
            putExtra("text", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            message.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ⚠️ NOT exact → avoids Android 14+ crash
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun scheduleTestNotification(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, BirthdayNotificationReceiver::class.java).apply {
            putExtra("title", "Test notification 🎂")
            putExtra("text", "This is a test birthday reminder")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 10_000,
            pendingIntent
        )
    }
}
