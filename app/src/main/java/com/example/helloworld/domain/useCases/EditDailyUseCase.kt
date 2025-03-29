package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.DailiesDao
import com.example.helloworld.domain.model.Daily
import com.example.helloworld.utils.DailyException
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