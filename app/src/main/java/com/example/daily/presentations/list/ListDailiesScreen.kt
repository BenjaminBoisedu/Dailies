package com.example.daily.presentations.list

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.daily.R
import com.example.daily.presentations.components.DailyCard
import com.example.daily.presentations.navigation.Screen
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.daily.sensor.LocationViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daily.presentations.meteo.MeteoViewModel
import com.google.maps.android.compose.Circle
import kotlin.math.roundToInt

@SuppressLint("MutableCollectionMutableState", "CoroutineCreationDuringComposition",
    "SuspiciousIndentation"
)
@Composable
fun ListDailiesScreen(
    navController: NavController,
    viewModel: ListDailiesViewModel  = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel(),
    meteoviewmodel:MeteoViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission accordée, mettre à jour la météo
            meteoviewmodel.getWeatherFromCurrentLocation()
        } else {
            // Permission refusée, afficher un message à l'utilisateur
            scope.launch {
                snackbarHostState.showSnackbar("Permission de localisation refusée")
            }
        }
    }
    val weatherData by meteoviewmodel.weatherData.observeAsState()
    val isLoading by meteoviewmodel.isLoading.observeAsState(false)
    val error by meteoviewmodel.error.observeAsState()

    DisposableEffect(key1 = true) {
        locationViewModel.startLocationUpdates()
        onDispose {
            locationViewModel.stopLocationUpdates()
        }
    }
        Scaffold (
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddEditDailyScreen.route)
                        },
                        Modifier.padding(16.dp),
                        containerColor = Color(0xFF7684A7),
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        shape = MaterialTheme.shapes.medium,

                        )
                    {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter une routine",
                        )
                    }
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.StatsScreen.route) },
                        containerColor = Color(0xFF99A4BE),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Statistiques"
                        )
                    }
                }
            }

        ) { contentPadding ->
            Column (Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(Color(0xFF303030)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                )
                Row (modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(110.dp)
                    .background(color = Color(0xFF303030)),
                ) {
                    if (weatherData == null && error == null && !isLoading) {
                        val hasLocationPermission = ContextCompat.checkSelfPermission(
                            navController.context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasLocationPermission) {
                            meteoviewmodel.getWeatherFromCurrentLocation()
                        } else {
                            // Centrer le bouton dans la Row
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
                    weatherData?.let { data ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,  // Centrage vertical ajouté
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = "https://openweathermap.org/img/wn/${data.weather.firstOrNull()?.icon}@4x.png",
                                    contentDescription = "Icône météo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            color = Color(0xFF7684A7),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            navController.navigate(Screen.MeteoScreen.route)
                                        }
                                )
                                Spacer(modifier = Modifier.height(8.dp))  // Remplace le padding top
                                Text(
                                    text = (data.weather.firstOrNull()?.description ?: "").replaceFirstChar {
                                        it.uppercase()
                                    },
                                    style = TextStyle(color = Color.White, fontSize = 16.sp)
                                )
                            }

                            // Côté droit: ville + température
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,  // Centrage vertical ajouté
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = data.name.replaceFirstChar { it.uppercase() },
                                    style = TextStyle(color = Color.White, fontSize = 20.sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))  // Espacement constant
                                Text(
                                    text = "${data.main.temp.roundToInt()}°C",
                                    style = TextStyle(color = Color.White, fontSize = 24.sp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Mes routines",
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Transparent),
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center

                        )
                    )
                    var expanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent)) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier.background(Color(0xFF303030)),
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                modifier = Modifier.background(Color.Transparent),
                                text = { Text("Paramètres") },
                                onClick = { /* Handle settings click */ },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                colors = MenuItemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color.White,
                                    trailingIconColor = Color.White,
                                    disabledTextColor = Color.White,
                                    disabledLeadingIconColor = Color.White,
                                    disabledTrailingIconColor = Color.White,
                                )
                            )
                            DropdownMenuItem(
                                modifier = Modifier.background(Color.Transparent),
                                text = { Text("A propos") },
                                onClick = { /* Handle about click */ },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                colors = MenuItemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color.White,
                                    trailingIconColor = Color.White,
                                    disabledTextColor = Color.White,
                                    disabledLeadingIconColor = Color.White,
                                    disabledTrailingIconColor = Color.White,
                                )
                            )
                            // Dans le DropdownMenu, après l'option "A propos"
                            DropdownMenuItem(
                                modifier = Modifier.background(Color.Transparent),
                                text = { Text("Localisation") },
                                onClick = { navController.navigate(Screen.LocationScreen.route) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                colors = MenuItemColors(
                                    textColor = Color.White,
                                    leadingIconColor = Color.White,
                                    trailingIconColor = Color.White,
                                    disabledTextColor = Color.White,
                                    disabledLeadingIconColor = Color.White,
                                    disabledTrailingIconColor = Color.White,
                                )
                            )
                        }
                    }

                }

                LazyColumn (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(viewModel.dailies.value) { daily ->
                        DailyCard(daily = daily, onDeleteClick = {
                            viewModel.onEvent(DailyEvent.Delete(daily))
                            scope.launch { snackbarHostState.showSnackbar("Routine supprimée")
                            }},
                            onEditClick = { navController.navigate(Screen.AddEditDailyScreen.route + "?dailyId=${daily.id}") },
                        )
                    }
                }
            }
        }
}
