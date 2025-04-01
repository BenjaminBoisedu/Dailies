package com.example.daily

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.daily.data.source.DailiesDatabase
import com.example.daily.domain.model.Daily
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.edit
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
                DailiesDatabase.MIGRATION_5_6,
                DailiesDatabase.MIGRATION_6_7
            )
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "Worker started")

        // The key issue - setting foreground service with proper type
        setForeground(createForegroundInfo())

        try {
            // Get dailies directly from the database
            val dailies = db.dao.getDailies().first()
            Log.d("NotificationWorker", "Retrieved ${dailies.size} dailies from database")

            // Filter for dailies that should trigger notifications now
            val dailiesToNotify = dailies.filter { daily ->
                try {
                    // Parse the daily date and time
                    val dailyDate = LocalDate.parse(daily.date, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()))
                    val currentDate = LocalDate.now()



                    if (daily.isRecurring && daily.recurringDays?.isNotEmpty() == true) {

                        // Obtenir le nom du jour actuel en français
                        val currentDayName = currentDate.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.FULL, Locale.FRENCH
                        ).lowercase()



                        // Convertir le jour actuel en abréviation
                        val currentDayAbbr = when(currentDayName) {
                            "lundi" -> "Lu"
                            "mardi" -> "Ma"
                            "mercredi" -> "Me"
                            "jeudi" -> "Je"
                            "vendredi" -> "Ve"
                            "samedi" -> "Sa"
                            "dimanche" -> "Di"
                            else -> currentDayName
                        }

                        val cleanedDays = daily.recurringDays.toString().removeSurrounding("[", "]")
                        val recurringDaysList = cleanedDays.split(",").map { it.trim() }

                        Log.d("NotificationWorker", "=== Comparaison détaillée des jours ===")
                        Log.d("NotificationWorker", "Analyse de daily: ${daily.title}")
                        Log.d("NotificationWorker", "Jour actuel: $currentDayName ($currentDayAbbr)")
                        Log.d("NotificationWorker", "Jours récurrents (liste nettoyée): $recurringDaysList")


                        val matches = recurringDaysList.any { day ->
                            Log.d("NotificationWorker", "Comparaison: '$day' vs '$currentDayAbbr'")
                            day == currentDayAbbr
                        }

                        Log.d("NotificationWorker", "Résultat matches: $matches pour ${daily.title}")
                        Log.d("NotificationWorker", "=== Fin comparaison détaillée ===")

                        // Pour les daily récurrentes, vérifier si le jour actuel correspond à l'un des jours récurrents
                        if (!matches) {
                            return@filter false
                        }
                    } else {
                        // Pour les daily non récurrentes, vérifier si la date correspond à aujourd'hui
                        if (dailyDate != currentDate) {
                            return@filter false
                        }
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

                    Log.d("NotificationWorker", "Daily ${daily.title}: heure cible=${hourMinute}, " +
                            "heure notif=${notificationMinuteOfDay}, " +
                            "heure actuelle=${currentMinuteOfDay}, " +
                            "fenêtre=${notificationMinuteOfDay} à ${notificationMinuteOfDay + 5}")


                    // Check if we're within a 5-minute window of the notification time
                    val shouldNotify = currentMinuteOfDay >= notificationMinuteOfDay &&
                            currentMinuteOfDay <= notificationMinuteOfDay + 15

                    // Check if we've already sent this notification today
                    val hasBeenNotified = hasNotificationBeenSent(daily.id ?: 0, daily.date)

                    val willNotify = true && !hasBeenNotified

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

            sendMissedEventNotifications(dailies)

            Log.d("NotificationWorker", "Worker completed successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Worker failed", e)
            return Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "worker_notification_channel"

        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            channelId,
            "Worker Notifications",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Vérification des rappels")
            .setSmallIcon(R.drawable.logo_appli)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // For Android 14 (API 34) and higher, specify foreground service type
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 9999
    }

    // Helper method to check if a notification has already been sent today
    private fun hasNotificationBeenSent(dailyId: Int, date: String): Boolean {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${dailyId}_${date}"
        val result = prefs.getBoolean(prefKey, false)
        Log.d("NotificationWorker", "Vérification notification envoyée: id=$dailyId, date=$date, résultat=$result")
        return result
    }

    // Helper method to mark a notification as sent
    @SuppressLint("UseKtx")
    private fun markNotificationAsSent(dailyId: Int, date: String) {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val prefKey = "notification_sent_${dailyId}_${date}"
        prefs.edit { putBoolean(prefKey, true) }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendNotifications(dailies: List<Daily>) {
        Log.d("NotificationWorker", "Sending notifications for ${dailies.size} dailies")
        Log.d("NotificationWorker", "Permission vérifiée: ${
            ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }")
            if (true &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
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

            // Create an intent to open the app when notification is clicked
            val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                daily.id ?: 0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("$emoji Rappel")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(daily.id ?: 0, notification)
                Log.d("NotificationWorker", "Notification sent for: ${daily.title}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendMissedEventNotifications(dailies: List<Daily>) {
        Log.d("NotificationWorker", "Vérification des événements manqués: ${dailies.size} dailies")

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NotificationWorker", "Permission de notification non accordée")
            return
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "missed_events_notifications"

        // Créer le canal de notification
        val channel = NotificationChannel(
            channelId,
            "Notifications d'événements manqués",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentMinuteOfDay = currentHour * 60 + currentMinute

        // Filtrer les événements déjà passés aujourd'hui et non marqués comme done
        val missedEvents = dailies.filter { daily ->
            try {
                // Vérifier si l'événement est pour aujourd'hui
                val dailyDate = LocalDate.parse(daily.date, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()))
                val currentDate = LocalDate.now()

                // Vérifier si nous sommes le bon jour
                val isToday = if (daily.isRecurring && daily.recurringDays?.isNotEmpty() == true) {
                    val currentDayName = currentDate.dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.FULL, Locale.FRENCH
                    ).lowercase()

                    val currentDayAbbr = when(currentDayName) {
                        "lundi" -> "Lu"
                        "mardi" -> "Ma"
                        "mercredi" -> "Me"
                        "jeudi" -> "Je"
                        "vendredi" -> "Ve"
                        "samedi" -> "Sa"
                        "dimanche" -> "Di"
                        else -> currentDayName
                    }

                    val cleanedDays = daily.recurringDays.toString().removeSurrounding("[", "]")
                    val recurringDaysList = cleanedDays.split(",").map { it.trim() }

                    recurringDaysList.any { it == currentDayAbbr }
                } else {
                    dailyDate == currentDate
                }

                if (!isToday || daily.done) {
                    return@filter false
                }

                // Calculer l'heure de l'événement en minutes
                val dailyTime = daily.time.split(":")
                val hourMinute = dailyTime[0].toInt() * 60 + dailyTime[1].toInt()

                // Vérifier si l'événement est déjà passé
                val isPassed = currentMinuteOfDay > hourMinute + 15 // On ajoute 15 minutes de marge

                // Vérifier si on a déjà envoyé une notification pour cet événement manqué
                val notificationKey = "missed_event_${daily.id}_${daily.date}"
                val hasSentMissedNotification = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                    .getBoolean(notificationKey, false)

                Log.d("NotificationWorker", "Événement manqué: ${daily.title}, passé: $isPassed, notif déjà envoyée: $hasSentMissedNotification")

                isPassed && !hasSentMissedNotification
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Erreur lors de la vérification d'événement manqué: ${daily.title}", e)
                false
            }
        }

        Log.d("NotificationWorker", "Événements manqués trouvés: ${missedEvents.size}")

        missedEvents.forEach { daily ->
            val dateFormatted = daily.date.split("-").joinToString("/")
            val timeFormatted = daily.time.split(":").take(2).joinToString("h")

            val contentText = "⏰ Événement manqué: ${daily.title} était prévu le $dateFormatted à $timeFormatted"

            // Créer une intention pour ouvrir l'application au clic sur la notification
            val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                (daily.id ?: 0) + 10000, // Différencier des IDs normaux
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("⏰ Événement manqué")
                .setContentText(contentText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify((daily.id ?: 0) + 10000, notification)
                Log.d("NotificationWorker", "Notification d'événement manqué envoyée pour: ${daily.title}")

                // Marquer cette notification comme envoyée
                context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).edit {
                    putBoolean("missed_event_${daily.id}_${daily.date}", true)
                }
            }
        }
    }
}