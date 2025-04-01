package com.example.daily.utils

import com.example.daily.presentations.HighPriority
import com.example.daily.presentations.StandardPriority
import com.example.daily.presentations.list.DailyVM

private val dailiesList: MutableList<DailyVM> = mutableListOf(
    DailyVM(
        id = 1,
        title = "Revisez vos cours",
        description = "Rappel pour réviser les cours",
        done = false,
        priority = StandardPriority,
        date = "21-02-2025",
        time = "20:38",
        latitude = 48.8566,
        longitude = 2.3522,
        locationName = "Paris",
        recurringType = "daily",
        recurringDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
        isRecurring = true,
        notificationTime = "30"
    ),
    DailyVM(
        id = 2,
        title = "Faire mes courses",
        description = "Rappel pour faire les courses",
        done = true,
        priority = HighPriority,
        date = "18-02-2025",
        time = "12:00",
        latitude = 48.8566,
        longitude = 2.3522,
        locationName = "Paris",
        recurringType = "daily",
        recurringDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
        isRecurring = true,
        notificationTime = "30"
    ),
    DailyVM(
        id = 3,
        title = "Notifications",
        description = "En tant que abonné, je veux recevoir des notifications",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00",
        latitude = 48.8566,
        longitude = 2.3522,
        locationName = "Paris",
        recurringType = "daily",
        recurringDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
        isRecurring = true,
        notificationTime = "30"
    ),
    DailyVM(
        id = 4,
        title = "Recherche d’articles",
        description = "En tant que utilisateur, je veux voir des articles",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00",
        latitude = 48.8566,
        longitude = 2.3522,
        locationName = "Paris",
        recurringType = "daily",
        recurringDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
        isRecurring = true,
        notificationTime = "30"
    )
)
class DailyException(message: String, e: Exception) : Throwable(message)

