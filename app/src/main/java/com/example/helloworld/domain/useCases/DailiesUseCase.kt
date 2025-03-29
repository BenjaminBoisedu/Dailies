package com.example.helloworld.domain.useCases

data class DailiesUseCases (
    val getDailies : GetDailiesUseCase,
    val getDaily : GetDailyUseCase,
    val upsertDaily : UpsertDailyUseCase,
    val editDaily: EditDailyUseCase,
    val deleteDaily : DeleteDailyUseCase
)