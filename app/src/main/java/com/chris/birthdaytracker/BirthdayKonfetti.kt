package com.chris.birthdaytracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun BirthdayKonfetti(
    modifier: Modifier = Modifier
) {
    KonfettiView(
        modifier = modifier.fillMaxSize(),
        parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = 25f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(
                    0xFFFCE18A.toInt(),
                    0xFFFF726D.toInt(),
                    0xFFB48DEF.toInt()
                ),
                emitter = Emitter(
                    duration = 200,
                    TimeUnit.MILLISECONDS
                ).max(150),
                position = Position.Relative(0.5, 0.2)
            )
        )
    )
}
