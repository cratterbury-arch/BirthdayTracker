package com.chris.birthdaytracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Birthday: ${contact.birthday?.toString() ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
