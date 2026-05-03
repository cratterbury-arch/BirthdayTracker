package com.chris.birthdaytracker

import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

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
            .map { it.lowercase().trim() }.toSet()
        val disabledPhoneAccounts = SettingsStore.disabledPhoneAccounts(context).first()
            .map { it.lowercase().trim() }.toSet()
        
        val metadataMap = getMetadataMap()
        
        // 1. Local App entries
        val localContacts = getLocalContacts()
        
        // 2. Phone contacts
        val phoneContacts = getPhoneContacts().filter { 
            val acc = it.accountName?.lowercase()?.trim()
            acc == null || acc !in disabledPhoneAccounts
        }.map { mergeMetadata(it, metadataMap) }
        
        // 3. Calendar birthdays
        val calendarContacts = getCalendarBirthdays().filter { 
            val acc = it.accountName?.lowercase()?.trim()
            acc != null && (
                acc in enabledCalendarAccounts ||
                (acc.endsWith("@googlemail.com") && acc.replace("@googlemail.com", "@gmail.com") in enabledCalendarAccounts) ||
                (acc.endsWith("@gmail.com") && acc.replace("@gmail.com", "@googlemail.com") in enabledCalendarAccounts)
            )
        }.map { mergeMetadata(it, metadataMap) }
        
        return localContacts + phoneContacts + calendarContacts
    }

    private fun mergeMetadata(contact: ContactModel, metadataMap: Map<String, ContactMetadataEntity>): ContactModel {
        val key = "${contact.name.lowercase().trim()}_${contact.birthday?.monthValue}_${contact.birthday?.dayOfMonth}"
        val metadata = metadataMap[key] ?: return contact
        return contact.copy(
            isFavorite = metadata.isFavorite,
            tags = if (metadata.tags.isBlank()) emptyList() else metadata.tags.split(",")
        )
    }

    private suspend fun getMetadataMap(): Map<String, ContactMetadataEntity> {
        val db = BirthdayApplication.getDatabase(context)
        return db.metadataDao().getAllMetadata().associateBy { it.contactKey }
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

                val birthday = parseDate(dateString)
                if (birthday != null) {
                    contacts.add(
                        ContactModel(
                            id = id,
                            name = name,
                            birthday = birthday,
                            photoUri = photoUri,
                            source = ContactSource.PHONE,
                            accountName = accountName,
                            isFromPhone = true,
                            isFavorite = false,
                            tags = emptyList()
                        )
                    )
                }
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

        val birthdayCalendarIds = mutableSetOf<Long>()
        val holidayCalendarIds = mutableSetOf<Long>()

        val calCursor = try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.NAME),
                null, null, null
            )
        } catch (e: Exception) { null }

        calCursor?.use {
            val idIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val displayNameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val nameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.NAME)
            while (it.moveToNext()) {
                val displayName = it.getString(displayNameIdx) ?: ""
                val name = it.getString(nameIdx) ?: ""
                val id = it.getLong(idIdx)
                
                if (displayName.contains("Holidays", ignoreCase = true)) {
                    holidayCalendarIds.add(id)
                } else if (displayName.contains("Birthdays", ignoreCase = true) || 
                           name.contains("Birthdays", ignoreCase = true) ||
                           name.lowercase() == "contacts") {
                    birthdayCalendarIds.add(id)
                }
            }
        }

        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.ALL_DAY
        )

        val selection = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        if (birthdayCalendarIds.isNotEmpty()) {
            selection.append("${CalendarContract.Events.CALENDAR_ID} IN (${birthdayCalendarIds.joinToString(",")})")
        }

        val titlePatterns = listOf("%Birthday%", "%B-day%", "%Anniversaire%", "%Geburtstag%")
        for (pattern in titlePatterns) {
            if (selection.isNotEmpty()) selection.append(" OR ")
            selection.append("${CalendarContract.Events.TITLE} LIKE ?")
            selectionArgs.add(pattern)
        }

        val cursor = try {
            context.contentResolver.query(uri, projection, selection.toString(), selectionArgs.toTypedArray(), null)
        } catch (e: Exception) {
            null
        }

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val dateIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val accountIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
            val calIdIndex = it.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID)
            val allDayIndex = it.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)

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
                val accountName = it.getString(accountIndex)
                val allDay = it.getInt(allDayIndex) == 1
                
                val extractedName = title.replace("'s Birthday", "", ignoreCase = true)
                    .replace("Birthday", "", ignoreCase = true)
                    .replace("B-day", "", ignoreCase = true)
                    .replace("Geburtstag", "", ignoreCase = true)
                    .replace("Anniversaire", "", ignoreCase = true)
                    .trim()

                if (extractedName.isEmpty() || extractedName.lowercase() == "my") continue

                val calendar = if (allDay) {
                    Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                } else {
                    Calendar.getInstance()
                }
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
                        isFromPhone = true,
                        isFavorite = false,
                        tags = emptyList()
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
                isFromPhone = false,
                isFavorite = entity.isFavorite,
                tags = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",")
            )
        }
    }

    suspend fun updateFavorite(contact: ContactModel, isFavorite: Boolean) {
        val db = BirthdayApplication.getDatabase(context)
        if (contact.source == ContactSource.LOCAL) {
            db.contactDao().updateFavorite(contact.id, isFavorite)
        } else {
            val key = "${contact.name.lowercase().trim()}_${contact.birthday?.monthValue}_${contact.birthday?.dayOfMonth}"
            val existing = db.metadataDao().getMetadata(key)
            db.metadataDao().insert(ContactMetadataEntity(
                contactKey = key,
                tags = existing?.tags ?: "",
                isFavorite = isFavorite
            ))
        }
    }
}
