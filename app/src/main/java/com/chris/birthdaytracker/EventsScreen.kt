package com.chris.birthdaytracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import android.app.DatePickerDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { BirthdayApplication.getDatabase(context) }
    
    var events by remember { mutableStateOf(emptyList<EventModel>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    fun refresh() {
        scope.launch {
            events = db.eventDao().getAll().map { entity ->
                EventModel(
                    id = entity.id,
                    title = entity.title,
                    date = entity.date,
                    type = entity.type,
                    tags = if (entity.tags.isBlank()) emptyList() else entity.tags.split(","),
                    isFavorite = entity.isFavorite
                )
            }.sortedBy { it.date.withYear(LocalDate.now().year) }
        }
    }
    
    LaunchedEffect(Unit) { refresh() }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No special events yet. Add an anniversary, pet birthday, or more!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onToggleFavorite = { isFav ->
                            scope.launch {
                                db.eventDao().updateFavorite(event.id, isFav)
                                refresh()
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddEventDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, date, type ->
                    scope.launch {
                        db.eventDao().insert(EventEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            date = date,
                            type = type
                        ))
                        showAddDialog = false
                        refresh()
                    }
                }
            )
        }
    }
}

@Composable
fun AddEventDialog(onDismiss: () -> Unit, onConfirm: (String, LocalDate, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Anniversary") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current
    
    val types = listOf("Anniversary", "Pet Birthday", "Christening", "Show", "Other")
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Special Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Event Type", style = MaterialTheme.typography.labelMedium)
                ScrollableTabRow(selectedTabIndex = types.indexOf(type), edgePadding = 0.dp) {
                    types.forEach { t ->
                        Tab(selected = type == t, onClick = { type = t }, text = { Text(t) })
                    }
                }
                
                OutlinedTextField(
                    value = date.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, date, type) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
