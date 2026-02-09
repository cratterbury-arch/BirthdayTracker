package com.chris.birthdaytracker

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UpcomingBirthdayCard(
    contact: ContactModel
) {
    val today = LocalDate.now()
    val birthday = contact.birthday ?: return

    val nextBirthday = birthday.nextBirthdayFrom(today)
    val daysToGo = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    val isToday = daysToGo == 0
    val ageOnNext = nextBirthday.year - birthday.year

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
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

                // DOB – quieter, bottom aligned
                Text(
                    text = birthday.format(DISPLAY_DATE_FORMAT),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isToday) {
                TodayBadge()
            } else {
                DaysToGoCircle(daysToGo)
            }
        }
    }
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
        targetValue = if (isToday) 1.08f else 1f,
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
            Text("in", fontSize = 11.sp, color = plum)
            Text(
                text = days.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = plum
            )
            Text("days", fontSize = 11.sp, color = plum)
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
        Text(
            text = "TODAY 🎉",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
