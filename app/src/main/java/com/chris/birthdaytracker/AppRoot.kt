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

    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }

    // Contacts permission launcher
    val contactsPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasContactsPermission = granted
        }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    LaunchedEffect(Unit) {
        // 🔔 Notifications permission
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // 📇 Contacts permission
        if (!hasContactsPermission) {
            contactsPermissionLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }
    }

    // Load contacts once permission is granted
    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission) {
            contacts = ContactsRepository(context).getAllContacts()

            // 🔔 Schedule notifications AFTER contacts load
            BirthdayNotificationScheduler
                .scheduleBirthdayNotifications(context, contacts)
        }
    }

    if (!hasContactsPermission) {
        PermissionRequestScreen {
            contactsPermissionLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }
    } else {
        AppScaffold(contacts)
    }
}
