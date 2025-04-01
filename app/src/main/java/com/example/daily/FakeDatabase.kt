package com.example.daily
import com.example.daily.data.source.DailiesDao
import com.example.daily.domain.model.Daily
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeDatabase : DailiesDao {

    var lastDeletedId: Int = -1
    var shouldReturnSuccessForDelete = true
    var shouldThrowExceptionOnDelete = false

    val dailiesList = mutableListOf<Daily>()

    override fun getDailies(): Flow<List<Daily>> = flow {
        emit(dailiesList)
    }

    override fun getDailyById(id: Int): Flow<Daily> {
        val daily = dailiesList.find { it.id == id }
        return flow {
            if (daily != null) {
                emit(daily)
            } else {
                throw Exception("Daily not found")
            }
        }
    }

    override fun getDaily(id: Int): Daily? {
        return dailiesList.find { it.id == id }
    }

    override suspend fun upsertDaily(daily: Daily) {
        val index = dailiesList.indexOfFirst { it.id == daily.id }
        if (index != -1) {
            dailiesList[index] = daily
        } else {
            dailiesList.add(daily)
        }
    }

    override suspend fun deleteDaily(daily: Daily): Int {
        val index = dailiesList.indexOfFirst { it.id == daily.id }
        return if (index != -1) {
            dailiesList.removeAt(index)
            1
        } else {
            0
        }
    }

    override suspend fun deleteDailyById(id: Int): Int {
        if (shouldThrowExceptionOnDelete) {
            throw RuntimeException("Erreur de base de données simulée")
        }

        lastDeletedId = id
        return if (shouldReturnSuccessForDelete) 1 else 0
    }
}