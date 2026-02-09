package com.chris.birthdaytracker

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

fun playBirthdaySound(context: Context) {
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Respect system silent / vibrate mode
    if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
        return
    }

    val player = MediaPlayer.create(context, R.raw.birthday_pop)
    player.setOnCompletionListener { it.release() }
    player.start()
}
