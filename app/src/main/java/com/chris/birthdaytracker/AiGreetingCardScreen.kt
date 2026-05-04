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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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

private val PlayfairDisplay = FontFamily(Font(R.font.playfair_display))
private val EmilysCandy = FontFamily(Font(R.font.emilys_candy))
private val DynaPuff = FontFamily(Font(R.font.dyna_puff))
private val LondrinaShadow = FontFamily(Font(R.font.lodrina_shadow))
private val ElmsSands = FontFamily(Font(R.font.elms_sands))

private data class CardFontStyle(
    val label: String,
    val fontFamily: FontFamily,
    val headlineWeight: FontWeight,
    val messageWeight: FontWeight,
    val titleSize: TextUnit,
    val nameSize: TextUnit,
    val messageSize: TextUnit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiGreetingCardScreen(
    contact: ContactModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pixabayService = remember {
        PixabayService(BuildConfig.PIXABAY_API_KEY)
    }

    val firstName = remember(contact.name) {
        contact.name.trim().split("\\s+".toRegex()).firstOrNull() ?: contact.name
    }

    val categories = listOf(
        "cute birthday illustration",
        "birthday card illustration",
        "cartoon birthday",
        "elegant birthday illustration",
        "birthday cake illustration",
        "balloons birthday illustration"
    )

    val fontStyles = listOf(
        CardFontStyle(
            label = "Elegant",
            fontFamily = PlayfairDisplay,
            headlineWeight = FontWeight.SemiBold,
            messageWeight = FontWeight.Normal,
            titleSize = 35.sp,
            nameSize = 34.sp,
            messageSize = 18.sp
        ),
        CardFontStyle(
            label = "Cute",
            fontFamily = EmilysCandy,
            headlineWeight = FontWeight.Normal,
            messageWeight = FontWeight.Normal,
            titleSize = 38.sp,
            nameSize = 36.sp,
            messageSize = 19.sp
        ),
        CardFontStyle(
            label = "Fun",
            fontFamily = DynaPuff,
            headlineWeight = FontWeight.Bold,
            messageWeight = FontWeight.Normal,
            titleSize = 38.sp,
            nameSize = 36.sp,
            messageSize = 18.sp
        ),
        CardFontStyle(
            label = "Shadow",
            fontFamily = LondrinaShadow,
            headlineWeight = FontWeight.Normal,
            messageWeight = FontWeight.Normal,
            titleSize = 44.sp,
            nameSize = 40.sp,
            messageSize = 20.sp
        ),
        CardFontStyle(
            label = "Simple",
            fontFamily = ElmsSands,
            headlineWeight = FontWeight.Bold,
            messageWeight = FontWeight.Normal,
            titleSize = 36.sp,
            nameSize = 34.sp,
            messageSize = 19.sp
        )
    )

    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedFontStyle by remember { mutableStateOf(fontStyles.first()) }
    var customSearch by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<PixabayImage>>(emptyList()) }
    var selectedImage by remember { mutableStateOf<PixabayImage?>(null) }
    var message by remember {
        mutableStateOf("Wishing you a birthday filled with love, laughter and lovely memories.")
    }
    var isLoading by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(1) }

    fun searchImages(resetPage: Boolean = true) {
        if (isLoading) return

        val query = customSearch.ifBlank { selectedCategory }

        if (resetPage) {
            page = 1
        }

        isLoading = true

        scope.launch {
            try {
                val result = pixabayService.searchImages(
                    query = query,
                    page = page
                )

                images = result
                selectedImage = result.firstOrNull()

                if (result.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No images found. Try another style.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("PixabayCard", "Image search failed", e)

                Toast.makeText(
                    context,
                    e.message ?: "Image search failed",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        searchImages(resetPage = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Birthday Card Maker") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searchImages(resetPage = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh images"
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
            BirthdayCardPreview(
                name = firstName,
                message = message,
                image = selectedImage,
                isLoading = isLoading,
                fontStyle = selectedFontStyle
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Card message") },
                placeholder = { Text("Write your birthday message...") },
                shape = RoundedCornerShape(16.dp),
                maxLines = 4
            )

            Text(
                text = "Font style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fontStyles.forEach { style ->
                    FilterChip(
                        selected = selectedFontStyle.label == style.label,
                        onClick = { selectedFontStyle = style },
                        label = { Text(style.label) },
                        shape = CircleShape
                    )
                }
            }

            if (selectedImage != null && !isLoading) {
                val imageForActions = selectedImage

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (imageForActions == null) return@Button

                            scope.launch {
                                val uri = downloadImageToCache(
                                    context = context,
                                    url = imageForActions.imageUrl
                                )

                                shareCard(
                                    context = context,
                                    name = firstName,
                                    message = message,
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
                            if (imageForActions == null) return@OutlinedButton

                            scope.launch {
                                saveImageToGallery(
                                    context = context,
                                    url = imageForActions.imageUrl
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

                selectedImage?.let { image ->
                    Text(
                        text = "Image by ${image.user} on Pixabay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 0.5.dp
                )
            }

            Text(
                text = "Choose an image style",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            customSearch = ""
                            searchImages(resetPage = true)
                        },
                        label = { Text(category.replaceFirstChar { it.uppercase() }) },
                        shape = CircleShape
                    )
                }
            }

            OutlinedTextField(
                value = customSearch,
                onValueChange = { customSearch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Or search for your own theme") },
                placeholder = { Text("e.g. dinosaurs birthday, princess party...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Button(
                onClick = { searchImages(resetPage = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Finding images...")
                } else {
                    Icon(Icons.Default.ImageSearch, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Find Images")
                }
            }

            Text(
                text = "Tap an image to use it",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images, key = { it.id }) { image ->
                    ImageChoiceTile(
                        image = image,
                        selected = selectedImage?.id == image.id,
                        onClick = { selectedImage = image }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    page += 1
                    searchImages(resetPage = false)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Load More Images")
            }
        }
    }
}

@Composable
private fun BirthdayCardPreview(
    name: String,
    message: String,
    image: PixabayImage?,
    isLoading: Boolean,
    fontStyle: CardFontStyle
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(32.dp)),
        elevation = CardDefaults.cardElevation(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (image != null) {
                SubcomposeAsyncImage(
                    model = image.imageUrl,
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
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(54.dp)
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
                                Color.Black.copy(alpha = 0.84f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                when {
                    isLoading && image == null -> {
                        CircularProgressIndicator(color = Color.White)
                    }

                    image == null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageSearch,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Choose an image to start",
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Happy Birthday",
                                color = Color.White,
                                fontFamily = fontStyle.fontFamily,
                                fontWeight = fontStyle.headlineWeight,
                                fontSize = fontStyle.titleSize,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = name,
                                color = Color.White,
                                fontFamily = fontStyle.fontFamily,
                                fontWeight = fontStyle.headlineWeight,
                                fontSize = fontStyle.nameSize,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = message,
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.96f),
                                fontFamily = fontStyle.fontFamily,
                                fontWeight = fontStyle.messageWeight,
                                fontSize = fontStyle.messageSize,
                                lineHeight = 25.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageChoiceTile(
    image: PixabayImage,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (selected) 3.dp else 0.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            SubcomposeAsyncImage(
                model = image.previewUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}

private suspend fun downloadImageToCache(
    context: android.content.Context,
    url: String
): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            val result = loader.execute(request) as? SuccessResult
            val bitmap = (result?.drawable as? BitmapDrawable)?.bitmap
                ?: return@withContext null

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
            Log.e("PixabayCard", "Image cache failed", e)
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

            val result = loader.execute(request) as? SuccessResult
            val bitmap = (result?.drawable as? BitmapDrawable)?.bitmap
                ?: throw Exception("Failed to load image")

            val filename = "birthday_card_${System.currentTimeMillis()}.png"

            val outputStream: OutputStream? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES
                        )
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
                    val imageFile = File(imagesDir, filename)
                    FileOutputStream(imageFile)
                }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved to Pictures", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PixabayCard", "Save image failed", e)

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

private fun shareCard(
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