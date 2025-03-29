package com.example.helloworld

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleNotificationWorker(applicationContext: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .build()

    val repeatingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
        1, TimeUnit.HOURS,  // Check every hour
        15, TimeUnit.MINUTES // Flex interval
    )
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "daily_notifications",
        ExistingPeriodicWorkPolicy.UPDATE,
        repeatingRequest
    )

    Log.d("MainActivity", "Notification worker scheduled")
}

fun runNotificationWorkerNow(context: Context) {
    val oneTimeRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .build()

    WorkManager.getInstance(context).enqueue(oneTimeRequest)
    Log.d("NotificationScheduler", "Immediate notification worker scheduled")
}