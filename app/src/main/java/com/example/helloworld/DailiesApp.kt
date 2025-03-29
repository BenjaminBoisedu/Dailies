package com.example.helloworld

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailiesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        createNotificationChannel()

        // Schedule background notifications
        scheduleNotificationWorker(applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "daily_notifications"
            val name = "Daily Notifications"
            val description = "Notifications pour vos tâches quotidiennes"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}