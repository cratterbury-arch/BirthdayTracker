package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM")

    val birthdaysByDate = contacts
        .filter { it.birthday != null }
        .groupBy { it.birthday!!.withYear(LocalDate.now().year) }
        .toSortedMap()

    if (birthdaysByDate.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("No birthdays found")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        birthdaysByDate.forEach { (date, contactsOnDate) ->
            item {
                Text(
                    text = date.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(contactsOnDate) { contact ->
                BirthdayCalendarCard(contact)
            }
        }
    }
}

@Composable
private fun BirthdayCalendarCard(contact: ContactModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = contact.birthday
                    ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    ?: "Birthday unknown",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
