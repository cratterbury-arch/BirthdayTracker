package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BirthdaysScreen(contacts: List<ContactModel>) {

    val upcoming = contacts
        .filter { it.birthday != null }
        .map { contact ->
            val birthday = contact.birthday!!
            Triple(
                contact,
                daysUntilBirthday(birthday),
                ageOnNextBirthday(birthday)
            )
        }
        .sortedBy { it.second }

    if (upcoming.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No upcoming birthdays")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upcoming) { (contact, days, age) ->
            UpcomingBirthdayCard(
                contact = contact,
                daysUntil = days,
                age = age
            )
        }
    }
}
