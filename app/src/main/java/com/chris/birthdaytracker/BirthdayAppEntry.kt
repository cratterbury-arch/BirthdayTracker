package com.chris.birthdaytracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BirthdayAppEntry() {

    var showIntro by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1400)
        showIntro = false
    }

    Box {
        AppRoot()

        AnimatedVisibility(
            visible = showIntro,
            exit = fadeOut(animationSpec = tween(700)) +
                    scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(700, easing = FastOutSlowInEasing)
                    )
        ) {
            IntroScreen()
        }
    }
}

@Composable
private fun IntroScreen() {

    val infiniteTransition = rememberInfiniteTransition(label = "Floating")

    // Bigger float movement
    val floatY by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Float"
    )

    // Glow pulse
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            SparkleLayer()

            // Soft glow behind logo
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .scale(glowScale)
                    .alpha(0.25f)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFC1E3),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2
                )
            }

            ShimmerLayer()

            Image(
                painter = painterResource(id = R.drawable.splashscreen),
                contentDescription = "Birthday Tracker Logo",
                modifier = Modifier
                    .size(220.dp)
                    .offset(y = floatY.dp)
            )
        }
    }
}

@Composable
private fun SparkleLayer() {

    val infiniteTransition = rememberInfiniteTransition(label = "Sparkles")

    val sparkleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SparkleDrift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {

        val random = Random(0)

        repeat(20) {

            val x = random.nextFloat() * size.width
            val y = (random.nextFloat() * size.height + sparkleOffset * 50f) % size.height
            val radius = random.nextFloat() * 5f + 2f
            val alpha = random.nextFloat() * 0.7f + 0.3f

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun ShimmerLayer() {

    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerMove"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(shimmerX, 0f),
                end = Offset(shimmerX + 300f, size.height)
            )
        )
    }
}
