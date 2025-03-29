package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException

class UpsertDailyUseCase(private val dailiesDao : DailiesDao) {
    @Throws(DailyException::class)
    suspend operator fun invoke(daily: Daily) {
        if (daily.title.isEmpty() || daily.description.isEmpty())
            throw DailyException("Daily data is invalid", IllegalArgumentException())
        dailiesDao.upsertDaily(daily)
    }
}