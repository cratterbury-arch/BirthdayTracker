package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun nextBirthday(birthday: LocalDate): LocalDate {
    val today = LocalDate.now()
    var next = birthday.withYear(today.year)
    if (next.isBefore(today)) {
        next = next.plusYears(1)
    }
    return next
}

fun daysUntilBirthday(birthday: LocalDate): Long {
    return ChronoUnit.DAYS.between(LocalDate.now(), nextBirthday(birthday))
}

fun ageOnNextBirthday(birthday: LocalDate): Int {
    return nextBirthday(birthday).year - birthday.year
}
