package com.chris.birthdaytracker

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun BirthdayKonfetti(
    enabled: Boolean
) {
    if (!enabled) return

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        // 🔊 Try sound first
        try {
            val player = MediaPlayer.create(context, R.raw.birthday_pop) // <-- your sound file
            player.start()
            player.setOnCompletionListener { it.release() }
        } catch (_: Exception) {
            // 📳 Fallback haptic
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        // 🎉 Confetti hook point
        // When ready, plug Konfetti here (no UI dependency yet)
    }
}
