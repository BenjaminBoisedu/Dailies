package com.example.daily.presentations.details

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.daily.presentations.HighPriority
import com.example.daily.presentations.list.ListDailiesViewModel
import com.example.daily.presentations.navigation.Screen

@SuppressLint("MutableCollectionMutableState")
@Composable
fun DetailDailyScreen(navController: NavHostController, viewModel: ListDailiesViewModel) {

    val daily = viewModel.dailies.value[0]
    val dailyTitle = daily.title
    val dailyDate = daily.date
    val dailyTime = daily.time
    val dailyPriority = daily.priority
    val dailyDone = daily.done


    Scaffold (
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddEditDailyScreen.route)
            },
                Modifier.padding(16.dp))
            {
                Icon(imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une routine",)
            }
        }
    ) { contentPadding ->
        Column(Modifier.padding(contentPadding)) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.DailiesListScreen.route)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterVertically),
                    containerColor = Color(0xFF6E4586),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(5.dp, 0.dp, 7.dp),
                    shape = Shapes().small
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retourner à la liste des routines",
                    )
                }
                Text(
                    text = "Détails de la routine",
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterVertically),
                    style = TextStyle(
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.15.sp,
                        color = Color(0xFF6E4586),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif


                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dailyTitle,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Transparent),
                        style = TextStyle(
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            color = if (dailyPriority == HighPriority) Color.Red else Color(0xFFFFA500)

                        )
                    )
                    if (dailyPriority == HighPriority) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Haut Priorité",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterVertically),
                            tint = Color.Red

                        )
                    }else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Priorité Standard",
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterVertically),
                            tint = Color(0xFFFFA500)
                        )
                    }
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dailyDate,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Transparent),
                        style = TextStyle(
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center

                        )
                    )
                    Text(
                        text = dailyTime,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Transparent),
                        style = TextStyle(
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center

                        )
                    )
                    if (dailyDone) {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = "Done",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Transparent),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center

                                )
                            )
                            IconButton(
                                onClick = {
                                    viewModel.deleteDaily(daily)
                                    navController.navigate(Screen.DailiesListScreen.route)
                                },
                                modifier = Modifier.padding(8.dp)
                            ) { }
                        }
                    } else {
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pas Terminé",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Transparent),
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center

                                )
                            )
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.AddEditDailyScreen.route)
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier",
                                    Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}