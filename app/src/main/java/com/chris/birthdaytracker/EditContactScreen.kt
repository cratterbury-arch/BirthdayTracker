package com.chris.birthdaytracker

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditContactScreen(
    contact: ContactModel?,
    initialDate: LocalDate? = null,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")

    // If it's a phone contact, we show a read-only view for basic info
    val isReadOnly = contact?.isFromPhone == true
    val repository = remember { ContactsRepository(context) }

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var birthdayInput by remember { 
        val initial = contact?.birthday ?: initialDate
        mutableStateOf(initial?.format(formatter) ?: "") 
    }
    var photoUri by remember { mutableStateOf(contact?.photoUri) }
    
    // Tag/Hobbies management - now editable for ALL contacts
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(contact?.tags ?: emptyList()) }

    var showAiCardScreen by remember { mutableStateOf(false) }

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
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(BirthdayWidget::class.java)
        glanceIds.forEach { id ->
            BirthdayWidget().update(context, id)
        }
        BirthdayWidgetProvider.refreshAllWidgets(context)
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim().replace(",", "")
        if (trimmed.isNotEmpty() && !tags.contains(trimmed)) {
            tags = tags + trimmed
            tagInput = ""
        }
    }

    if (showAiCardScreen && contact != null) {
        AiGreetingCardScreen(
            contact = contact.copy(tags = tags), // Pass current tags
            onBack = { showAiCardScreen = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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

                Row {
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
                    
                    // Added Close Button since swipe-down is disabled
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            if (contact != null) {
                // AI GREETING CARD BUTTON
                Button(
                    onClick = { showAiCardScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("AI Greeting Card Generator")
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Source,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Source: ${contact.source.name} (${contact.accountName ?: "Device"})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isReadOnly) {
                Text(
                    text = "Note: System contacts' names and dates cannot be edited here, but you can add gift ideas below!",
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

            Divider()

            Text(
                "Gift Ideas & Hobbies",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { tags = tags - tag },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            OutlinedTextField(
                value = tagInput,
                onValueChange = { 
                    if (it.endsWith(",") || it.endsWith("\n")) {
                        addTag(it)
                    } else {
                        tagInput = it
                    }
                },
                placeholder = { Text("Add hobbies or gift ideas (comma to separate)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addTag(tagInput) })
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        
                        if (isReadOnly && contact != null) {
                            // Saving metadata for sync'd contact
                            val key = "${contact.name.lowercase().trim()}_${contact.birthday?.monthValue}_${contact.birthday?.dayOfMonth}"
                            db.metadataDao().insert(ContactMetadataEntity(
                                contactKey = key,
                                tags = tags.joinToString(","),
                                isFavorite = contact.isFavorite
                            ))
                        } else {
                            // Saving local contact
                            val entity = LocalContactEntity(
                                id = contact?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                birthday = finalBirthday,
                                photoUri = photoUri?.toString(),
                                isFavorite = contact?.isFavorite ?: false,
                                tags = tags.joinToString(",")
                            )
                            db.contactDao().insert(entity)
                        }
                        
                        updateWidget()
                        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
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
