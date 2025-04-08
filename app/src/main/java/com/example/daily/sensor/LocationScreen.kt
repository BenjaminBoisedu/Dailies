package com.example.daily.sensor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    hasLocationPermission: Boolean = true,
    onRequestPermission: () -> Unit = {}
) {
    val location by viewModel.currentLocation.collectAsState()

    // On démarre les mises à jour de localisation seulement si les permissions sont accordées
    if (hasLocationPermission) {
        DisposableEffect(key1 = true) {
            viewModel.startLocationUpdates()
            onDispose {
                viewModel.stopLocationUpdates()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Informations de localisation",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!hasLocationPermission) {
            // Si les permissions de localisation ne sont pas accordées, afficher un message et un bouton
            Text(
                text = "L'accès à la localisation est requis pour afficher votre position.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(onClick = onRequestPermission) {
                Text("Demander la permission")
            }
        } else if (location != null) {
            // Si on a les permissions et une localisation
            val loc = location!!

            Text(
                text = "Latitude: ${loc.latitude}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Longitude: ${loc.longitude}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (loc.hasAltitude()) {
                Text(
                    text = "Altitude: ${loc.altitude} m",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (loc.hasSpeed()) {
                Text(
                    text = "Vitesse: ${loc.speed} m/s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (loc.hasAccuracy()) {
                Text(
                    text = "Précision: ${loc.accuracy} m",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Si on a les permissions mais pas encore de localisation
            Text(
                text = "En attente de localisation...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}