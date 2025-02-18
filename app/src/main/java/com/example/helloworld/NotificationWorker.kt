package com.example.helloworld

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.helloworld.presentations.list.StoryVM
import com.example.helloworld.utils.getStories
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")
        val today = LocalDate.now()
        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

        getStories().collect { stories ->
            val upcomingStories = stories.filter { story ->
                try {
                    val storyDate = LocalDate.parse(story.date, dateFormat)
                    Log.d("NotificationWorker", "Story date: $storyDate")
                    isWithinNextDays(today, storyDate, 3)
                } catch (e: Exception) {
                    Log.e("NotificationWorker", "Error parsing date: ${story.date}", e)
                    false
                }
            }

            if (upcomingStories.isNotEmpty()) {
                sendNotifications(upcomingStories)
            }
        }

        return Result.success()
    }

    private fun isWithinNextDays(today: LocalDate, targetDate: LocalDate, days: Int): Boolean {
        val futureDate = today.plusDays(days.toLong())
        return targetDate.isAfter(today) && targetDate.isBefore(futureDate)
    }

    private fun sendNotifications(stories: List<StoryVM>) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "story_notifications"

        val channel = NotificationChannel(
            channelId,
            "Story Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)


        stories.forEach { story ->
            val emoji = when {
                story.done -> "✅"
                story.priority?.toString()?.contains("High", ignoreCase = true) == true -> "🔴"
                else -> "📝"
            }
            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("$emoji Rappel")
                .setContentText("Hey, N'oublie pas 📝${story.title} 📝 , c'est pour le ${story.date.split("-")} à ${story.time.split(":").first()}h !🔔🔔🔔")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                    R.drawable.ic_notification,
                    "✓ Mark as done",
                    null
                )
                .build()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(story.id, notification)
                Log.d("NotificationWorker", "Notification sent for: ${story.title}")
            }
        }
    }
}