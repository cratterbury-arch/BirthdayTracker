package com.chris.birthdaytracker

fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }

    val builder = StringBuilder()

    for (i in digits.indices) {
        builder.append(digits[i])

        if (i == 1 || i == 3) {
            if (i != digits.lastIndex) {
                builder.append('/')
            }
        }

        if (builder.length >= 10) break
    }

    return builder.toString()
}
