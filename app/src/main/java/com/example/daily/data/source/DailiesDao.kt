package com.example.daily.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

import com.example.daily.domain.model.Daily
import kotlinx.coroutines.flow.Flow


@Dao
interface DailiesDao {

    @Query("SELECT * FROM dailies ORDER BY SUBSTR(date, 7, 4) || SUBSTR(date, 4, 2) || SUBSTR(date, 1, 2) ASC")
    fun getDailies() : Flow<List<Daily>>

    @Query("SELECT * FROM dailies WHERE id = :id")
    fun getDailyById(id: Int) : Flow<Daily>

    @Query("SELECT * FROM dailies WHERE ID = :id")
    fun getDaily(id: Int) : Daily?

    @Upsert
    suspend fun upsertDaily(daily: Daily)

    @Delete
    suspend fun deleteDaily(daily: Daily) : Int

    @Query("DELETE FROM dailies WHERE id = :id")
    suspend fun deleteDailyById(id: Int) : Int
}