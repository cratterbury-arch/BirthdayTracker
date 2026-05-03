package com.chris.birthdaytracker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    contacts: List<ContactModel>,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )

    val scope = rememberCoroutineScope()
    var selectedContact by rememberSaveable { mutableStateOf<ContactModel?>(null) }
    var showAddContact by remember { mutableStateOf(false) }
    var preselectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    label = { Text("Birthdays") }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null) },
                    label = { Text("Events") }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(2) }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendar") }
                )

                NavigationBarItem(
                    selected = pagerState.currentPage == 3,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(3) }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0 || pagerState.currentPage == 2) {
                FloatingActionButton(
                    onClick = { 
                        preselectedDate = null
                        showAddContact = true 
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Birthday")
                }
            }
        }
    ) { innerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> BirthdaysTab(
                    contacts = contacts,
                    onRefresh = onRefresh,
                    isRefreshing = isRefreshing
                )
                1 -> EventsScreen()
                2 -> CalendarScreen(
                    contacts = contacts,
                    selectedContact = selectedContact,
                    onContactSelected = { contact ->
                        selectedContact = contact
                    },
                    onDateSelected = { date ->
                        preselectedDate = date
                        showAddContact = true
                    },
                    onPopupDismissed = { selectedContact = null }
                )
                3 -> SettingsScreen(onDataChanged = onRefresh)
            }
        }

        if (showAddContact) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showAddContact = false 
                    preselectedDate = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                EditContactScreen(
                    contact = null,
                    initialDate = preselectedDate,
                    onDone = {
                        showAddContact = false
                        preselectedDate = null
                        onRefresh()
                    }
                )
            }
        }
    }
}
