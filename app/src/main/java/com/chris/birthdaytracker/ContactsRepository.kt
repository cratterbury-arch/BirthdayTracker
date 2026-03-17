package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ContactsRepository(private val context: Context) {

    private val dateFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE, // yyyy-MM-dd
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("--MM-dd") // Common for phone contacts without year
    )

    suspend fun getAllContacts(): List<ContactModel> {
        val phoneContacts = getPhoneContacts()
        val localContacts = getLocalContacts()
        
        val allMapped = (phoneContacts + localContacts).associateBy { it.id }
        return allMapped.values.toList()
    }

    private fun getPhoneContacts(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

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

        val cursor = try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
        } catch (e: Exception) {
            null
        }

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
            val photoIndex = it.getColumnIndexOrThrow(ContactsContract.Data.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex).toString()
                val name = it.getString(nameIndex) ?: continue
                val dateString = it.getString(dateIndex)
                val photoUri = it.getString(photoIndex)?.let { Uri.parse(it) }

                val birthday = parseDate(dateString)

                contacts.add(
                    ContactModel(
                        id = id,
                        name = name,
                        birthday = birthday,
                        photoUri = photoUri,
                        isFromPhone = true
                    )
                )
            }
        }

        return contacts
    }

    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString == null) return null
        
        for (format in dateFormats) {
            try {
                if (format.toString().contains("--")) {
                    // Handle --MM-dd by adding a dummy year
                    val parts = dateString.split("-").filter { it.isNotEmpty() }
                    if (parts.size == 2) {
                        return LocalDate.of(1900, parts[0].toInt(), parts[1].toInt())
                    }
                }
                return LocalDate.parse(dateString, format)
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }

    suspend fun getLocalContacts(): List<ContactModel> {
        val db = BirthdayApplication.getDatabase(context)
        return db.contactDao().getAll().map { entity ->
            ContactModel(
                id = entity.id,
                name = entity.name,
                birthday = entity.birthday,
                photoUri = entity.photoUri?.let { Uri.parse(it) },
                isFromPhone = false
            )
        }
    }
}
