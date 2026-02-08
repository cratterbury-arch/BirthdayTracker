package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun ContactModel.birthDate(): LocalDate? =
    birthday?.let {
        runCatching { LocalDate.parse(it, formatter) }.getOrNull()
    }

fun ContactModel.daysUntilBirthday(today: LocalDate = LocalDate.now()): Long? {
    val birth = birthDate() ?: return null
    var next = birth.withYear(today.year)
    if (!next.isAfter(today)) next = next.plusYears(1)
    return ChronoUnit.DAYS.between(today, next)
}

fun ContactModel.isBirthdayToday(today: LocalDate = LocalDate.now()): Boolean =
    birthDate()?.let {
        it.dayOfMonth == today.dayOfMonth && it.month == today.month
    } ?: false

fun ContactModel.ageOnDate(date: LocalDate): Int? =
    birthDate()?.let {
        ChronoUnit.YEARS.between(it, date).toInt()
    }

fun ContactModel.nextBirthday(today: LocalDate = LocalDate.now()): LocalDate? {
    val birth = birthDate() ?: return null
    var next = birth.withYear(today.year)
    if (!next.isAfter(today)) next = next.plusYears(1)
    return next
}
