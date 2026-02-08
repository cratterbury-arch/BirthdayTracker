package com.chris.birthdaytracker

import android.net.Uri
import java.time.LocalDate

data class ContactModel(
    val id: Long,
    val name: String,
    val birthday: LocalDate?,
    val photoUri: Uri?
)
