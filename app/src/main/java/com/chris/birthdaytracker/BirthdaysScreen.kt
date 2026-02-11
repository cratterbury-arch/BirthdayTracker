package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var searchText by remember { mutableStateOf("") }

    // 🎛 Confetti toggle (SAFE, PUBLIC API)
    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    val birthdaysToday = contacts.filter {
        it.birthday?.month == today.month &&
                it.birthday?.dayOfMonth == today.dayOfMonth
    }

    // 🔊 Sound once per screen open
    LaunchedEffect(birthdaysToday.isNotEmpty()) {
        if (birthdaysToday.isNotEmpty()) {
            playBirthdaySound(context)
        }
    }

    val sortedContacts = contacts
        .filter { it.birthday != null }
        .sortedBy { contact ->
            val next = contact.birthday!!
                .withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }
            ChronoUnit.DAYS.between(today, next)
        }

    val filteredContacts = sortedContacts.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🎉 CONFETTI — IN FRONT
        if (birthdaysToday.isNotEmpty() && confettiEnabled) {
            BirthdayCelebrationOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search by name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No upcoming birthdays")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filteredContacts) { contact ->
                        UpcomingBirthdayCard(contact = contact)
                    }
                }
            }
        }
    }
}