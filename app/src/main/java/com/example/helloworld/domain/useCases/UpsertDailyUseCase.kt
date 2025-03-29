package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.DailiesDao
import com.example.helloworld.domain.model.Daily
import com.example.helloworld.utils.DailyException

class UpsertDailyUseCase(private val dailiesDao : DailiesDao) {
    @Throws(DailyException::class)
    suspend operator fun invoke(daily: Daily) {
        if (daily.title.isEmpty() || daily.description.isEmpty())
            throw DailyException("Daily data is invalid", IllegalArgumentException())
        dailiesDao.upsertDaily(daily)
    }
}