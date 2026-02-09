package com.chris.birthdaytracker

import android.content.Context
import androidx.compose.runtime.*
import java.time.LocalDate

@Composable
fun BirthdayCelebrationEffect(
    enabledSound: Boolean,
    enabledConfetti: Boolean,
    isBirthdayToday: Boolean,
    context: Context,
    showConfetti: @Composable () -> Unit
) {
    var fired by remember { mutableStateOf(false) }

    if (isBirthdayToday && !fired) {
        LaunchedEffect(Unit) {
            if (enabledSound) {
                playBirthdaySound(context)
            }
            fired = true
        }
    }

    if (isBirthdayToday && enabledConfetti) {
        showConfetti()
    }
}
