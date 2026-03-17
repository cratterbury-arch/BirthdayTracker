package com.chris.birthdaytracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "local_contacts")
data class LocalContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val birthday: LocalDate,
    val isFromPhone: Boolean = false,
    val photoUri: String? = null
)
