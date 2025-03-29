package com.example.helloworld.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

import com.example.helloworld.domain.model.Daily
import kotlinx.coroutines.flow.Flow


@Dao
interface DailiesDao {

    @Query("SELECT * FROM dailies")
    fun getDailies() : Flow<List<Daily>>

    @Query("SELECT * FROM dailies WHERE id = :id")
    fun getDailyById(id: Int) : Flow<Daily>

    @Query("SELECT * FROM dailies WHERE ID = :id")
    fun getDaily(id: Int) : Daily?

    @Upsert
    suspend fun upsertDaily(daily: Daily)

    @Delete
    suspend fun deleteDaily(daily: Daily) : Int

}