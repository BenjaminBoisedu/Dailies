package com.example.daily.presentations.graph

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.daily.presentations.list.ListDailiesViewModel
import java.util.SortedMap

@SuppressLint("DefaultLocale")
@Composable
fun StatsScreen(
    viewModel: ListDailiesViewModel,
    statsViewModel: StatsViewModel,
    navController: NavController
) {
    val statsData by statsViewModel.statsData.collectAsState()
    val isLoading by statsViewModel.isLoading.collectAsState()

    // Fusion des données du ViewModel et des données calculées localement
    val mergedData = remember(statsData, viewModel.dailies.value) {
        val dailies = viewModel.dailies.value
        val completedDailies = dailies.filter { it.done }

        // Combiner les données du repository avec celles calculées localement
        val completedByDate = completedDailies
            .groupBy { it.date }
            .mapValues { entry -> entry.value.size }

        // Fusionner avec les données du StatsViewModel
        val merged = (statsData.keys + completedByDate.keys).associateWith { date ->
            val fromStats = statsData[date] ?: 0
            val fromLocal = completedByDate[date] ?: 0
            maxOf(fromStats, fromLocal)
        }

        merged.toSortedMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .padding(16.dp)
    ) {
        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth()
            .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && mergedData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF99A4BE))
            }
        } else if (mergedData.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aucune routine complétée pour le moment",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Contenu des statistiques
            StatsContent(data = mergedData)
        }
    }
}

@Composable
fun StatsContent(data: SortedMap<String, Int>) {
    Column {
        // Graphique avec Compose Canvas
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

                Spacer(modifier = Modifier.height(8.dp))

                SimpleBarChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Résumé des statistiques
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF303030))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Résumé",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val totalCompleted = data.values.sum()
                val bestDay = data.maxByOrNull { it.value }
                val averagePerDay = if (data.isNotEmpty()) {
                    data.values.sum().toFloat() / data.size
                } else 0f

                StatItem(
                    title = "Total des routines complétées",
                    value = totalCompleted.toString()
                )

                Spacer(modifier = Modifier.height(8.dp))

                bestDay?.let {
                    StatItem(
                        title = "Meilleur jour",
                        value = "${it.key} (${it.value} routines)"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatItem(
                    title = "Moyenne par jour",
                    value = String.format("%.1f", averagePerDay)
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

    // Trier les données par date
    val sortedData = data.toSortedMap()
    val maxValue = sortedData.values.maxOrNull() ?: 1

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 20.dp,
                    bottom = 30.dp,
                    start = 10.dp,
                    end = 10.dp
                )
        ) {
            val barWidth = size.width / (sortedData.size * 2)
            val maxHeight = size.height - 30

            // Dessiner les barres
            sortedData.entries.forEachIndexed { index, entry ->
                val barHeight = (entry.value.toFloat() / maxValue) * maxHeight
                val xOffset = index * (barWidth * 2) + barWidth / 2

                drawRect(
                    color = Color(0xFF99A4BE),
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )

                // Dessiner les valeurs au-dessus des barres
                drawContext.canvas.nativeCanvas.drawText(
                    entry.value.toString(),
                    xOffset + barWidth / 2,
                    size.height - barHeight - 10,
                    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = Color.White.toArgb()
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                // Dessiner les labels des dates en bas
                val label = entry.key.takeLast(5) // Format court de la date
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    xOffset + barWidth / 2,
                    size.height + 15,
                    androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = Color.White.copy(alpha = 0.7f).toArgb()
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