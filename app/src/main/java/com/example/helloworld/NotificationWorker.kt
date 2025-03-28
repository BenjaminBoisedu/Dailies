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
                StoriesDatabase.MIGRATION_3_5,
            )
            .build()
    }

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")
        val today = LocalDate.now()
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

        try {
            // Get stories directly from the database
            val stories = db.dao.getStories().first()
            Log.d("NotificationWorker", "Retrieved ${stories.size} stories from database")


            // Filter for upcoming stories
            val upcomingStories = stories.filter { story ->
                try {
                    val storyDate = LocalDate.parse(story.date, dateFormat)
                    val isUpcoming = isWithinNextDays(today, storyDate, 3)
                    Log.d("NotificationWorker", "Story ${story.title} date=${story.date} isUpcoming=$isUpcoming")
                    isUpcoming
                } catch (e: Exception) {
                    Log.e("NotificationWorker", "Error parsing date: ${story.date}", e)
                    false
                }
            }

            Log.d("NotificationWorker", "Found ${upcomingStories.size} upcoming stories")


            if (upcomingStories.isNotEmpty()) {
                sendNotifications(upcomingStories)
            } else {
                Log.d("NotificationWorker", "No upcoming stories to notify about")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in worker", e)
            return Result.failure()
        }
    }

    private fun isWithinNextDays(today: LocalDate, targetDate: LocalDate, days: Int): Boolean {
        val futureDate = today.plusDays(days.toLong())
        return !targetDate.isBefore(today) && !targetDate.isAfter(futureDate)
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

            val contentText = "📝 Rappel : ${story.title} - ${dateFormatted} à ${timeFormatted}"

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