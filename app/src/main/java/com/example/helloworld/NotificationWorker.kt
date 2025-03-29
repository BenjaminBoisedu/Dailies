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
import com.example.helloworld.data.source.StoriesDatabase
import com.example.helloworld.domain.model.Story
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
            StoriesDatabase::class.java,
            StoriesDatabase.DATABASE_NAME
        )
            .addMigrations(
                StoriesDatabase.MIGRATION_1_2,
                StoriesDatabase.MIGRATION_2_3,
                StoriesDatabase.MIGRATION_3_4,
                StoriesDatabase.MIGRATION_4_5,
                StoriesDatabase.MIGRATION_3_5,
                StoriesDatabase.MIGRATION_5_6
            )
            .build()
    }

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")
        val currentTimeMillis = System.currentTimeMillis()

        try {
            // Get stories directly from the database
            val stories = db.dao.getStories().first()
            Log.d("NotificationWorker", "Retrieved ${stories.size} stories from database")

            // Filter for stories that should trigger notifications now
            val storiesToNotify = stories.filter { story ->
                try {
                    // Parse the story date and time
                    val storyDateTime = getDateTimeFromStory(story)
                    val storyDate = LocalDate.parse(story.date, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()))
                    val currentDate = LocalDate.now()

                    // Check if the story date is today
                    if (storyDate != currentDate) {
                        return@filter false
                    }

                    // Check if the story is already done
                    if (story.done) {
                        return@filter false
                    }

                    val notificationTimeMinutes = story.notificationTime.toIntOrNull() ?: 30
                    val storyTime = story.time.split(":")
                    val hourMinute = storyTime[0].toInt() * 60 + storyTime[1].toInt()

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
                    val hasBeenNotified = hasNotificationBeenSent(story.id ?: 0, story.date)

                    val willNotify = shouldNotify && !hasBeenNotified

                    // Log for debugging
                    if (shouldNotify) {
                        Log.d("NotificationWorker", "Story ${story.title} - Should notify: $shouldNotify, Already notified: $hasBeenNotified")
                    }

                    willNotify
                } catch (e: Exception) {
                    Log.e("NotificationWorker", "Error processing story: ${story.title}", e)
                    false
                }
            }

            Log.d("NotificationWorker", "Found ${storiesToNotify.size} stories to notify about")

            if (storiesToNotify.isNotEmpty()) {
                sendNotifications(storiesToNotify)

                // Mark these notifications as sent
                storiesToNotify.forEach { story ->
                    markNotificationAsSent(story.id ?: 0, story.date)
                }
            } else {
                Log.d("NotificationWorker", "No stories to notify about right now")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in worker", e)
            return Result.failure()
        }
    }

    // Helper method to check if a notification has already been sent today
    private fun hasNotificationBeenSent(storyId: Int, date: String): Boolean {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${storyId}_${date}"
        return prefs.getBoolean(prefKey, false)
    }

    // Helper method to mark a notification as sent
    private fun markNotificationAsSent(storyId: Int, date: String) {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${storyId}_${date}"
        prefs.edit().putBoolean(prefKey, true).apply()
    }

    // Helper method to convert story date and time strings to timestamp
    private fun getDateTimeFromStory(story: Story): Long {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val localDateTime = java.time.LocalDateTime.parse("${story.date} ${story.time}", formatter)
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun sendNotifications(stories: List<Story>) {
        Log.d("NotificationWorker", "Sending notifications for ${stories.size} stories")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationWorker", "Notification permission not granted")
            return
        }
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "story_notifications"

        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            channelId,
            "Story Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        stories.forEach { story ->
            val emoji = when {
                story.done -> "✅"
                story.priority == 3 -> "🔴" // High priority
                story.priority == 2 -> "🟡" // Medium priority
                story.priority == 1 -> "🟢" // Low priority
                else -> "📝"
            }

            val dateFormatted = story.date.split("-").joinToString("/")
            val timeFormatted = story.time.split(":").take(2).joinToString("h")

            // Personnaliser le texte en fonction du délai de notification
            val timeDesc = when(story.notificationTime) {
                "15" -> "15 minutes"
                "30" -> "30 minutes"
                "60" -> "1 heure"
                "120" -> "2 heures"
                "1440" -> "1 jour"
                else -> story.notificationTime
            }

            val contentText = "📝 Rappel: ${story.title} aura lieu dans $timeDesc - $dateFormatted à $timeFormatted"

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
                notificationManager.notify(story.id ?: 0, notification)
                Log.d("NotificationWorker", "Notification sent for: ${story.title}")
            }
        }
    }
}