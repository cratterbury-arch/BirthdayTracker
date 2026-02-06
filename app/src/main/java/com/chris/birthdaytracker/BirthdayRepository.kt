package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BirthdayRepository {

    private val birthdayFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getContactsWithBirthday(context: Context): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.CONTACT_ID,                              // 0
                ContactsContract.Data.DISPLAY_NAME,                            // 1
                ContactsContract.CommonDataKinds.Event.START_DATE,             // 2
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI               // 3
            ),
            "${ContactsContract.Data.MIMETYPE}=? AND ${ContactsContract.CommonDataKinds.Event.TYPE}=?",
            arrayOf(
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
            ),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {

                val id = it.getLong(0)
                val name = it.getString(1) ?: continue
                val rawDate = it.getString(2) ?: continue
                val photoUriString = it.getString(3)

                val formattedBirthday = normalizeBirthday(rawDate) ?: continue
                val photoUri = photoUriString?.let { uri -> Uri.parse(uri) }

                contacts.add(
                    ContactModel(
                        id = id,
                        displayName = name,
                        birthday = formattedBirthday,
                        photoUri = photoUri
                    )
                )
            }
        }

        return contacts
    }

    private fun normalizeBirthday(raw: String): String? {
        return try {
            when {
                raw.contains("-") -> {
                    val parsed = LocalDate.parse(raw)
                    parsed.format(birthdayFormatter)
                }
                raw.contains("/") -> raw
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
