package com.chris.birthdaytracker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    showSearch: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    var searchText by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<ContactModel?>(null) }
    var duplicatesToResolve by remember { mutableStateOf<Pair<String, List<ContactModel>>?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val preferredSources by SettingsStore
        .preferredSources(context)
        .collectAsState(initial = emptyMap())

    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    val sortBySurname by SettingsStore
        .sortBySurname(context)
        .collectAsState(initial = false)

    val repository = remember { ContactsRepository(context) }

    // Helper to get display name based on setting
    fun getDisplayName(name: String): String {
        if (!sortBySurname) return name
        val parts = name.trim().split(" ")
        if (parts.size < 2) return name
        return "${parts.last()}, ${parts.dropLast(1).joinToString(" ")}"
    }

    // Optimized processing: Only re-calculate when contacts or preferences change
    val processedContacts = remember(contacts, preferredSources, sortBySurname) {
        val groups = contacts.groupBy { 
            "${it.name.lowercase().trim()}_${it.birthday?.monthValue}_${it.birthday?.dayOfMonth}" 
        }
        
        groups.map { (key, group) ->
            val preferredId = preferredSources[key]
            val primary = if (preferredId != null) {
                group.find { it.id == preferredId } ?: group.first()
            } else {
                group.sortedBy { 
                    when(it.source) {
                        ContactSource.LOCAL -> 0
                        ContactSource.PHONE -> 1
                        ContactSource.CALENDAR -> 2
                    }
                }.first()
            }
            Triple(key, primary, group)
        }.sortedWith(compareBy<Triple<String, ContactModel, List<ContactModel>>> { (_, contact, _) ->
            val next = contact.birthday!!
                .withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }
            ChronoUnit.DAYS.between(today, next)
        }.thenBy { (_, contact, _) ->
            if (sortBySurname) {
                contact.name.split(" ").last().lowercase()
            } else {
                contact.name.lowercase()
            }
        })
    }

    val filteredContacts = remember(searchText, processedContacts, showOnlyFavorites) {
        processedContacts.filter { (_, contact, _) -> 
            val matchesSearch = searchText.isBlank() || contact.name.contains(searchText, ignoreCase = true)
            val matchesFavorite = !showOnlyFavorites || contact.isFavorite
            matchesSearch && matchesFavorite
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (processedContacts.any { (_, contact, _) -> 
            contact.birthday?.month == today.month && contact.birthday?.dayOfMonth == today.dayOfMonth
        } && confettiEnabled) {
            BirthdayCelebrationOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search...") },
                        modifier = Modifier.weight(1f),
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
                    
                    Spacer(Modifier.width(8.dp))
                    
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { showOnlyFavorites = !showOnlyFavorites },
                        label = { Text("Favorites") },
                        leadingIcon = {
                            Icon(
                                if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            if (isRefreshing && contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredContacts.isEmpty()) {
                val message = if (showOnlyFavorites) "No favorite birthdays found." else "No birthdays found. Add some to get started!"
                BirthdayEmptyState(message = message)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val monthsInOrder = filteredContacts.map { (_, contact, _) ->
                        val next = contact.birthday!!
                            .withYear(today.year)
                            .let { if (it.isBefore(today)) it.plusYears(1) else it }
                        next.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()
                    }.distinct()

                    monthsInOrder.forEach { month ->
                        stickyHeader {
                            MonthHeader(month)
                        }

                        items(
                            items = filteredContacts.filter { (_, contact, _) ->
                                val next = contact.birthday!!
                                    .withYear(today.year)
                                    .let { if (it.isBefore(today)) it.plusYears(1) else it }
                                next.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase() == month
                            },
                            key = { it.second.id }
                        ) { (key, contact, allVersions) ->
                            Box(
                                modifier = Modifier.clickable { 
                                    if (allVersions.size > 1 && key !in preferredSources) {
                                        duplicatesToResolve = key to allVersions
                                    } else {
                                        selectedContact = contact 
                                    }
                                }
                            ) {
                                val displayContact = if (sortBySurname) {
                                    contact.copy(name = getDisplayName(contact.name))
                                } else contact

                                UpcomingBirthdayCard(
                                    contact = displayContact,
                                    onToggleFavorite = { isFav ->
                                        scope.launch {
                                            repository.updateFavorite(contact, isFav)
                                            onRefresh()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedContact != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedContact = null },
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { false } // DISABLE SWIPE TO CLOSE
                )
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
                title = { Text("Choose Primary Source") },
                text = { Text("We found this birthday in multiple places. Which one would you like to keep as the primary source?") },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        duplicatesToResolve!!.second.forEach { duplicate ->
                            Button(
                                onClick = {
                                    scope.launch {
                                        SettingsStore.setPreferredSource(context, duplicatesToResolve!!.first, duplicate.id)
                                        selectedContact = duplicate
                                        duplicatesToResolve = null
                                    }
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
                                Text("${duplicate.source.name}: ${duplicate.accountName ?: "Device"}")
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

@Composable
fun MonthHeader(month: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = month,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
        )
    }
}
