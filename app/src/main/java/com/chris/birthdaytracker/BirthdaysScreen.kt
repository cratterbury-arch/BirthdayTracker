@file:OptIn(ExperimentalMaterial3Api::class)

package com.chris.birthdaytracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
                query.isBlank() ||
                        it.displayName.contains(query, ignoreCase = true)
            }
            .filter {
                when (filter) {
                    BirthdayFilter.ALL -> true
                    BirthdayFilter.TODAY -> it.isBirthdayToday(today)
                    BirthdayFilter.WEEK -> (it.daysUntilBirthday(today) ?: 99) <= 7
                    BirthdayFilter.MONTH -> (it.daysUntilBirthday(today) ?: 999) <= 31
                }
            }
    }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                if (query.isNotBlank()) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                query = ""
                                focusManager.clearFocus()
                            }
                    )
                }
            }
        )

        /* 🧭 Filter Chips */
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BirthdayFilter.values().forEach { chip ->
                AnimatedFilterChip(
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

        Spacer(Modifier.height(12.dp))

        /* 📭 Empty State */
        AnimatedVisibility(
            visible = filteredContacts.isEmpty(),
            enter = fadeIn(tween(200)) + slideInVertically { it / 4 },
            exit = fadeOut(tween(150))
        ) {
            BirthdayEmptyState(
                message = when (filter) {
                    BirthdayFilter.TODAY -> "No birthdays today 🎈"
                    BirthdayFilter.WEEK -> "No birthdays this week 🎈"
                    BirthdayFilter.MONTH -> "No birthdays this month 🎈"
                    BirthdayFilter.ALL -> "No matching birthdays 🎈"
                }
            )
        }

        /* 🎂 List */
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredContacts, key = { it.id }) { contact ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(250)) +
                            slideInVertically { it / 6 }
                ) {
                    UpcomingBirthdayCard(contact) {
                        onSelect(contact)
                    }
                }
            }
        }
    }
}

/* 🎨 Animated Chip */
@Composable
private fun AnimatedFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(200),
        label = "chip-scale"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}
