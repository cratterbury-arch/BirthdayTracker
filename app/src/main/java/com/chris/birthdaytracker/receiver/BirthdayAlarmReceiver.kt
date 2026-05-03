package com.chris.birthdaytracker.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import com.chris.birthdaytracker.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BirthdayAlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val today = Calendar.getInstance()
        val todayMonth = today.get(Calendar.MONTH) + 1 // Adjust for Calendar's 0-based months
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val upcomingBirthdays = getUpcomingBirthdays(context, todayMonth, todayDay)

        if (upcomingBirthdays.isNotEmpty()) {
            showNotification(context, upcomingBirthdays)
        }
    }

    private fun getUpcomingBirthdays(context: Context, month: Int, day: Int): List<String> {
        val upcoming = mutableListOf<String>()
        val projection = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection, selection, selectionArgs, null
        )?.use { cursor ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            while (cursor.moveToNext()) {
                val name = cursor.getString(0) ?: "Unknown"
                val startDate = cursor.getString(1) ?: ""
                try {
                    val birthday = sdf.parse(startDate)
                    if (birthday != null) {
                        val cal = Calendar.getInstance().apply { time = birthday }
                        if (cal.get(Calendar.MONTH) + 1 == month && cal.get(Calendar.DAY_OF_MONTH) == day) {
                            upcoming.add(name)
                        }
                    }
                } catch (e: Exception) {
                    // Handle parsing exceptions
                }
            }
        }

        return upcoming
    }

    private fun showNotification(context: Context, names: List<String>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentText = if (names.size == 1) {
            "It's ${names[0]}'s birthday today!"
        } else {
            "Birthdays today: ${names.joinToString()}"
        }

        val notification = NotificationCompat.Builder(context, "birthday_channel")
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("Birthday Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}