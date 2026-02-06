package com.chris.birthdaytracker

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ContactsRepository(
    private val context: Context
) {

    private val resolver: ContentResolver = context.contentResolver

    /* =========================================================
       READ CONTACTS
       ========================================================= */

    fun getContacts(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        val cursor = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.Contacts.PHOTO_URI
            ),
            "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?",
            arrayOf(
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
            ),
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
            val photoIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val rawDate = it.getString(dateIndex)
                val photoUri = it.getString(photoIndex)?.let(Uri::parse)

                val formattedBirthday = rawDate?.let { normalizeBirthday(it) }

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

    /* =========================================================
       WRITE / UPDATE BIRTHDAY (CRASH-SAFE)
       ========================================================= */

    fun updateBirthday(
        contactId: Long,
        birthday: String
    ) {
        // Expect dd/MM/yyyy
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(birthday, formatter)

        val isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // yyyy-MM-dd

        val values = ContentValues().apply {
            put(ContactsContract.CommonDataKinds.Event.START_DATE, isoDate)
            put(
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
            )
        }

        val where =
            "${ContactsContract.Data.CONTACT_ID}=? AND " +
                    "${ContactsContract.Data.MIMETYPE}=? AND " +
                    "${ContactsContract.CommonDataKinds.Event.TYPE}=?"

        val args = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        val updated = resolver.update(
            ContactsContract.Data.CONTENT_URI,
            values,
            where,
            args
        )

        // If no existing birthday row, insert a new one
        if (updated == 0) {
            val insertValues = ContentValues().apply {
                put(ContactsContract.Data.CONTACT_ID, contactId)
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                )
                put(
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                )
                put(ContactsContract.CommonDataKinds.Event.START_DATE, isoDate)
            }

            resolver.insert(
                ContactsContract.Data.CONTENT_URI,
                insertValues
            )
        }
    }

    /* =========================================================
       DATE NORMALISATION
       ========================================================= */

    private fun normalizeBirthday(raw: String): String? {
        return try {
            when {
                raw.length == 10 && raw.contains("-") -> {
                    // yyyy-MM-dd
                    val date = LocalDate.parse(raw)
                    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }
                raw.length == 5 && raw.contains("-") -> {
                    // MM-dd (no year)
                    val parts = raw.split("-")
                    val month = parts[0].padStart(2, '0')
                    val day = parts[1].padStart(2, '0')
                    "$day/$month/1900"
                }
                raw.length == 10 && raw.contains("/") -> raw
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
