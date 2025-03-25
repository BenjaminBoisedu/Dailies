package com.example.helloworld.di

import android.app.Application
import androidx.room.Room
import com.example.helloworld.data.source.StoriesDatabase
import com.example.helloworld.domain.useCases.DeleteStoryUseCase
import com.example.helloworld.domain.useCases.EditStoryUseCase
import com.example.helloworld.domain.useCases.GetStoriesUseCase
import com.example.helloworld.domain.useCases.GetStoryUseCase
import com.example.helloworld.domain.useCases.StoriesUseCases
import com.example.helloworld.domain.useCases.UpsertStoryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideStoriesDatabase(app: Application): StoriesDatabase {
        return Room.databaseBuilder(
            app,
            StoriesDatabase::class.java,
            StoriesDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideStoriesUseCases(db: StoriesDatabase): StoriesUseCases {
        return StoriesUseCases(
            getStories = GetStoriesUseCase(db.dao),
            getStory = GetStoryUseCase(db.dao),
            upsertStory = UpsertStoryUseCase(db.dao),
            editStory = EditStoryUseCase(db.dao),
            deleteStory = DeleteStoryUseCase(db.dao)
        )
    }
}