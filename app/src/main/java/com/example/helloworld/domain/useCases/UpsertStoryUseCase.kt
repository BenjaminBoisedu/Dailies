package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.StoriesDao
import com.example.helloworld.domain.model.Story
import com.example.helloworld.utils.StoryException

class UpsertStoryUseCase(private val storiesDao : StoriesDao) {
    @Throws(StoryException::class)
    suspend operator fun invoke(story: Story) {
        if (story.title.isEmpty() || story.description.isEmpty())
            throw StoryException("Story data is invalid")
        storiesDao.upsertStory(story)
    }
}