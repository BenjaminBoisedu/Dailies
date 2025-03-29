package com.example.helloworld

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.helloworld.data.source.DailiesDatabase
import com.example.helloworld.domain.model.Daily
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            DailiesDatabase::class.java,
            DailiesDatabase.DATABASE_NAME
        )
            .addMigrations(
                DailiesDatabase.MIGRATION_1_2,
                DailiesDatabase.MIGRATION_2_3,
                DailiesDatabase.MIGRATION_3_4,
                DailiesDatabase.MIGRATION_4_5,
                DailiesDatabase.MIGRATION_3_5,
                DailiesDatabase.MIGRATION_5_6
            )
            .build()
    }

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")
        val currentTimeMillis = System.currentTimeMillis()

        try {
            // Get dailies directly from the database
            val dailies = db.dao.getDailies().first()
            Log.d("NotificationWorker", "Retrieved ${dailies.size} dailies from database")

            // Filter for dailies that should trigger notifications now
            val dailiesToNotify = dailies.filter { daily ->
                try {
                    // Parse the daily date and time
                    val dailyDateTime = getDateTimeFromDaily(daily)
                    val dailyDate = LocalDate.parse(daily.date, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()))
                    val currentDate = LocalDate.now()

                    // Check if the daily date is today
                    if (dailyDate != currentDate) {
                        return@filter false
                    }

                    // Check if the daily is already done
                    if (daily.done) {
                        return@filter false
                    }

                    val notificationTimeMinutes = daily.notificationTime.toIntOrNull() ?: 30
                    val dailyTime = daily.time.split(":")
                    val hourMinute = dailyTime[0].toInt() * 60 + dailyTime[1].toInt()

                    // Calculate the exact notification time in minutes of the day
                    val notificationMinuteOfDay = hourMinute - notificationTimeMinutes

                    // Get current hour and minute
                    val calendar = java.util.Calendar.getInstance()
                    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(java.util.Calendar.MINUTE)
                    val currentMinuteOfDay = currentHour * 60 + currentMinute

                    // Check if we're within a 5-minute window of the notification time
                    val shouldNotify = currentMinuteOfDay >= notificationMinuteOfDay &&
                            currentMinuteOfDay <= notificationMinuteOfDay + 5

                    // Check if we've already sent this notification today
                    val hasBeenNotified = hasNotificationBeenSent(daily.id ?: 0, daily.date)

                    val willNotify = shouldNotify && !hasBeenNotified

                    // Log for debugging
                    if (shouldNotify) {
                        Log.d("NotificationWorker", "Daily ${daily.title} - Should notify: $shouldNotify, Already notified: $hasBeenNotified")
                    }

                    willNotify
                } catch (e: Exception) {
                    Log.e("NotificationWorker", "Error processing daily: ${daily.title}", e)
                    false
                }
            }

            Log.d("NotificationWorker", "Found ${dailiesToNotify.size} dailies to notify about")

            if (dailiesToNotify.isNotEmpty()) {
                sendNotifications(dailiesToNotify)

                // Mark these notifications as sent
                dailiesToNotify.forEach { daily ->
                    markNotificationAsSent(daily.id ?: 0, daily.date)
                }
            } else {
                Log.d("NotificationWorker", "No dailies to notify about right now")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in worker", e)
            return Result.failure()
        }
    }

    // Helper method to check if a notification has already been sent today
    private fun hasNotificationBeenSent(dailyId: Int, date: String): Boolean {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${dailyId}_${date}"
        return prefs.getBoolean(prefKey, false)
    }

    // Helper method to mark a notification as sent
    private fun markNotificationAsSent(dailyId: Int, date: String) {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${dailyId}_${date}"
        prefs.edit().putBoolean(prefKey, true).apply()
    }

    // Helper method to convert daily date and time strings to timestamp
    private fun getDateTimeFromDaily(daily: Daily): Long {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val localDateTime = java.time.LocalDateTime.parse("${daily.date} ${daily.time}", formatter)
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun sendNotifications(dailies: List<Daily>) {
        Log.d("NotificationWorker", "Sending notifications for ${dailies.size} dailies")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationWorker", "Notification permission not granted")
            return
        }
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "daily_notifications"

        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            channelId,
            "Daily Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        dailies.forEach { daily ->
            val emoji = when {
                daily.done -> "✅"
                daily.priority == 3 -> "🔴" // High priority
                daily.priority == 2 -> "🟡" // Medium priority
                daily.priority == 1 -> "🟢" // Low priority
                else -> "📝"
            }

            val dateFormatted = daily.date.split("-").joinToString("/")
            val timeFormatted = daily.time.split(":").take(2).joinToString("h")

            // Personnaliser le texte en fonction du délai de notification
            val timeDesc = when(daily.notificationTime) {
                "15" -> "15 minutes"
                "30" -> "30 minutes"
                "60" -> "1 heure"
                "120" -> "2 heures"
                "1440" -> "1 jour"
                else -> daily.notificationTime
            }

            val contentText = "📝 Rappel: ${daily.title} aura lieu dans $timeDesc - $dateFormatted à $timeFormatted"

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("$emoji Rappel")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(daily.id ?: 0, notification)
                Log.d("NotificationWorker", "Notification sent for: ${daily.title}")
            }
        }
    }
}