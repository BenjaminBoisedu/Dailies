package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.DailiesDao
import com.example.helloworld.domain.model.Daily
import com.example.helloworld.utils.DailyException

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