package com.chris.birthdaytracker

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract

object ContactEditor {

    fun updateBirthday(
        context: Context,
        contactId: Long,
        birthdayDigits: String // ddMMyyyy
    ) {
        if (birthdayDigits.length != 8) return

        val formatted =
            "${birthdayDigits.substring(4, 8)}-" +
                    "${birthdayDigits.substring(2, 4)}-" +
                    birthdayDigits.substring(0, 2)

        val ops = ArrayList<ContentProviderOperation>()

        // Delete existing birthday (if any)
        ops.add(
            ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.CONTACT_ID}=? AND " +
                            "${ContactsContract.Data.MIMETYPE}=? AND " +
                            "${ContactsContract.CommonDataKinds.Event.TYPE}=?",
                    arrayOf(
                        contactId.toString(),
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
                    )
                )
                .build()
        )

        // Insert new birthday
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    getRawContactId(context, contactId)
                )
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    formatted
                )
                .build()
        )

        context.contentResolver.applyBatch(
            ContactsContract.AUTHORITY,
            ops
        )
    }

    private fun getRawContactId(
        context: Context,
        contactId: Long
    ): Long {
        val cursor = context.contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.CONTACT_ID}=?",
            arrayOf(contactId.toString()),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }

        throw IllegalStateException("RawContact not found")
    }
}
