package com.chris.birthdaytracker

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.core.net.toUri
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val birthday = contact.birthday ?: return

    val nextBirthday = birthday.nextBirthdayFrom(today)
    val daysToGo = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    val isToday = daysToGo == 0
    val ageOnNext = nextBirthday.year - birthday.year

    val cardModifier = if (isToday) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF44336),
                        Color(0xFFFFEB3B),
                        Color(0xFF4CAF50),
                        Color(0xFF2196F3),
                        Color(0xFF9C27B0)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                BirthdayPhoto(
                    photoUri = contact.photoUri,
                    isToday = isToday
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // Name
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // AGE – visual focus
                    Text(
                        text = "Turning $ageOnNext 🎂",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start
                    )

                    // Source & DOB info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = birthday.format(DISPLAY_DATE_FORMAT),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        contact.accountName?.let { account ->
                            Spacer(Modifier.width(8.dp))
                            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = account,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isToday) {
                    TodayBadge()
                } else {
                    DaysToGoCircle(daysToGo)
                }
            }

            if (isToday) {
                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                TextButton(
                    onClick = { launchMessagingIntent(context, contact.id) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wish them a Happy Birthday!")
                }
            }
        }
    }
}

private fun launchMessagingIntent(context: android.content.Context, contactId: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "smsto:${contactId}".toUri()
        putExtra("sms_body", "Happy birthday!")
    }
    val chooser = Intent.createChooser(intent, "send them a birthday message!")
    context.startActivity(chooser)
}

/* ---------- Date helpers ---------- */

private fun LocalDate.nextBirthdayFrom(today: LocalDate): LocalDate {
    val thisYear = withYear(today.year)
    return when {
        thisYear.isEqual(today) -> thisYear
        thisYear.isAfter(today) -> thisYear
        else -> thisYear.plusYears(1)
    }
}

private val DISPLAY_DATE_FORMAT =
    DateTimeFormatter.ofPattern("d MMM")

/* ---------- Photo ---------- */

@Composable
private fun BirthdayPhoto(
    photoUri: Uri?,
    isToday: Boolean
) {
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isToday) { 1.08f } else { 1f },
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(if (isToday) 72.dp else 56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/* ---------- Countdown ---------- */

@Composable
private fun DaysToGoCircle(days: Int) {
    val plum = Color(0xFF6A4C93)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFE9DFFF))
            .border(2.dp, plum, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "in",
                fontSize = 9.sp,
                lineHeight = 9.sp,
                color = plum
            )

            Text(
                text = days.toString(),
                fontSize = 17.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                color = plum
            )

            Text(
                text = if (days == 1) "day" else "days",
                fontSize = 9.sp,
                lineHeight = 9.sp,
                color = plum
            )
        }
    }
}



/* ---------- Today badge ---------- */

@Composable
private fun TodayBadge() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TODAY",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "🎉",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
