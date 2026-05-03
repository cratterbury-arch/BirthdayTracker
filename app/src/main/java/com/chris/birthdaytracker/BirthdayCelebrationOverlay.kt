package com.chris.birthdaytracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun BirthdayCelebrationOverlay(
    modifier: Modifier = Modifier
) {
    KonfettiView(
        modifier = modifier.zIndex(2f),
        parties = listOf(

            // 🎊 MAIN TOP BURST
            Party(
                speed = 10f,
                maxSpeed = 45f,
                damping = 0.85f,
                spread = 360,
                colors = listOf(
                    0xfce18a,
                    0xff726d,
                    0xf4306d,
                    0xb48def,
                    0x6A4C93
                ),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(220),
                position = Position.Relative(0.5, 0.0)
            ),

            // 🎊 LEFT EDGE SPRAY (EXPLICITLY OFFSCREEN)
            Party(
                speed = 8f,
                maxSpeed = 35f,
                damping = 0.9f,
                spread = 120,
                colors = listOf(
                    0xff726d,
                    0xf4306d,
                    0xb48def
                ),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(140),
                position = Position.Relative(-0.05, 0.3)
            ),

            // 🎊 RIGHT EDGE SPRAY (EXPLICITLY OFFSCREEN)
            Party(
                speed = 8f,
                maxSpeed = 35f,
                damping = 0.9f,
                spread = 120,
                angle = 180,
                colors = listOf(
                    0xff726d,
                    0xf4306d,
                    0xb48def
                ),
                emitter = Emitter(2, TimeUnit.SECONDS).perSecond(140),
                position = Position.Relative(1.05, 0.3)
            )
        )
    )
}
