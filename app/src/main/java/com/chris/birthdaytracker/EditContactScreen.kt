package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EditContactScreen(
    contact: ContactModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current

    var dateDigits by remember {
        mutableStateOf(
            contact.birthday
                ?.replace("/", "")
                ?.take(8)
                ?: ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.titleLarge
        )

        BirthdayDateField(
            value = dateDigits,
            onValueChange = { dateDigits = it }
        )

        Button(
            onClick = {
                ContactEditor.updateBirthday(
                    context,
                    contact.id,
                    dateDigits
                )
                onDone()
            },
            enabled = dateDigits.length == 8
        ) {
            Text("Save birthday")
        }
    }
}
