package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun BirthdayPopupContent(
    contacts: List<ContactModel>,
    date: LocalDate,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "🎂 Birthdays",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(contacts) { contact ->
                    BirthdayPopupCard(contact, date)
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}
