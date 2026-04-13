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
        val enabledCalendarAccounts = SettingsStore.enabledCalendarAccounts(context).first()
        val disabledPhoneAccounts = SettingsStore.disabledPhoneAccounts(context).first()
        
        // 1. Local App entries (always shown)
        val localContacts = getLocalContacts()
        
        // 2. Phone contacts (shown unless explicitly disabled)
        val phoneContacts = getPhoneContacts().filter { 
            it.accountName == null || it.accountName !in disabledPhoneAccounts
        }
        
        // 3. Calendar birthdays (shown only if explicitly enabled)
        val calendarContacts = getCalendarBirthdays().filter { 
            it.accountName in enabledCalendarAccounts 
        }
        
        // 4. Combine and Deduplicate
        val allRaw = localContacts + phoneContacts + calendarContacts
        val uniqueContacts = mutableListOf<ContactModel>()
        val seenKeys = mutableSetOf<String>()

        for (contact in allRaw) {
            val birthday = contact.birthday ?: continue
            val normalizedName = contact.name.lowercase().trim()
            val key = "${normalizedName}_${birthday.monthValue}_${birthday.dayOfMonth}"
            
            if (!seenKeys.contains(key)) {
                uniqueContacts.add(contact)
                seenKeys.add(key)
            }
        }
        
        return uniqueContacts
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

        val holidayCalendarIds = mutableSetOf<Long>()
        val calCursor = try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
                null, null, null
            )
        } catch (e: Exception) { null }

        calCursor?.use {
            val idIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: ""
                if (name.contains("Holidays", ignoreCase = true)) {
                    holidayCalendarIds.add(it.getLong(idIdx))
                }
            }
        }

        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Events.CALENDAR_ID
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
            val calIdIndex = it.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID)

            while (it.moveToNext()) {
                val calId = it.getLong(calIdIndex)
                if (calId in holidayCalendarIds) continue 

                val title = it.getString(titleIndex) ?: continue
                if (title.contains("King's Birthday", ignoreCase = true)) continue
                if (title.contains("Queen's Birthday", ignoreCase = true)) continue
                if (title.contains("Bank Holiday", ignoreCase = true)) continue
                
                val lowercaseTitle = title.lowercase()
                if (lowercaseTitle == "my birthday" || lowercaseTitle == "birthday" || lowercaseTitle == "happy birthday" || lowercaseTitle == "happy birthday!") continue

                val id = "cal_" + it.getLong(idIndex).toString()
                val dtStart = it.getLong(dateIndex)
                val accountName = it.getString(ownerIndex)
                
                val extractedName = title.replace("'s Birthday", "", ignoreCase = true).replace("Birthday", "", ignoreCase = true).trim()
                if (extractedName.isEmpty() || extractedName.lowercase() == "my") continue

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dtStart
                val birthday = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

                contacts.add(
                    ContactModel(
                        id = id,
                        name = extractedName,
                        birthday = birthday,
                        photoUri = null,
                        source = ContactSource.CALENDAR,
                        accountName = accountName,
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
