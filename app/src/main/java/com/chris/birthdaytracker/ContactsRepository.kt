package com.chris.birthdaytracker

import android.content.Context
import android.provider.ContactsContract

class ContactsRepository(
    private val context: Context
) {

    fun getAllContacts(): List<ContactModel> {
        val results = mutableListOf<ContactModel>()

        val resolver = context.contentResolver

        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        ) ?: return emptyList()

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex =
                it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            val photoIndex =
                it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)              // ✅ FIXED
                val displayName = it.getString(nameIndex)
                val photoUri = it.getString(photoIndex)

                val birthday = getBirthdayForContact(id)

                results.add(
                    ContactModel(
                        id = id,
                        displayName = displayName,
                        birthday = birthday,
                        photoUri = photoUri
                    )
                )
            }
        }

        return results
    }

    private fun getBirthdayForContact(contactId: Long): String? {
        val resolver = context.contentResolver

        val cursor = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE),
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
            ),
            null
        ) ?: return null

        cursor.use {
            if (it.moveToFirst()) {
                return it.getString(0)
            }
        }

        return null
    }
}
