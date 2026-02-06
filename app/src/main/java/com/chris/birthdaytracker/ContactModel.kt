package com.chris.birthdaytracker

import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ContactModel(
    val id: Long,
    val displayName: String,
    val birthday: String?, // dd/MM/yyyy (year may be placeholder)
    val photoUri: Uri?
) {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private fun parsedBirthday(): LocalDate? =
        try {
            birthday?.let { LocalDate.parse(it, formatter) }
        } catch (_: Exception) {
            null
        }

    /** True if birthday is today (ignores year) */
    fun isBirthdayToday(today: LocalDate = LocalDate.now()): Boolean {
        val date = parsedBirthday() ?: return false
        return date.dayOfMonth == today.dayOfMonth &&
                date.month == today.month
    }

    /** Next birthday date (today counts as today, not next year) */
    fun nextBirthday(from: LocalDate = LocalDate.now()): LocalDate? {
        val birthDate = parsedBirthday() ?: return null

        var next = birthDate.withYear(from.year)

        if (next.isBefore(from)) {
            next = next.plusYears(1)
        }

        return next
    }

    /** Days until birthday (today = 0) */
    fun daysUntilBirthday(from: LocalDate = LocalDate.now()): Long? {
        if (isBirthdayToday(from)) return 0
        val next = nextBirthday(from) ?: return null
        return ChronoUnit.DAYS.between(from, next)
    }

    /** Age on the next birthday */
    fun ageOnNextBirthday(from: LocalDate = LocalDate.now()): Int? {
        val birthDate = parsedBirthday() ?: return null
        val next = nextBirthday(from) ?: return null
        return ChronoUnit.YEARS.between(birthDate, next).toInt()
    }

    /** Age today (used for today-birthday UI) */
    fun ageToday(from: LocalDate = LocalDate.now()): Int? {
        val birthDate = parsedBirthday() ?: return null
        return ChronoUnit.YEARS.between(birthDate, from).toInt()
    }
}
