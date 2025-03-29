package com.example.helloworld.di

import android.app.Application
import androidx.room.Room
import com.example.helloworld.data.source.DailiesDatabase
import com.example.helloworld.domain.useCases.DeleteDailyUseCase
import com.example.helloworld.domain.useCases.EditDailyUseCase
import com.example.helloworld.domain.useCases.GetDailiesUseCase
import com.example.helloworld.domain.useCases.GetDailyUseCase
import com.example.helloworld.domain.useCases.DailiesUseCases
import com.example.helloworld.domain.useCases.UpsertDailyUseCase
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
    fun provideDailiesDatabase(app: Application): DailiesDatabase {
        return Room.databaseBuilder(
            app,
            DailiesDatabase::class.java,
            DailiesDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDailiesUseCases(db: DailiesDatabase): DailiesUseCases {
        return DailiesUseCases(
            getDailies = GetDailiesUseCase(db.dao),
            getDaily = GetDailyUseCase(db.dao),
            upsertDaily = UpsertDailyUseCase(db.dao),
            editDaily = EditDailyUseCase(db.dao),
            deleteDaily = DeleteDailyUseCase(db.dao)
        )
    }
}