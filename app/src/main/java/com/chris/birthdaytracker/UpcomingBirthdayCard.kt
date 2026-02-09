package com.chris.birthdaytracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel
) {
    val today = LocalDate.now()

    val birthdayText = contact.birthday
        ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        ?: "Birthday unknown"

    val daysUntilBirthday = contact.birthday?.let { birthday ->
        val nextBirthday = birthday.withYear(today.year)
            .let { if (it.isBefore(today)) it.plusYears(1) else it }
        ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    }

    val daysText = daysUntilBirthday?.let {
        if (it == 0) "Today 🎉" else "$it days"
    } ?: "—"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ✅ CONTACT PHOTO (or fallback)
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = birthdayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = daysText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
