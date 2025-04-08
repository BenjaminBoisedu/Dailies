package com.example.daily.presentations.meteo

import android.Manifest
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.daily.presentations.meteo.api.DailyAPI
import com.example.daily.presentations.meteo.model.WeatherResponse
import com.example.daily.presentations.meteo.repository.DailyRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MeteoViewModel(application: Application) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val apiKey = "9bbaaac5bc82c76314f0c5bdece8c2ca"
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    private val repository: DailyRepository

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val api = retrofit.create(DailyAPI::class.java)
        repository = DailyRepository(api)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getWeatherFromCurrentLocation() {
        _isLoading.value = true
        _error.value = null

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    fetchWeatherData(it.latitude, it.longitude)
                } ?: run {
                    _error.value = "Impossible d'obtenir la localisation"
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _error.value = "Erreur lors de la récupération de la localisation: ${it.message}"
                _isLoading.value = false
            }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByLocation(latitude, longitude, apiKey)
                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                } else {
                    _error.value = "Erreur: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}