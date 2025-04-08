package com.example.daily.presentations.meteo.repository
import com.example.daily.presentations.meteo.api.DailyAPI
import com.example.daily.presentations.meteo.model.WeatherResponse
import retrofit2.Response

class DailyRepository(private val api: DailyAPI) {
    suspend fun getWeatherByLocation(
        latitude: Double,
        longitude: Double,
        apiKey: String
    ): Response<WeatherResponse> {
        return api.getWeatherByLocation(latitude, longitude, apiKey = apiKey)
    }
}