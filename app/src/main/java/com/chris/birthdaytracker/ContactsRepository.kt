package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ContactsRepository(private val context: Context) {

    private val outputFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun getContacts(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        )

        // ❗ NOTE: NO phone-number filter anymore
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex =
                cursor.getColumnIndexOrThrow(
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                )
            val photoIndex =
                cursor.getColumnIndexOrThrow(
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                )

            while (cursor.moveToNext()) {
                val contactId = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                val photoUriString = cursor.getString(photoIndex)

                val birthday = getBirthday(contactId)

                // Optional: only include contacts that actually have a birthday
                if (birthday == null) continue

                contacts.add(
                    ContactModel(
                        id = contactId,
                        displayName = name,
                        birthday = birthday,
                        photoUri = photoUriString?.let { Uri.parse(it) }
                    )
                )
            }
        }

        return contacts
    }

    private fun getBirthday(contactId: Long): String? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Event.START_DATE
        )

        val selection =
            "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ? AND " +
                    "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"

        val selectionArgs = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val rawDate = cursor.getString(0) ?: return null
                return normalizeDate(rawDate)
            }
        }

        return null
    }

    private fun normalizeDate(raw: String): String? {
        return try {
            when {
                raw.length == 10 -> {
                    // yyyy-MM-dd
                    val date = LocalDate.parse(raw)
                    outputFormatter.format(date)
                }

                raw.length == 5 -> {
                    // MM-dd (no year)
                    val date = LocalDate.parse("2000-$raw")
                    outputFormatter.format(date)
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
