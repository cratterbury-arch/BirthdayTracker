package com.chris.birthdaytracker

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class CardData(
    val message: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiGreetingCardScreen(
    contact: ContactModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isGenerating by remember { mutableStateOf(false) }
    var generatedCard by remember { mutableStateOf<CardData?>(null) }

    var interests by remember { mutableStateOf(contact.tags.joinToString(", ")) }
    var selectedStyle by remember { mutableStateOf("3D Render") }
    var editedMessage by remember {
        mutableStateOf("Wishing you a wonderful birthday filled with joy!")
    }

    val styles = listOf(
        "3D Render",
        "Cartoon",
        "Sketch",
        "Watercolor",
        "Anime",
        "Cyberpunk",
        "Minimalist"
    )

    val firstName = remember(contact.name) {
        contact.name.trim().split("\\s+".toRegex()).firstOrNull() ?: contact.name
    }

    val aiService = remember {
        if (BuildConfig.GEMINI_API_KEY.isNotEmpty()) {
            GeminiAiService(
                apiKey = BuildConfig.GEMINI_API_KEY,
                modelName = BuildConfig.GEMINI_MODEL_NAME
            )
        } else {
            OpenAiService(BuildConfig.OPENAI_API_KEY)
        }
    }

    fun generateCard() {
        if (isGenerating) return

        isGenerating = true

        scope.launch {
            try {
                Log.d("AIGreetingCard", "🚀 Generating card with AI service")

                val result = aiService.generateCard(
                    name = firstName,
                    interests = interests,
                    style = selectedStyle
                )

                generatedCard = CardData(
                    message = result.message,
                    imageUrl = result.imageUrl
                )

                editedMessage = result.message

                Log.d("AIGreetingCard", "✅ Design generated successfully")

            } catch (e: Exception) {
                Log.e("AIGreetingCard", "❌ Generation failed", e)

                Toast.makeText(
                    context,
                    "AI generation failed. Check Logcat.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isGenerating = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Designer Playground") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GreetingCardPreview(
                name = firstName,
                data = generatedCard,
                displayMessage = editedMessage,
                isGenerating = isGenerating
            )

            OutlinedTextField(
                value = editedMessage,
                onValueChange = { editedMessage = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Personalize Message") },
                placeholder = { Text("Enter a custom message...") },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            if (generatedCard != null && !isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val uri = downloadAndSaveImageToCache(
                                    context = context,
                                    url = generatedCard!!.imageUrl
                                )

                                shareCardWithImage(
                                    context = context,
                                    name = firstName,
                                    message = editedMessage,
                                    imageUri = uri
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                saveImageToGallery(
                                    context = context,
                                    url = generatedCard!!.imageUrl
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Redesign Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("Interests (e.g. Pokemon, Music, Pizza)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                Text(
                    text = "Art Style",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styles.forEach { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style) },
                            shape = CircleShape
                        )
                    }
                }
            }

            Button(
                onClick = { generateCard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isGenerating,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Designing...")
                } else {
                    Icon(Icons.Default.Brush, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (generatedCard == null) "Create Card" else "Regenerate",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingCardPreview(
    name: String,
    data: CardData?,
    displayMessage: String,
    isGenerating: Boolean
) {
    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF1A1C1E),
            Color(0xFF000000)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(32.dp)),
        elevation = CardDefaults.cardElevation(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            if (data != null && data.imageUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = data.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (isGenerating) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (data == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Ready to create something special",
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HAPPY BIRTHDAY",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = name.uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = displayMessage,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

private suspend fun downloadAndSaveImageToCache(
    context: android.content.Context,
    url: String
): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)

            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap ?: return@withContext null

            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()

            val file = File(cachePath, "shared_birthday_card.png")

            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("AIGreetingCard", "Share image cache failed", e)
            null
        }
    }
}

private suspend fun saveImageToGallery(
    context: android.content.Context,
    url: String
) {
    withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)

            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap
                ?: throw Exception("Failed to load image")

            val filename = "birthday_card_${System.currentTimeMillis()}.png"

            val outputStream: OutputStream? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val imageUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    imageUri?.let {
                        context.contentResolver.openOutputStream(it)
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    )

                    val image = File(imagesDir, filename)
                    FileOutputStream(image)
                }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved to Pictures", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("AIGreetingCard", "Save image failed", e)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Save failed: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

private fun shareCardWithImage(
    context: android.content.Context,
    name: String,
    message: String,
    imageUri: Uri?
) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND

        if (imageUri != null) {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }

        putExtra(
            Intent.EXTRA_TEXT,
            "Happy Birthday, $name!\n\n$message"
        )
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Share Birthday Card"
        )
    )
}