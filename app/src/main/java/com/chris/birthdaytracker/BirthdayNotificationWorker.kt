package com.chris.birthdaytracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BirthdayNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("BirthdayNotificationWorker", "Worker started")
        val contacts = ContactsRepository(applicationContext).getAllContacts()
        BirthdayNotificationScheduler.scheduleBirthdayNotifications(applicationContext, contacts)
        Log.d("BirthdayNotificationWorker", "Worker finished")
        return Result.success()
    }
}
