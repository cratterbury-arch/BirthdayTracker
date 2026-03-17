package com.chris.birthdaytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    showSearch: Boolean = true
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var searchText by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<ContactModel?>(null) }

    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    val birthdaysToday = contacts.filter {
        it.birthday?.month == today.month &&
                it.birthday?.dayOfMonth == today.dayOfMonth
    }

    val sortedContacts = remember(contacts) {
        contacts
            .filter { it.birthday != null }
            .sortedBy { contact ->
                val next = contact.birthday!!
                    .withYear(today.year)
                    .let { if (it.isBefore(today)) it.plusYears(1) else it }
                ChronoUnit.DAYS.between(today, next)
            }
    }

    val filteredContacts = if (showSearch && searchText.isNotEmpty()) {
        sortedContacts.filter { it.name.contains(searchText, ignoreCase = true) }
    } else {
        sortedContacts
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (birthdaysToday.isNotEmpty() && confettiEnabled) {
            BirthdayCelebrationOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (showSearch) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search birthdays...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                )
            }

            if (isRefreshing && contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (contacts.isEmpty()) {
                BirthdayEmptyState(message = "No birthdays found. Add some to get started!")
            } else {
                AnimatedContent(
                    targetState = filteredContacts,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith
                                fadeOut(animationSpec = tween(400))
                    },
                    label = "ListAnimation"
                ) { targetContacts ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        itemsIndexed(
                            items = targetContacts,
                            key = { _, contact -> contact.id }
                        ) { index, contact ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(
                                    initialOffsetY = { 40 * (index + 1) },
                                    animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(500)),
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                Box(modifier = Modifier.clickable { selectedContact = contact }) {
                                    UpcomingBirthdayCard(contact = contact)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedContact != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedContact = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                EditContactScreen(
                    contact = selectedContact,
                    onDone = {
                        selectedContact = null
                        onRefresh()
                    }
                )
            }
        }
    }
}
