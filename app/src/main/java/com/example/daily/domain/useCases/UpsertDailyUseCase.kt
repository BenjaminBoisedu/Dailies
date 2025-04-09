package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpsertDailyUseCase(private val dailiesDao: DailiesDao) {
    @Throws(DailyException::class)
    suspend operator fun invoke(daily: Daily) {
        if (daily.title.isEmpty() || daily.description.isEmpty())
            throw DailyException("Daily data is invalid", IllegalArgumentException())

        // Si la routine est marquée comme complétée mais n'a pas de date de complétion
        val updatedDaily = if (daily.done && daily.DateDone == null) {
            // Format dd-MM-yyyy pour la date de complétion
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            daily.copy(DateDone = currentDate)
        } else {
            daily
        }

        dailiesDao.upsertDaily(updatedDaily)
    }

    /**
     * Permet de corriger les routines existantes dont la date de complétion est manquante
     */
    suspend fun fixExistingCompletedDailies() {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        dailiesDao.getDailies() // Récupérer toutes les routines
            .collect { dailies ->
                dailies.forEach { daily ->
                    // Vérifier si la routine est complétée et n'a pas de date de complétion
                    if (daily.done && daily.DateDone == null) {
                        val updatedDaily = daily.copy(DateDone = currentDate)
                        dailiesDao.upsertDaily(updatedDaily)
                    }
                }
            }
    }
}