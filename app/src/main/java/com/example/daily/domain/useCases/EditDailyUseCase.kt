package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditDailyUseCase(private val dailiesDao: DailiesDao) {
    fun getDaily(id: Int): Flow<Daily> {
        return dailiesDao.getDailyById(id)
    }

    @Throws(DailyException::class)
    suspend fun updateDaily(daily: Daily) {
        if (daily.title.isEmpty() || daily.description.isEmpty() || daily.date.isEmpty() || daily.time.isEmpty()) {
            throw DailyException("Daily data is invalid", IllegalArgumentException())
        }

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
}