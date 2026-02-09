package com.chris.birthdaytracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.fillMaxSize
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun BirthdayConfetti(
    modifier: Modifier = Modifier
) {
    KonfettiView(
        modifier = modifier.fillMaxSize(),
        parties = listOf(

            // 🎉 Top burst
            Party(
                speed = 0f,
                maxSpeed = 35f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(200),
                position = Position.Relative(0.5, 0.0)
            ),

            // 🎊 Left → Right
            Party(
                speed = 8f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = 0,
                spread = 90,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(180),
                position = Position.Relative(0.08, 0.3)
            ),

            // 🎊 Right → Left
            Party(
                speed = 8f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = 180,
                spread = 90,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(180),
                position = Position.Relative(0.92, 0.3)
            )
        )
    )
}
