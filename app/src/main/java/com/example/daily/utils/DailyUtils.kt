package com.example.daily.utils

import com.example.daily.presentations.HighPriority
import com.example.daily.presentations.StandardPriority
import com.example.daily.presentations.list.DailyVM
import kotlinx.coroutines.flow.flow

private val dailiesList: MutableList<DailyVM> = mutableListOf(
    DailyVM(
        id = 1,
        title = "Revisez vos cours",
        description = "Rappel pour réviser les cours",
        done = false,
        priority = StandardPriority,
        date = "21-02-2025",
        time = "20:38"
    ),
    DailyVM(
        id = 2,
        title = "Faire mes courses",
        description = "Rappel pour faire les courses",
        done = true,
        priority = HighPriority,
        date = "18-02-2025",
        time = "12:00"
    ),
    DailyVM(
        id = 3,
        title = "Notifications",
        description = "En tant que abonné, je veux recevoir des notifications",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00"
    ),
    DailyVM(
        id = 4,
        title = "Recherche d’articles",
        description = "En tant que utilisateur, je veux voir des articles",
        done = true,
        priority = HighPriority,
        date = "13-02-2025",
        time = "12:00"
    )
)
class DailyException(message: String, e: Exception) : Throwable(message)

fun getDailies() = flow {
    emit(dailiesList)
}

fun addOrUpdateDaily(daily: DailyVM) {
    if (daily.title.isBlank() || daily.description?.isBlank() != false || daily.date.isBlank() || daily.time.isBlank() || daily.priority == null) {
        throw DailyException("Unable to save daily : Title cannot be empty", Exception("Title cannot be empty"))
    }

    val existingDaily = dailiesList.find { it.id == daily.id }
    existingDaily?.let {
        dailiesList.remove(it)
    }
    dailiesList.add(daily)
}

fun findDaily(dailyId: Int) : DailyVM? {
    return dailiesList.find { it .id == dailyId }
}
