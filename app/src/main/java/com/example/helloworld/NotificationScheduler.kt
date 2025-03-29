package com.example.helloworld

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

fun scheduleNotificationWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        // Remove battery constraints completely
        .build()

    // Run the worker more frequently to catch all notifications
    val repeatingRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
        15, TimeUnit.MINUTES,  // Minimum interval
        5, TimeUnit.MINUTES    // Flex interval
    ).setConstraints(constraints)
        .addTag("notification_work")
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "notification_work",
        ExistingPeriodicWorkPolicy.UPDATE,
        repeatingRequest
    )
    Log.d("NotificationScheduler", "Notification worker scheduled with constraints")

    // Also schedule an immediate run to test
    runNotificationWorkerNow(context)
}

fun runNotificationWorkerNow(context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
    Log.d("NotificationScheduler", "Immediate notification worker started")
}