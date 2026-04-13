package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ContactsRepository(private val context: Context) {

    private val dateFormats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("--MM-dd")
    )

    suspend fun getAllContacts(): List<ContactModel> {
        val disabledAccounts = SettingsStore.disabledAccounts(context).first()
        
        val phoneContacts = getPhoneContacts().filter { it.accountName !in disabledAccounts }
        val calendarContacts = getCalendarBirthdays().filter { it.accountName !in disabledAccounts }
        val localContacts = getLocalContacts()
        
        return localContacts + phoneContacts + calendarContacts
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
            ContactsContract.Data.PHOTO_URI,
            ContactsContract.RawContacts.ACCOUNT_NAME
        )

        val selection =
            "${ContactsContract.Data.MIMETYPE} = ? AND " +
                    "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"

        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        val cursor = try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        } catch (e: Exception) {
            null
        }

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val dateIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
            val photoIndex = it.getColumnIndexOrThrow(ContactsContract.Data.PHOTO_URI)
            val accountIndex = it.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex).toString()
                val name = it.getString(nameIndex) ?: continue
                val dateString = it.getString(dateIndex)
                val photoUri = it.getString(photoIndex)?.let { Uri.parse(it) }
                val accountName = it.getString(accountIndex)

                contacts.add(
                    ContactModel(
                        id = id,
                        name = name,
                        birthday = parseDate(dateString),
                        photoUri = photoUri,
                        source = ContactSource.PHONE,
                        accountName = accountName,
                        isFromPhone = true
                    )
                )
            }
        }
        return contacts
    }

    private fun getCalendarBirthdays(): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        if (context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )

        val selection = "${CalendarContract.Events.TITLE} LIKE ?"
        val selectionArgs = arrayOf("%Birthday%")

        val cursor = try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        } catch (e: Exception) {
            null
        }

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val dateIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val ownerIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT)

            while (it.moveToNext()) {
                val id = "cal_" + it.getLong(idIndex).toString()
                val title = it.getString(titleIndex) ?: continue
                val dtStart = it.getLong(dateIndex)
                val accountName = it.getString(ownerIndex)
                
                val name = title.replace("'s Birthday", "", ignoreCase = true)
                                .replace("Birthday", "", ignoreCase = true)
                                .trim()

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dtStart
                val birthday = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                contacts.add(
                    ContactModel(
                        id = id,
                        name = if (name.isNotEmpty()) name else title,
                        birthday = birthday,
                        photoUri = null,
                        source = ContactSource.CALENDAR,
                        accountName = accountName,
                        isFromPhone = false
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
                    val parts = dateString.split("-").filter { it.isNotEmpty() }
                    if (parts.size == 2) return LocalDate.of(1900, parts[0].toInt(), parts[1].toInt())
                }
                return LocalDate.parse(dateString, format)
            } catch (e: Exception) {}
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
                source = ContactSource.LOCAL,
                accountName = "Local App",
                isFromPhone = false
            )
        }
    }
}
