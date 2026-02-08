package com.chris.birthdaytracker

data class ContactModel(
    val id: Long,
    val displayName: String,
    val birthday: String? = null,
    val photoUri: String? = null,
    val isLocal: Boolean = false
)
