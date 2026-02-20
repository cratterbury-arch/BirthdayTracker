package com.chris.birthdaytracker

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onFinished: () -> Unit) {

    val infiniteTransition = rememberInfiniteTransition()

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    var startExit by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startExit) 0.8f else 1f,
        animationSpec = tween(600),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(600),
        label = ""
    )

    LaunchedEffect(Unit) {
        delay(2000)
        startExit = true
        delay(600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF111827),
                        Color(0xFF1F2937)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = null,
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .scale(scale)
                .alpha(alpha)
                .size(220.dp)
        )
    }
}
