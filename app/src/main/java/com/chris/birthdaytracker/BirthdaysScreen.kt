package com.chris.birthdaytracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import java.time.LocalDate

enum class BirthdayFilter {
    ALL, TODAY, WEEK, MONTH
}

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    selected: ContactModel?,
    onSelect: (ContactModel) -> Unit,
    onDoneEditing: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(BirthdayFilter.ALL) }
    val focusManager = LocalFocusManager.current
    val today = LocalDate.now()

    val filteredContacts = remember(contacts, query, filter) {
        contacts
            .filter {
                query.isBlank() || it.displayName.contains(query, ignoreCase = true)
            }
            .filter {
                when (filter) {
                    BirthdayFilter.ALL -> true
                    BirthdayFilter.TODAY -> it.isBirthdayToday(today)
                    BirthdayFilter.WEEK -> (it.daysUntilBirthday(today) ?: 999) <= 7
                    BirthdayFilter.MONTH -> (it.daysUntilBirthday(today) ?: 999) <= 31
                }
            }
    }

    // ✏️ Edit screen
    if (selected != null) {
        EditContactScreen(
            contact = selected,
            onDone = onDoneEditing
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {

        /* 🔍 Sticky Search */
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search birthdays") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotBlank()) {
                    Text(
                        "✕",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                query = ""
                                focusManager.clearFocus()
                            }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        /* 🧭 Filter Chips (STABLE) */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BirthdayFilter.values().forEach { chip ->
                AnimatedAssistChip(
                    selected = filter == chip,
                    label = when (chip) {
                        BirthdayFilter.ALL -> "All"
                        BirthdayFilter.TODAY -> "Today"
                        BirthdayFilter.WEEK -> "Week"
                        BirthdayFilter.MONTH -> "Month"
                    },
                    onClick = { filter = chip }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* 📭 Empty State */
        AnimatedVisibility(
            visible = filteredContacts.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyBirthdaysState(
                when (filter) {
                    BirthdayFilter.TODAY -> "No birthdays today 🎈"
                    BirthdayFilter.WEEK -> "No birthdays this week 🎈"
                    BirthdayFilter.MONTH -> "No birthdays this month 🎈"
                    BirthdayFilter.ALL -> "No matching birthdays 🎈"
                }
            )
        }

        /* 📋 List */
        if (filteredContacts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredContacts) { contact ->
                    UpcomingBirthdayCard(contact) {
                        onSelect(contact)
                    }
                }
            }
        }
    }
}

/* 🎨 Animated STABLE Chip */
@Composable
private fun AnimatedAssistChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        label = "chip-scale"
    )

    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

/* 📭 Empty State */
@Composable
private fun EmptyBirthdaysState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
