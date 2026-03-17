package com.chris.birthdaytracker

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    contact: ContactModel?,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")

    // If it's a phone contact, we show a read-only view or a message
    val isReadOnly = contact?.isFromPhone == true

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var birthdayInput by remember { 
        mutableStateOf(contact?.birthday?.format(formatter) ?: "") 
    }
    var photoUri by remember { mutableStateOf(contact?.photoUri) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
            }
            photoUri = it
        }
    }

    val birthday = remember(birthdayInput) {
        try { LocalDate.parse(birthdayInput, formatter) } catch (e: Exception) { null }
    }

    val calendar = Calendar.getInstance()
    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selectedDate = LocalDate.of(year, month + 1, day)
                birthdayInput = selectedDate.format(formatter)
            },
            birthday?.year ?: calendar.get(Calendar.YEAR),
            (birthday?.monthValue?.minus(1)) ?: calendar.get(Calendar.MONTH),
            birthday?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    suspend fun updateWidget() {
        // Update Glance widget
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(BirthdayWidget::class.java)
        glanceIds.forEach { id ->
            BirthdayWidget().update(context, id)
        }
        // Update RemoteViews widget
        BirthdayWidgetProvider.refreshAllWidgets(context)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    contact == null -> "Add Birthday"
                    isReadOnly -> "Contact Details"
                    else -> "Edit Birthday"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            if (contact != null && !isReadOnly) {
                IconButton(onClick = {
                    scope.launch {
                        val db = BirthdayApplication.getDatabase(context)
                        db.contactDao().deleteById(contact.id)
                        updateWidget()
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (isReadOnly) {
            Text(
                text = "Note: Phone contacts cannot be edited directly in this app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .then(if (!isReadOnly) Modifier.clickable { photoPickerLauncher.launch("image/*") } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize()
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { if (!isReadOnly) name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = isReadOnly
        )

        OutlinedTextField(
            value = birthdayInput,
            onValueChange = { input ->
                if (!isReadOnly && input.length <= 8) {
                    birthdayInput = input.filter { it.isDigit() }
                }
            },
            label = { Text("Birthday (DD/MM/YYYY)") },
            placeholder = { Text("DD/MM/YYYY") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateTransformation(),
            trailingIcon = {
                if (!isReadOnly) {
                    IconButton(onClick = { datePicker.show() }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Pick date"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = isReadOnly
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isReadOnly) {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalBirthday = try {
                        LocalDate.parse(birthdayInput, formatter)
                    } catch (e: Exception) {
                        null
                    }

                    if (finalBirthday == null) {
                        Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        val db = BirthdayApplication.getDatabase(context)
                        val entity = LocalContactEntity(
                            id = contact?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            birthday = finalBirthday,
                            photoUri = photoUri?.toString()
                        )
                        db.contactDao().insert(entity)
                        updateWidget()
                        
                        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        } else {
            Button(
                onClick = { onDone() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        var out = ""
        for (i in input.indices) {
            out += input[i]
            if (i == 1 || i == 3) out += "/"
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return out.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return input.length
            }
        }

        return TransformedText(AnnotatedString(out), numberOffsetTranslator)
    }
}
