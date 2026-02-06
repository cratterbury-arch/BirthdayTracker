package com.chris.birthdaytracker

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@Composable
fun EditContactScreen(
    contact: ContactModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ContactsRepository(context) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    var textState by remember {
        mutableStateOf(
            TextFieldValue(
                text = contact.birthday ?: "",
                selection = TextRange((contact.birthday ?: "").length)
            )
        )
    }

    fun formatAndPreserveCursor(
        old: TextFieldValue,
        new: TextFieldValue
    ): TextFieldValue {
        // Handle deletions naturally
        if (new.text.length < old.text.length) {
            return new
        }

        val digits = new.text.filter { it.isDigit() }.take(8)

        val formatted = buildString {
            if (digits.length >= 2) {
                append(digits.substring(0, 2))
                if (digits.length > 2) append("/")
            } else {
                append(digits)
                return@buildString
            }

            if (digits.length >= 4) {
                append(digits.substring(2, 4))
                if (digits.length > 4) append("/")
            } else {
                append(digits.substring(2))
                return@buildString
            }

            append(digits.substring(4))
        }

        val newCursor = formatted.length.coerceAtMost(formatted.length)

        return TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor)
        )
    }

    val isValidDate = remember(textState.text) {
        try {
            LocalDate.parse(textState.text, formatter)
            true
        } catch (_: Exception) {
            false
        }
    }

    val initialDate = remember(contact.birthday) {
        try {
            contact.birthday?.let { LocalDate.parse(it, formatter) }
        } catch (_: Exception) {
            null
        }
    }

    val calendar = Calendar.getInstance().apply {
        initialDate?.let {
            set(it.year, it.monthValue - 1, it.dayOfMonth)
        }
    }

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val picked = LocalDate.of(year, month + 1, day)
                val formatted = picked.format(formatter)
                textState = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = textState,
            onValueChange = { new ->
                textState = formatAndPreserveCursor(textState, new)
            },
            label = { Text("Birthday (dd/MM/yyyy)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = {
                IconButton(onClick = { datePicker.show() }) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Pick date"
                    )
                }
            },
            isError = textState.text.isNotBlank() && !isValidDate,
            supportingText = {
                if (textState.text.isNotBlank() && !isValidDate) {
                    Text("Enter a valid date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                try {
                    LocalDate.parse(textState.text, formatter)

                    repository.updateBirthday(
                        contactId = contact.id,
                        birthday = textState.text
                    )

                    WidgetRefresher.refresh(context)
                    onDone()

                } catch (_: DateTimeParseException) {
                    Toast.makeText(
                        context,
                        "Please enter a valid date (dd/MM/yyyy)",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to save birthday",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            enabled = textState.text.isNotBlank() && isValidDate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
