package com.example.daily.domain.useCases

import com.example.daily.FakeDatabase
import com.example.daily.domain.model.Daily
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetDailyUseCaseTest {
    private lateinit var getDailyUseCase: GetDailyUseCase
    private lateinit var fakeDailiesDao: FakeDatabase
    private lateinit var daily: Daily


    @Before
    fun setUp() {
    }

    @Test
    fun invoke() {
    }

}