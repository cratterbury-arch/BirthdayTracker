package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun LocalDate.daysUntilNextBirthday(today: LocalDate = LocalDate.now()): Long {
    val nextBirthday = withYear(today.year).let {
        if (it.isBefore(today) || it.isEqual(today)) {
            it.plusYears(1)
        } else {
            it
        }
    }
    return ChronoUnit.DAYS.between(today, nextBirthday)
}
