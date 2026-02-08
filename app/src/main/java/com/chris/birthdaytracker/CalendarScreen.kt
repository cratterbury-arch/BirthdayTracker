package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(contacts) { contact ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = contact.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = contact.birthday ?: "No birthday",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (BirthdayRepository.isBirthdayToday(contact.birthday)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🎂 Birthday today!", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
