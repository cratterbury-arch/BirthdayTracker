package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>
) {
    val today = LocalDate.now()

    val sortedContacts = contacts
        .filter { it.birthday != null }
        .sortedBy { birthday ->
            val next = birthday.birthday!!.withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }
            ChronoUnit.DAYS.between(today, next)
        }

    if (sortedContacts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No upcoming birthdays")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(sortedContacts) { contact ->
            UpcomingBirthdayCard(contact = contact)
        }
    }
}
