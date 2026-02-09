package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun BirthdayPopupCard(
    contact: ContactModel,
    date: LocalDate
) {
    val birthday = contact.birthday ?: return
    val age = date.year - birthday.year

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(contact.name, fontWeight = FontWeight.Bold)
            Text("Turning $age 🎉")
            Text("Born ${birthday.dayOfMonth}/${birthday.monthValue}/${birthday.year}")
        }
    }
}
