package com.chris.birthdaytracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.chris.birthdaytracker.ui.theme.BirthdayTrackerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class BottomTab {
    Birthdays, Calendar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BirthdayTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ContactsPermissionGate {
                        AppRoot()
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------- */

@Composable
fun ContactsPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            granted = it[Manifest.permission.READ_CONTACTS] == true &&
                    it[Manifest.permission.WRITE_CONTACTS] == true
        }

    LaunchedEffect(Unit) {
        if (!granted) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    if (granted) content()
    else Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Please allow contacts permission")
    }
}

/* ---------------------------------------------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }
    val scope = rememberCoroutineScope()

    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var selected by remember { mutableStateOf<ContactModel?>(null) }

    val pagerState = rememberPagerState { 2 }

    fun refresh() {
        contacts = repository.getContacts()
            .sortedBy { it.daysUntilBirthday(LocalDate.now()) ?: Long.MAX_VALUE }
        WidgetRefresher.refresh(context)
    }

    LaunchedEffect(Unit) { refresh() }

    BackHandler(enabled = selected != null) { selected = null }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    icon = { Icon(Icons.Default.Cake, null) },
                    label = { Text("Birthdays") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    label = { Text("Calendar") }
                )
            }
        }
    ) { padding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> BirthdaysScreen(
                    contacts = contacts,
                    selected = selected,
                    onSelect = { selected = it },
                    onDoneEditing = {
                        selected = null
                        refresh()
                    }
                )
                1 -> CalendarScreen(contacts = contacts)
            }
        }
    }
}

/* ---------------------------------------------------------- */

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>,
    selected: ContactModel?,
    onSelect: (ContactModel) -> Unit,
    onDoneEditing: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts
        else contacts.filter {
            it.displayName.contains(query, ignoreCase = true)
        }
    }

    if (selected == null) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            modifier = Modifier.clickable { query = "" }
                        )
                    }
                },
                placeholder = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) {
                    UpcomingBirthdayCard(it) { onSelect(it) }
                }
            }
        }
    } else {
        EditContactScreen(contact = selected, onDone = onDoneEditing)
    }
}

/* ---------------------------------------------------------- */
/* 🎉 TODAY PULSE ANIMATION */

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = contact.isBirthdayToday(today)
    val days = contact.daysUntilBirthday(today)
    val age =
        if (isToday) contact.ageToday(today)
        else contact.ageOnNextBirthday(today)

    val pulse by animateFloatAsState(
        targetValue = if (isToday) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isToday)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
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

            Spacer(Modifier.width(16.dp))

            Column {
                Text(contact.displayName, style = MaterialTheme.typography.bodyLarge)
                contact.birthday?.let { Text(it) }

                if (days != null && age != null) {
                    Text(
                        if (isToday) "🎉 Today! Turning $age"
                        else "In $days days · Turning $age",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
