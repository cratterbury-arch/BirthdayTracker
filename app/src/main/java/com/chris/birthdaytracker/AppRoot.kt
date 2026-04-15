package com.chris.birthdaytracker

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCalendarPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var didSkipPermissions by remember { mutableStateOf(false) }

    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Combined permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasContactsPermission = permissions[Manifest.permission.READ_CONTACTS] ?: hasContactsPermission
        hasCalendarPermission = permissions[Manifest.permission.READ_CALENDAR] ?: hasCalendarPermission
    }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    val refreshContacts = {
        scope.launch {
            isLoading = true
            val loadedContacts = ContactsRepository(context).getAllContacts()
            contacts = loadedContacts
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        // 🔔 Notifications permission
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // Request initial permissions
        if (!hasContactsPermission && !hasCalendarPermission) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALENDAR
                )
            )
        }
    }

    // Load contacts once permission is granted OR user skips
    LaunchedEffect(hasContactsPermission, hasCalendarPermission, didSkipPermissions) {
        if (hasContactsPermission || hasCalendarPermission || didSkipPermissions) {
            val loadedContacts = ContactsRepository(context).getAllContacts()
            contacts = loadedContacts

            // 🔊 Sound once per session
            if (!SoundPlaybackManager.hasSoundPlayedThisSession()) {
                val today = LocalDate.now()
                val birthdaysToday = loadedContacts.any { contact ->
                    contact.birthday != null &&
                    contact.birthday.month == today.month &&
                    contact.birthday.dayOfMonth == today.dayOfMonth
                }

                if (birthdaysToday) {
                    playBirthdaySound(context)
                    SoundPlaybackManager.markSoundAsPlayedThisSession()
                }
            }
        }
    }

    if (!hasContactsPermission && !hasCalendarPermission && !didSkipPermissions) {
        PermissionRequestScreen(
            onRequest = {
                permissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALENDAR
                    )
                )
            },
            onSkip = {
                didSkipPermissions = true
            }
        )
    } else {
        AppScaffold(
            contacts = contacts,
            onRefresh = { refreshContacts() },
            isRefreshing = isLoading
        )
    }
}
