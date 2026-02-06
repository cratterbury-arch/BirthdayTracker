package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun EditContactScreen(
    contact: ContactModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val repository = remember { ContactsRepository(context) }

    var birthdayField by remember {
        mutableStateOf(TextFieldValue(contact.birthday ?: ""))
    }

    var error by remember { mutableStateOf<String?>(null) }

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

        OutlinedTextField(
            value = birthdayField,
            onValueChange = {
                birthdayField = formatBirthdayInput(it)
            },
            label = { Text("Birthday (dd/MM/yyyy)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                // Explicitly dismiss keyboard
                focusManager.clearFocus(force = true)

                try {
                    repository.updateBirthday(
                        contactId = contact.id,
                        birthday = birthdayField.text.trim()
                    )
                    onDone()
                } catch (e: Exception) {
                    error = "Failed to save birthday"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
