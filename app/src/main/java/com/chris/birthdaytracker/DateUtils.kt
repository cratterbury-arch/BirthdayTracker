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

fun getZodiacSign(date: LocalDate): String {
    val day = date.dayOfMonth
    return when (date.monthValue) {
        1 -> if (day < 20) "Capricorn ♑" else "Aquarius ♒"
        2 -> if (day < 19) "Aquarius ♒" else "Pisces ♓"
        3 -> if (day < 21) "Pisces ♓" else "Aries ♈"
        4 -> if (day < 20) "Aries ♈" else "Taurus ♉"
        5 -> if (day < 21) "Taurus ♉" else "Gemini ♊"
        6 -> if (day < 21) "Gemini ♊" else "Cancer ♋"
        7 -> if (day < 23) "Cancer ♋" else "Leo ♌"
        8 -> if (day < 23) "Leo ♌" else "Virgo ♍"
        9 -> if (day < 23) "Virgo ♍" else "Libra ♎"
        10 -> if (day < 23) "Libra ♎" else "Scorpio ♏"
        11 -> if (day < 22) "Scorpio ♏" else "Sagittarius ♐"
        12 -> if (day < 22) "Sagittarius ♐" else "Capricorn ♑"
        else -> ""
    }
}

fun getChineseZodiac(date: LocalDate): String {
    val year = date.year
    if (year < 1900) return ""
    return when ((year - 1900) % 12) {
        0 -> "Rat 🐀"
        1 -> "Ox 🐂"
        2 -> "Tiger 🐅"
        3 -> "Rabbit 🐇"
        4 -> "Dragon 🐉"
        5 -> "Snake 🐍"
        6 -> "Horse 🐎"
        7 -> "Goat 🐐"
        8 -> "Monkey 🐒"
        9 -> "Rooster 🐓"
        10 -> "Dog 🐕"
        11 -> "Pig 🐖"
        else -> ""
    }
}
