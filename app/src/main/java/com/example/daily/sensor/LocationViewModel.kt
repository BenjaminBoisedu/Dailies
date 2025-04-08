package com.example.daily.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val TAG = "LocationViewModel"

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

        // Configurer la requête de localisation avec une haute précision
        locationRequest = LocationRequest.Builder(5000) // 5 secondes
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(2000) // 2 secondes minimum
            .setMaxUpdateDelayMillis(10000) // 10 secondes maximum
            .setMinUpdateDistanceMeters(1f) // 1 mètre de distance minimum
            .build()

        // Callback pour recevoir les mises à jour de localisation
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Nouvelle localisation reçue: ${location.latitude}, ${location.longitude}")
                    _currentLocation.value = location
                }
            }
        }

        // Tenter d'obtenir la dernière localisation connue
        getLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        Log.d(TAG, "Dernière localisation connue: ${it.latitude}, ${it.longitude}")
                        _currentLocation.value = it
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erreur lors de la récupération de la dernière localisation", e)
                }
        } else {
            Log.d(TAG, "Permissions de localisation non accordées")
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (hasLocationPermission()) {
            Log.d(TAG, "Démarrage des mises à jour de localisation")
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.d(TAG, "Impossible de démarrer les mises à jour: permissions non accordées")
        }
    }

    fun stopLocationUpdates() {
        Log.d(TAG, "Arrêt des mises à jour de localisation")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(): Flow<Location> {
        if (hasLocationPermission()) {
            Log.d(TAG, "Récupération de la localisation actuelle")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        Log.d(TAG, "Localisation actuelle: ${it.latitude}, ${it.longitude}")
                        _currentLocation.value = it
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erreur lors de la récupération de la localisation actuelle", e)
                }
        } else {
            Log.d(TAG, "Permissions de localisation non accordées")
        }

        // Utilisez filterNotNull() pour transformer StateFlow<Location?> en Flow<Location>
        return _currentLocation.asStateFlow().filterNotNull()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}