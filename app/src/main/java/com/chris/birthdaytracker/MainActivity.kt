package com.chris.birthdaytracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.chris.birthdaytracker.ui.theme.BirthdayTrackerTheme
import kotlinx.coroutines.launch
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
/* 🔐 CONTACT PERMISSION GATE */

@Composable
fun ContactsPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED &&
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
            granted =
                result[Manifest.permission.READ_CONTACTS] == true &&
                        result[Manifest.permission.WRITE_CONTACTS] == true
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

    if (granted) {
        content()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Please allow contacts permission")
        }
    }
}

/* ---------------------------------------------------------- */
/* 🧠 APP ROOT */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }
    val scope = rememberCoroutineScope()

    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var selected by remember { mutableStateOf<ContactModel?>(null) }

    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    fun refreshContacts() {
        contacts = repository.getContacts()
            .sortedBy { it.daysUntilBirthday(LocalDate.now()) ?: Long.MAX_VALUE }
    }

    LaunchedEffect(Unit) {
        refreshContacts()
    }

    BackHandler(enabled = selected != null) {
        selected = null
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(Icons.Default.Cake, null) },
                    label = { Text("Birthdays") }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    label = { Text("Calendar") }
                )
            }
        }
    ) { padding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> BirthdaysScreen(
                    contacts = contacts,
                    selected = selected,
                    onSelect = { selected = it },
                    onDoneEditing = {
                        selected = null
                        refreshContacts()
                    }
                )

                1 -> CalendarScreen(
                    contacts = contacts
                )
            }
        }
    }
}
