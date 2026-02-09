package com.chris.birthdaytracker

import androidx.compose.ui.graphics.Color

fun contactKonfettiColor(contact: ContactModel): Color {
    val palette = listOf(
        Color(0xFFE57373),
        Color(0xFFBA68C8),
        Color(0xFF64B5F6),
        Color(0xFF81C784),
        Color(0xFFFFB74D)
    )
    return palette[kotlin.math.abs(contact.name.hashCode()) % palette.size]
}
