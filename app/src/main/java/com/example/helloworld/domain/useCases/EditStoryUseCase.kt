package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.StoriesDao
import com.example.helloworld.domain.model.Story
import com.example.helloworld.utils.StoryException
import kotlinx.coroutines.flow.Flow

class EditStoryUseCase(private val storiesDao: StoriesDao) {
    // Récupère la story à éditer
    fun getStory(id: Int): Flow<Story> {
        return storiesDao.getStoryById(id)
    }

    // Met à jour la story
    @Throws(StoryException::class)
    suspend fun updateStory(story: Story) {
        if (story.title.isEmpty() || story.description.isEmpty() || story.date.isEmpty() || story.time.isEmpty()) {
            throw StoryException("Story data is invalid")
        }
        storiesDao.upsertStory(story)
    }
}