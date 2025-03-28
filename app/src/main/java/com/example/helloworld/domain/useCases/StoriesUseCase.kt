package com.example.helloworld.domain.useCases

data class StoriesUseCases (
    val getStories : GetStoriesUseCase,
    val getStory : GetStoryUseCase,
    val upsertStory : UpsertStoryUseCase,
    val editStory: EditStoryUseCase,
    val deleteStory : DeleteStoryUseCase
)