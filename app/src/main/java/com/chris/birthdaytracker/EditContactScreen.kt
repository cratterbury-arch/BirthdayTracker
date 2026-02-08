package com.chris.birthdaytracker

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun EditContactScreen(
    contact: ContactModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // 🔒 Force birthday into a String no matter what its original type is
    val initialBirthdayText = remember(contact) {
        contact.birthday?.toString() ?: ""
    }

    var birthdayText by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialBirthdayText,
                selection = TextRange(initialBirthdayText.length)
            )
        )
    }

    val calendar = Calendar.getInstance()

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                val formatted = date.format(formatter)
                birthdayText = TextFieldValue(
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
            value = birthdayText,
            onValueChange = { birthdayText = it },
            label = { Text("Birthday (dd/MM/yyyy)") },
            trailingIcon = {
                IconButton(onClick = { datePicker.show() }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Pick date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                Toast.makeText(context, "Saved (temporarily)", Toast.LENGTH_SHORT).show()
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
