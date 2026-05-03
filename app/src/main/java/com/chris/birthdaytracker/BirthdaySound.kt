package com.chris.birthdaytracker

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun playBirthdaySound(context: Context) {
    // App setting
    val enabled = runBlocking {
        SettingsStore.soundEnabled(context).first()
    }
    if (!enabled) return

    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Respect silent / vibrate mode
    if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return

    val player = MediaPlayer.create(context, R.raw.birthday_pop)
    player.setOnCompletionListener { it.release() }
    player.start()
}
