package com.chris.birthdaytracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.LocalDate

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = contact.isBirthdayToday(today)
    val days = contact.daysUntilBirthday(today)
    val age = contact.ageOnDate(
        if (isToday) today else contact.nextBirthday(today) ?: today
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor =
                if (isToday)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            contact.photoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )

                contact.birthday?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (days != null && age != null) {
                    Text(
                        text =
                            if (isToday) "🎉 Today! Turning $age"
                            else "In $days days · Turning $age",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
