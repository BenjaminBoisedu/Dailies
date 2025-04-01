package com.example.daily.presentations.splashscreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.daily.R
import com.example.daily.presentations.navigation.Screen
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun SplashScreen(navController: NavHostController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = ""
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 1f,
        animationSpec = tween(durationMillis = 1500),
        label = ""
    )
    val rotateAnim = animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = ""
    )

    // Utiliser Animatable pour une animation plus fluide
    val animatableOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatableOffset.animateTo(
                targetValue = 200f,
                animationSpec = tween(1500, easing = LinearEasing)
            )
            animatableOffset.animateTo(
                targetValue = -200f,
                animationSpec = tween(1500, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        navController.navigate(Screen.DailiesListScreen.route) {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    Splash(
        alpha = alphaAnim.value,
        scale = scaleAnim.value,
        rotation = rotateAnim.value,
        diagonalOffset = animatableOffset.value
    )
}

@Composable
fun Splash(alpha: Float, scale: Float, rotation: Float, diagonalOffset: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF303030)),
        contentAlignment = Alignment.Center
    ) {
        // Bandes diagonales animées
        DiagonalStripes(diagonalOffset = diagonalOffset)

        // Logo
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de l'application Daily",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(alpha)
                    .scale(scale)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun DiagonalStripes(diagonalOffset: Float) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val stripeWidth = 40f
        val stripeSpacing = 100f
        val primaryColor = Color(0xFF7684A7)

        val stripeColors = listOf(
            primaryColor.copy(alpha = 0.2f),
            primaryColor.copy(alpha = 0.25f),
            primaryColor.copy(alpha = 0.3f)
        )

        // Limiter le nombre de bandes pour de meilleures performances
        val visibleRange = -8..8

        withTransform({
            rotate(45f, Offset(size.width / 2f, size.height / 2f))
        }) {
            for (i in visibleRange) {

                // Alterner les couleurs pour créer un effet de profondeur
                val colorIndex = (i % stripeColors.size).let { if (it < 0) it + stripeColors.size else it }
                val stripeColor = stripeColors[colorIndex]

                val xPos = size.width / 2 + i * stripeSpacing + diagonalOffset
                drawRect(
                    color = stripeColor,
                    topLeft = Offset(xPos, -size.height),
                    size = Size(stripeWidth, size.height * 3)
                )
            }
        }
    }
}