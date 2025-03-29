package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.DailiesDao
import com.example.helloworld.domain.model.Daily
import kotlinx.coroutines.flow.Flow

class GetDailyUseCase(private val dailiesDao : DailiesDao) {
    operator fun invoke() : Flow<List<Daily>> {
        return dailiesDao.getDailies()
    }
}