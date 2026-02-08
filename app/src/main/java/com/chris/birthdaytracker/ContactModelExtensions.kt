package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

/* ---------- Date helpers (LOGIC) ---------- */

fun ContactModel.nextBirthday(from: LocalDate = LocalDate.now()): LocalDate? {
    val birth = birthday ?: return null

    var next = birth.withYear(from.year)
    if (next.isBefore(from)) {
        next = next.plusYears(1)
    }
    return next
}

fun ContactModel.daysUntilBirthday(from: LocalDate = LocalDate.now()): Int? {
    val next = nextBirthday(from) ?: return null
    return ChronoUnit.DAYS.between(from, next).toInt()
}

fun ContactModel.ageOnDate(date: LocalDate): Int? {
    val birth = birthday ?: return null
    return date.year - birth.year
}

/* ---------- UI helpers (STRINGS ONLY) ---------- */

fun ContactModel.formattedBirthday(): String {
    return birthday
        ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        ?: "Birthday unknown"
}
