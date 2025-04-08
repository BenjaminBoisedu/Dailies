package com.example.daily.sensor

data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val name: String? = null
)