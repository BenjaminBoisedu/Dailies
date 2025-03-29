package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException
import kotlinx.coroutines.flow.Flow

class EditDailyUseCase(private val dailiesDao: DailiesDao) {
    fun getDaily(id: Int): Flow<Daily> {
        return dailiesDao.getDailyById(id)
    }

    @Throws(DailyException::class)
    suspend fun updateDaily(daily: Daily) {
        if (daily.title.isEmpty() || daily.description.isEmpty() || daily.date.isEmpty() || daily.time.isEmpty()) {
            throw DailyException("Daily data is invalid", IllegalArgumentException())
        }
        dailiesDao.upsertDaily(daily)
    }
}