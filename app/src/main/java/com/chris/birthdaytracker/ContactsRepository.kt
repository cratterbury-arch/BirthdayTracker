package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.time.LocalDate

class ContactsRepository(private val context: Context) {

    fun getAllContacts(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.Data.PHOTO_URI
        )

        val selection =
            "${ContactsContract.Data.MIMETYPE} = ? AND " +
                    "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"

        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
            val photoIndex = it.getColumnIndexOrThrow(ContactsContract.Data.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: continue
                val dateString = it.getString(dateIndex)
                val photoUri = it.getString(photoIndex)?.let { Uri.parse(it) }

                val birthday = try {
                    dateString?.let { LocalDate.parse(it) }
                } catch (e: Exception) {
                    null
                }

                contacts.add(
                    ContactModel(
                        id = id,
                        name = name,
                        birthday = birthday,
                        photoUri = photoUri
                    )
                )
            }
        }

        return contacts
    }
}
