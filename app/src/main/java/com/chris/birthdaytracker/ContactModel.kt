package com.chris.birthdaytracker

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class ContactModel(
    val id: Long,
    val name: String,
    val birthday: LocalDate?,
    val photoUri: Uri?
) : Parcelable
