package com.chris.birthdaytracker

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun formatBirthdayInput(value: TextFieldValue): TextFieldValue {
    val digits = value.text.filter { it.isDigit() }.take(8)

    val formatted = buildString {
        digits.forEachIndexed { index, c ->
            append(c)
            if (index == 1 || index == 3) {
                if (index != digits.lastIndex) append('/')
            }
        }
    }

    val newCursor = formatted.length.coerceAtMost(formatted.length)

    return TextFieldValue(
        text = formatted,
        selection = TextRange(newCursor)
    )
}
