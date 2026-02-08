package com.chris.birthdaytracker

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun AppRoot() {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var contacts by remember { mutableStateOf<List<ContactModel>>(emptyList()) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasPermission = granted
        }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            contacts = ContactsRepository(context).getAllContacts()
        }
    }

    if (!hasPermission) {
        PermissionRequestScreen {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    } else {
        BirthdaysScreen(contacts)
    }
}
