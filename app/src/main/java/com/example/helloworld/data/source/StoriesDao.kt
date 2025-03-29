package com.example.helloworld.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

import com.example.helloworld.domain.model.Story
import kotlinx.coroutines.flow.Flow


@Dao
interface StoriesDao {

    @Query("SELECT * FROM stories")
    fun getStories() : Flow<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :id")
    fun getStoryById(id: Int) : Flow<Story>

    @Query("SELECT * FROM stories WHERE ID = :id")
    fun getStory(id: Int) : Story?

    @Upsert
    suspend fun upsertStory(story: Story)
    @Delete
    suspend fun deleteStory(story: Story) : Int
}