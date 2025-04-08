package com.example.daily

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DailiesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Créer les canaux de notification
        createNotificationChannels()

        // Schedule background notifications
        scheduleNotificationWorker(applicationContext)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal pour les notifications quotidiennes
            val dailyChannelId = "daily_notifications"
            val dailyChannelName = "Daily Notifications"
            val dailyChannelDescription = "Notifications pour vos tâches quotidiennes"
            val dailyImportance = NotificationManager.IMPORTANCE_DEFAULT

            val dailyChannel = NotificationChannel(dailyChannelId, dailyChannelName, dailyImportance).apply {
                description = dailyChannelDescription
            }

            // Canal pour les notifications de routines
            val routinesChannelId = "routines_channel"
            val routinesChannelName = "Routines Notifications"
            val routinesChannelDescription = "Notifications déclenchées par vos routines"
            val routinesImportance = NotificationManager.IMPORTANCE_DEFAULT

            val routinesChannel = NotificationChannel(routinesChannelId, routinesChannelName, routinesImportance).apply {
                description = routinesChannelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(routinesChannel)
        }
    }
}