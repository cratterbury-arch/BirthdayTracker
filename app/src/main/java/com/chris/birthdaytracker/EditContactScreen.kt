package com.chris.birthdaytracker

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun EditContactScreen(
    contact: ContactModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }

    var birthday by remember { mutableStateOf(contact.birthday ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = birthday,
            onValueChange = { birthday = it },
            label = { Text("Birthday (dd/MM/yyyy)") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !isSaving,
            onClick = {
                val trimmed = birthday.trim()

                if (trimmed.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Please enter a birthday",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                try {
                    LocalDate.parse(trimmed, formatter)

                    isSaving = true
                    repository.updateBirthday(
                        contactId = contact.id,
                        birthday = trimmed
                    )

                    // 🔁 FORCE WIDGET REFRESH AFTER SAVE
                    WidgetRefresher.refresh(context)

                    onDone()

                } catch (e: DateTimeParseException) {
                    Toast.makeText(
                        context,
                        "Invalid date format. Use dd/MM/yyyy",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Failed to save birthday",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isSaving = false
                }
            }
        ) {
            Text(if (isSaving) "Saving…" else "Save")
        }
    }
}
