package com.chris.birthdaytracker

import android.content.Context
import android.media.MediaPlayer

fun playBirthdaySound(context: Context) {
    val player = MediaPlayer.create(context, R.raw.birthday_pop)
    player.setOnCompletionListener { it.release() }
    player.start()
}
