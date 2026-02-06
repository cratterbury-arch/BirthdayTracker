package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val birthdayFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun ContactModel.birthdayDate(): LocalDate? =
    try {
        birthday?.let { LocalDate.parse(it, birthdayFormatter) }
    } catch (_: Exception) {
        null
    }

fun ContactModel.isBirthdayOn(date: LocalDate): Boolean {
    val birth = birthdayDate() ?: return false
    return birth.dayOfMonth == date.dayOfMonth &&
            birth.month == date.month
}

fun ContactModel.ageOnDate(date: LocalDate): Int? {
    val birth = birthdayDate() ?: return null
    var age = date.year - birth.year
    if (date < birth.withYear(date.year)) {
        age--
    }
    return age
}
