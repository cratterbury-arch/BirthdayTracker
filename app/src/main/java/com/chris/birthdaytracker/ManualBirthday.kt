package com.chris.birthdaytracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manual_birthdays")
data class ManualBirthday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val date: String, // Store as "YYYY-MM-DD"
    val photoUri: String? = null,
    val isPet: Boolean = false
)