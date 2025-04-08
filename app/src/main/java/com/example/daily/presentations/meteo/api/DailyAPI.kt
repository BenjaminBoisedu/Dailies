package com.example.daily.presentations.meteo.api

import com.example.daily.presentations.meteo.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DailyAPI {
    @GET("weather")
    suspend fun getWeatherByLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("lang") Lang: String = "fr",
        @Query("appid") apiKey: String
    ): Response<WeatherResponse>
}