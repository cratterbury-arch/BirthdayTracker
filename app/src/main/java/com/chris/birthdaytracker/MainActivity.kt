package com.chris.birthdaytracker

import android.Manifest
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.chris.birthdaytracker.ui.theme.BirthdayTrackerTheme
import android.content.pm.PackageManager

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
   🔐 Permission Gate (CRITICAL)
   ========================================================= */

@Composable
fun ContactsPermissionGate(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    if (hasPermission) {
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Please allow contacts access to continue")
        }
    }
}

/* =========================================================
   🏠 App Root
   ========================================================= */

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }

    var currentTab by remember { mutableStateOf(BottomTab.Birthdays) }
    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var selected by remember { mutableStateOf<ContactModel?>(null) }

    LaunchedEffect(Unit) {
        contacts = repository.getContacts()
            .sortedBy { it.daysUntilBirthday() ?: Long.MAX_VALUE }
    }

    // 🔙 Handle back gesture when editing
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
                    onSelect = { selected = it },
                    onRefresh = {
                        contacts = repository.getContacts()
                            .sortedBy { it.daysUntilBirthday() ?: Long.MAX_VALUE }
                    },
                    selected = selected,
                    onDoneEditing = { selected = null }
                )

                BottomTab.Calendar -> CalendarScreen(
                    contacts = contacts
                )
            }
        }
    }
}

/* =========================================================
   🎂 Birthdays Screen
   ========================================================= */

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    onSelect: (ContactModel) -> Unit,
    onRefresh: () -> Unit,
    selected: ContactModel?,
    onDoneEditing: () -> Unit
) {
    if (selected == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contacts) { contact ->
                UpcomingBirthdayCard(contact) {
                    onSelect(contact)
                }
            }
        }
    } else {
        EditContactScreen(
            contact = selected,
            onDone = {
                onDoneEditing()
                onRefresh()
            }
        )
    }
}

/* =========================================================
   🧾 Birthday Card
   ========================================================= */

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel,
    onClick: () -> Unit
) {
    val days = contact.daysUntilBirthday()
    val age = contact.ageOnNextBirthday()
    val isToday = days == 0L

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
                    Text(text = it)
                }

                if (days != null && age != null) {
                    val label =
                        if (isToday) "🎉 Today! Turning $age"
                        else "In $days days · Turning $age"

                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
