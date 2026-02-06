package com.chris.birthdaytracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.chris.birthdaytracker.ui.theme.BirthdayTrackerTheme
import java.time.LocalDate

enum class BottomTab {
    Birthdays,
    Calendar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BirthdayTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactsPermissionGate {
                        AppRoot()
                    }
                }
            }
        }
    }
}

/* =========================================================
   🔐 Contacts permission gate
   ========================================================= */

@Composable
fun ContactsPermissionGate(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var hasRead by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasWrite by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            hasRead = result[Manifest.permission.READ_CONTACTS] == true
            hasWrite = result[Manifest.permission.WRITE_CONTACTS] == true
        }

    LaunchedEffect(Unit) {
        if (!hasRead || !hasWrite) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    if (hasRead && hasWrite) {
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Please allow Contacts permission to continue")
        }
    }
}

/* =========================================================
   🏠 App root
   ========================================================= */

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }

    var currentTab by remember { mutableStateOf(BottomTab.Birthdays) }
    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var selected by remember { mutableStateOf<ContactModel?>(null) }

    fun refreshContactsAndWidget() {
        val today = LocalDate.now()
        contacts = repository.getContacts()
            .sortedBy { it.daysUntilBirthday(today) ?: Long.MAX_VALUE }

        WidgetRefresher.refresh(context)
    }

    LaunchedEffect(Unit) {
        refreshContactsAndWidget()
    }

    BackHandler(enabled = selected != null) {
        selected = null
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == BottomTab.Birthdays,
                    onClick = { currentTab = BottomTab.Birthdays },
                    icon = { Icon(Icons.Default.Cake, null) },
                    label = { Text("Birthdays") }
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.Calendar,
                    onClick = { currentTab = BottomTab.Calendar },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    label = { Text("Calendar") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                BottomTab.Birthdays -> BirthdaysScreen(
                    contacts = contacts,
                    selected = selected,
                    onSelect = { selected = it },
                    onDoneEditing = {
                        selected = null
                        refreshContactsAndWidget()
                    }
                )

                BottomTab.Calendar -> CalendarScreen(
                    contacts = contacts
                )
            }
        }
    }
}

/* =========================================================
   🎂 Birthdays screen (WITH SEARCH)
   ========================================================= */

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    selected: ContactModel?,
    onSelect: (ContactModel) -> Unit,
    onDoneEditing: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (selected == null) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // 🔍 Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = { Text("Search contacts") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredContacts) { contact ->
                    UpcomingBirthdayCard(
                        contact = contact,
                        onClick = { onSelect(contact) }
                    )
                }
            }
        }
    } else {
        EditContactScreen(
            contact = selected,
            onDone = onDoneEditing
        )
    }
}

/* =========================================================
   🧾 Birthday card
   ========================================================= */

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val days = contact.daysUntilBirthday(today)
    val isToday = contact.isBirthdayToday(today)
    val age =
        if (isToday) contact.ageToday(today)
        else contact.ageOnNextBirthday(today)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            contact.photoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )

                contact.birthday?.let {
                    Text(it)
                }

                if (days != null && age != null) {
                    Text(
                        text = if (isToday)
                            "🎉 Today! Turning $age"
                        else
                            "In $days days · Turning $age",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
