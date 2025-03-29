package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import kotlinx.coroutines.flow.Flow

class GetDailiesUseCase(private val dailiesDao : DailiesDao) {
    operator fun invoke() : Flow<List<Daily>> {
        return dailiesDao.getDailies()
    }
}