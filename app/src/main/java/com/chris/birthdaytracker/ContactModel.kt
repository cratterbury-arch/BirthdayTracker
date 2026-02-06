package com.chris.birthdaytracker

import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ContactModel(
    val id: Long,
    val displayName: String,
    val birthday: String?, // dd/MM/yyyy
    val photoUri: Uri?
) {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun nextBirthday(from: LocalDate = LocalDate.now()): LocalDate? {
        birthday ?: return null
        val date = LocalDate.parse(birthday, formatter)

        var next = date.withYear(from.year)
        if (!next.isAfter(from)) {
            next = next.plusYears(1)
        }
        return next
    }

    fun daysUntilBirthday(from: LocalDate = LocalDate.now()): Long? {
        val next = nextBirthday(from) ?: return null
        return ChronoUnit.DAYS.between(from, next)
    }

    fun ageOnNextBirthday(from: LocalDate = LocalDate.now()): Int? {
        birthday ?: return null
        val birthDate = LocalDate.parse(birthday, formatter)
        val next = nextBirthday(from) ?: return null
        return ChronoUnit.YEARS.between(birthDate, next).toInt()
    }
}
