package com.example.daily.presentations.list

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
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
import com.example.daily.sensor.LocationViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState

@SuppressLint("MutableCollectionMutableState", "CoroutineCreationDuringComposition",
    "SuspiciousIndentation"
)
@Composable
fun ListDailiesScreen(navController: NavController, viewModel: ListDailiesViewModel  = hiltViewModel(), locationViewModel: LocationViewModel = hiltViewModel()
) {

    val location by locationViewModel.currentLocation.collectAsState()

    DisposableEffect(key1 = true) {
        locationViewModel.startLocationUpdates()
        onDispose {
            locationViewModel.stopLocationUpdates()
        }
    }

    var filterByLocation by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

        Scaffold (
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate(Screen.AddEditDailyScreen.route)
                },
                    Modifier.padding(16.dp),
                    containerColor = Color(0xFF7684A7),
                    contentColor = Color.Black,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    shape = MaterialTheme.shapes.medium,

                )
                    {
                    Icon(imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une routine",)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Drawable of logo

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
