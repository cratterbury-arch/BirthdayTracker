package com.chris.birthdaytracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object BirthdayNotificationScheduler {

    suspend fun scheduleBirthdayNotifications(
        context: Context,
        contacts: List<ContactModel>
    ) {
        val enabled = SettingsStore.notificationsEnabled(context).first()
        if (!enabled) return

        val notificationDays = SettingsStore.notificationDays(context).first()
        val notificationTimeStr = SettingsStore.notificationTime(context).first()
        val favoritesOnly = SettingsStore.favoritesOnlyNotifications(context).first()

        val timeParts = notificationTimeStr.split(":")
        val notificationTime = LocalTime.of(timeParts[0].toInt(), timeParts[1].toInt())

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        contacts.forEach { contact ->
            if (favoritesOnly && !contact.isFavorite) return@forEach

            val birthday = contact.birthday ?: return@forEach
            val today = LocalDate.now()

            val next = birthday.withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }

            notificationDays.forEach { dayOffset ->
                val offset = dayOffset.toLong()
                val scheduleDate = next.minusDays(offset)
                
                val message = when(offset) {
                    0L -> "It's ${contact.name}'s birthday today! 🎉"
                    1L -> "Reminder: ${contact.name}'s birthday is tomorrow! 🎂"
                    else -> "Reminder: ${contact.name}'s birthday is in $offset days 🎂"
                }

                scheduleNotification(
                    context,
                    alarmManager,
                    scheduleDate,
                    notificationTime,
                    message,
                    "${contact.id}_$dayOffset"
                )
            }
        }
    }

    private fun scheduleNotification(
        context: Context,
        alarmManager: AlarmManager,
        date: LocalDate,
        time: LocalTime,
        message: String,
        uniqueId: String
    ) {
        val intent = Intent(context, BirthdayNotificationReceiver::class.java).apply {
            putExtra("title", "Birthday Tracker")
            putExtra("text", message)
        }
        val requestCode = uniqueId.hashCode()

        val triggerTime = date
            .atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Don't schedule alarms for the past
        if (triggerTime < System.currentTimeMillis()) {
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            putExtra("title", "Test Notification 🎂")
            putExtra("text", "This is a test birthday reminder")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + 5_000 // 5 seconds

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }
}
