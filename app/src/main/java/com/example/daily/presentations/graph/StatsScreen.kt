package com.example.daily.presentations.graph

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

import java.util.SortedMap
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("DefaultLocale")
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val statsData by viewModel.statsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val incompleteDailies by viewModel.incompleteDailies.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Titre de la page
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Icon ou bouton de retour
            if (navController != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
            }
        }


        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section graphique existante
                if (statsData.isNotEmpty()) {
                    StatsContent(statsData.toSortedMap(), onFixDates = { viewModel.fixMissingDates() })
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Aucune donnée disponible")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Statistiques générales existantes
                Text(
                    text = "Statistiques générales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                StatItem(
                    title = "Total de routines complétées",
                    value = statsData.values.sum().toString()
                )
                StatItem(
                    title = "Total de routines non complétées",
                    value = incompleteDailies.size.toString()
                )
                StatItem(
                    title = "Total de routines",
                    value = (statsData.values.sum() + incompleteDailies.size).toString()
                )
                StatItem(
                    title = "Moyenne de routines complétées par jour",
                    value = if (statsData.isNotEmpty()) {
                        String.format("%.2f", statsData.values.average())
                    } else {
                        "0.00"
                    }
                )

                // Nouvelle section pour les routines non terminées
                if (incompleteDailies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Routines non terminées",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            incompleteDailies.forEach { daily ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = daily.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                }

                                // Afficher la description (optionnel)
                                if (daily.description.isNotEmpty()) {
                                    Text(
                                        text = daily.description,
                                        color = Color.White.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    )
                                }

                                Divider(
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StatsContent(
    data: SortedMap<String, Int>,
    onFixDates: () -> Unit
) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Routines complétées par jour",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Afficher un avertissement si des routines sans date existent
                if (data.containsKey("Non défini")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${data["Non défini"]} routines sans date",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Yellow
                        )
                        Button(
                            onClick = onFixDates,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FAF)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Corriger", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Formatter pour parser les dates au format dd-MM-yyyy
                val inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                // Formatter pour l'affichage au format jour mois
                val displayFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

                // Conversion et regroupement des données par jour
                val groupedData = mutableMapOf<String, Int>()

                data.forEach { (dateStr, count) ->
                    if (dateStr == "Non défini") {
                        groupedData["Non défini"] = count
                    } else {
                        try {
                            val date = LocalDate.parse(dateStr, inputFormatter)
                            val formattedDate = date.format(displayFormatter)
                            groupedData[formattedDate] = (groupedData[formattedDate] ?: 0) + count
                        } catch (e: Exception) {
                            groupedData[dateStr] = count
                        }
                    }
                }

                SimpleBarChart(
                    data = groupedData.toSortedMap(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

            }
        }
    }
}

@Composable
fun SimpleBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val sortedData = data.entries.toList()
    val maxValue = sortedData.maxOfOrNull { it.value } ?: 1

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 20.dp,
                    bottom = 40.dp,
                    start = 10.dp,
                    end = 10.dp
                )
        ) {
            val barWidth = (size.width - 10) / (sortedData.size * 3f)
            val maxHeight = size.height - 60
            val spaceBetweenBars = barWidth * 0.5f

            // Dessiner les barres
            sortedData.forEachIndexed { index, entry ->
                val barHeight = (entry.value.toFloat() / maxValue) * maxHeight
                val xOffset = index * (barWidth + spaceBetweenBars) + 10

                // Dessiner la barre
                drawRect(
                    color = Color(0xFF7684A7),
                    topLeft = Offset(xOffset, size.height - 40 - barHeight),
                    size = Size(barWidth, barHeight)
                )

                // Dessiner la valeur au-dessus de la barre
                drawContext.canvas.nativeCanvas.drawText(
                    entry.value.toString(),
                    xOffset + barWidth / 2,
                    size.height - 40 - barHeight - 10,
                    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = Color.White.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                // Dessiner la date sous la barre
                drawContext.canvas.nativeCanvas.drawText(
                    entry.key,
                    xOffset + barWidth / 2,
                    size.height - 10,
                    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = Color.White.copy(alpha = 0.8f).toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}


@Composable
fun StatItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}