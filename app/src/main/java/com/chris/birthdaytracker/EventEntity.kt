package com.chris.birthdaytracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: LocalDate,
    val type: String,
    val tags: String = "",
    val isFavorite: Boolean = false
)
