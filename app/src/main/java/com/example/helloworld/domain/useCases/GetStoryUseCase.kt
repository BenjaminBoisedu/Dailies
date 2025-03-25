package com.example.helloworld.domain.useCases

import com.example.helloworld.data.source.StoriesDao
import com.example.helloworld.domain.model.Story
import kotlinx.coroutines.flow.Flow

class GetStoryUseCase(private val storiesDao : StoriesDao) {
    operator fun invoke() : Flow<List<Story>> {
        return storiesDao.getStories()
    }
}