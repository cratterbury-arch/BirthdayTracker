package com.chris.birthdaytracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_metadata")
data class ContactMetadataEntity(
    @PrimaryKey val contactKey: String, // name_month_day
    val tags: String = "",
    val isFavorite: Boolean = false
)
