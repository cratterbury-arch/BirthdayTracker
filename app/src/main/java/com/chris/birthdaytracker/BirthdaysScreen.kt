package com.chris.birthdaytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var duplicatesToResolve by remember { mutableStateOf<List<ContactModel>?>(null) }

    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    // Deduplicate logic for the main list - keep only one per person
    // But track if there ARE duplicates so we can show a badge
    val processedContacts = remember(contacts) {
        val groups = contacts.groupBy { 
            "${it.name.lowercase().trim()}_${it.birthday?.monthValue}_${it.birthday?.dayOfMonth}" 
        }
        
        groups.values.map { group ->
            // Priority: LOCAL > PHONE > CALENDAR
            val primary = group.sortedBy { 
                when(it.source) {
                    ContactSource.LOCAL -> 0
                    ContactSource.PHONE -> 1
                    ContactSource.CALENDAR -> 2
                }
            }.first()
            
            // Attached info about duplicates
            primary to group
        }.sortedBy { (contact, _) ->
            val next = contact.birthday!!
                .withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }
            ChronoUnit.DAYS.between(today, next)
        }
    }

    val birthdaysToday = processedContacts.filter { (it, _) ->
        it.birthday?.month == today.month &&
                it.birthday?.dayOfMonth == today.dayOfMonth
    }

    val filteredContacts = if (showSearch && searchText.isNotEmpty()) {
        processedContacts.filter { (it, _) -> it.name.contains(searchText, ignoreCase = true) }
    } else {
        processedContacts
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
                            key = { _, (contact, _) -> contact.id }
                        ) { index, (contact, allVersions) ->
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
                                Box(modifier = Modifier.clickable { 
                                    if (allVersions.size > 1) {
                                        duplicatesToResolve = allVersions
                                    } else {
                                        selectedContact = contact 
                                    }
                                }) {
                                    Box {
                                        UpcomingBirthdayCard(contact = contact)
                                        
                                        if (allVersions.size > 1) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .padding(top = 16.dp, end = 24.dp)
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = "Duplicate",
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                    modifier = Modifier.padding(4.dp)
                                                )
                                            }
                                        }
                                    }
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

        if (duplicatesToResolve != null) {
            AlertDialog(
                onDismissRequest = { duplicatesToResolve = null },
                title = { Text("Multiple Sources Found") },
                text = { Text("We found this birthday in multiple places. Which version would you like to view/edit?") },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        duplicatesToResolve!!.forEach { duplicate ->
                            Button(
                                onClick = {
                                    selectedContact = duplicate
                                    duplicatesToResolve = null
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when(duplicate.source) {
                                        ContactSource.LOCAL -> MaterialTheme.colorScheme.primary
                                        ContactSource.PHONE -> MaterialTheme.colorScheme.secondary
                                        ContactSource.CALENDAR -> MaterialTheme.colorScheme.tertiary
                                    }
                                )
                            ) {
                                Text("${duplicate.source.name}: ${duplicate.name}")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { duplicatesToResolve = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
