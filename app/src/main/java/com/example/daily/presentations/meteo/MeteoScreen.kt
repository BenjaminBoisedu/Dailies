package com.example.daily.presentations.meteo

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MeteoScreen(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    meteoviewmodel:MeteoViewModel = hiltViewModel(),
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            meteoviewmodel.getWeatherFromCurrentLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permission de localisation refusée")
            }
        }
    }
    val weatherData by meteoviewmodel.weatherData.observeAsState()
    val isLoading by meteoviewmodel.isLoading.observeAsState(false)
    val error by meteoviewmodel.error.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (weatherData == null && error == null && !isLoading) {
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                navController.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasLocationPermission) {
                meteoviewmodel.getWeatherFromCurrentLocation()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7684A7)
                        )
                    ) {
                        Text("Voir la météo", color = Color.White)
                    }
                }
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