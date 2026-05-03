package com.chris.birthdaytracker

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BirthdayRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun nextBirthday(birthday: String?): LocalDate? {
        if (birthday.isNullOrBlank()) return null

        val birthDate = try {
            LocalDate.parse(birthday, formatter)
        } catch (e: Exception) {
            return null
        }

        val today = LocalDate.now()

        var next = birthDate.withYear(today.year)

        if (next.isBefore(today)) {
            next = next.withYear(today.year + 1)
        }

        return next
    }

    fun isBirthdayToday(birthday: String?): Boolean {
        val next = nextBirthday(birthday) ?: return false
        return next == LocalDate.now()
    }
}
