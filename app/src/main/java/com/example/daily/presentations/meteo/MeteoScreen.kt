package com.example.daily.presentations.meteo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

@Composable
fun MeteoScreen(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: MeteoViewModel = viewModel()
) {
    val weatherData by viewModel.weatherData.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasLocationPermission) {
            Text("Permission de localisation requise pour afficher la météo")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Demander la permission")
            }
        } else {
            if (weatherData == null && error == null && !isLoading) {
                Button(onClick = { viewModel.getWeatherFromCurrentLocation() }) {
                    Text("Obtenir la météo actuelle")
                }
            }

            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            weatherData?.let { data ->
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${data.main.temp.roundToInt()}°C",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data.weather.firstOrNull()?.description ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min")
                        Text("${data.main.temp_min}°C")
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Max")
                        Text("${data.main.temp_max}°C")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Humidité")
                        Text("${data.main.humidity}%")
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pression")
                        Text("${data.main.pressure} hPa")
                    }
                }
            }
        }
    }
}