package com.chris.birthdaytracker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class EventModel(
    val id: String,
    val title: String,
    val date: LocalDate,
    val type: String, // Anniversary, Pet, etc.
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
) : Parcelable
