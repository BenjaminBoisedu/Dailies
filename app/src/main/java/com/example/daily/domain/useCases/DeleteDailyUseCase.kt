package com.example.daily.domain.useCases

import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import com.example.daily.utils.DailyException

class DeleteDailyUseCase(private val dailiesDao: DailiesDao) {
    @Throws(DailyException::class)
    suspend operator fun invoke(daily: Daily?): Boolean {
        return try {
            if (daily == null) {
                throw DailyException("Cannot delete a null Daily object", NullPointerException())
            }

            val id = daily.id ?: throw DailyException("Daily object has null ID", NullPointerException())

            val rowsDeleted = dailiesDao.deleteDailyById(id)
            rowsDeleted > 0
        } catch (e: Exception) {
            throw DailyException(
                "Unable to delete daily: ${e.message ?: "Unknown error"}",
                e
            )
        }
    }
}