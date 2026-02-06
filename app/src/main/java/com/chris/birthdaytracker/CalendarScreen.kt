package com.chris.birthdaytracker

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

/* =========================================================
   📅 Calendar Screen
   ========================================================= */

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val months = remember {
        (-12..12).map { YearMonth.now().plusMonths(it.toLong()) }
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(months) { month ->
                MonthSection(
                    month = month,
                    contacts = contacts,
                    onDateClick = { selectedDate = it }
                )
            }
        }

        selectedDate?.let { date ->
            AnimatedBirthdayPopup(
                date = date,
                contacts = contacts.filter { it.isBirthdayOn(date) },
                onDismiss = { selectedDate = null }
            )
        }
    }
}

/* =========================================================
   📆 Month Section
   ========================================================= */

@Composable
private fun MonthSection(
    month: YearMonth,
    contacts: List<ContactModel>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge
        )

        repeat(6) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { dayIndex ->
                    val dayNumber =
                        week * 7 + dayIndex + 1 - firstDay.dayOfWeek.value

                    if (dayNumber in 1..daysInMonth) {
                        val date = month.atDay(dayNumber)
                        val hasBirthday = contacts.any { it.isBirthdayOn(date) }

                        CalendarDay(
                            day = dayNumber,
                            hasBirthday = hasBirthday,
                            onClick = { onDateClick(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

/* =========================================================
   🗓️ Calendar Day
   ========================================================= */

@Composable
private fun CalendarDay(
    day: Int,
    hasBirthday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (hasBirthday)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else
                    Color.Transparent
            )
            .clickable(enabled = hasBirthday) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(day.toString())
    }
}

/* =========================================================
   🎉 Animated Popup (Entrance Animation)
   ========================================================= */

@Composable
private fun AnimatedBirthdayPopup(
    date: LocalDate,
    contacts: List<ContactModel>,
    onDismiss: () -> Unit
) {
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { show = true }

    val scale by animateFloatAsState(
        targetValue = if (show) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "popupScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = tween(350),
        label = "popupAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            BirthdayCelebrationCard(
                date = date,
                contacts = contacts
            )
        }
    }
}

/* =========================================================
   🎂 Celebration Card
   ========================================================= */

@Composable
private fun BirthdayCelebrationCard(
    date: LocalDate,
    contacts: List<ContactModel>
) {
    val glowAlpha by animateFloatAsState(
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            tween(1400),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                AnimatedConfettiBurst()

                contacts.forEach { contact ->
                    val age = contact.ageOnDate(date)

                    contact.photoUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp))
                        )
                    }

                    Text(
                        text = contact.displayName,
                        style = MaterialTheme.typography.titleLarge
                    )

                    age?.let {
                        TurningAgeText(it)
                    }

                    contact.birthday?.let {
                        Text("Born $it")
                    }
                }
            }
        }
    }
}

/* =========================================================
   🎊 Confetti Burst (one-time)
   ========================================================= */

@Composable
private fun AnimatedConfettiBurst() {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1200)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val emojis = listOf("🎉", "🎊", "✨", "🧁", "🎈")

            emojis.forEachIndexed { index, emoji ->
                val infinite = rememberInfiniteTransition(label = "confetti$index")

                val y by infinite.animateFloat(
                    initialValue = -20f,
                    targetValue = 160f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 900 + index * 150,
                            easing = LinearEasing
                        )
                    ),
                    label = "y"
                )

                val x by infinite.animateFloat(
                    initialValue = 0f,
                    targetValue = if (index % 2 == 0) 40f else -40f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "x"
                )

                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.offset(x.dp, y.dp)
                )
            }
        }
    }
}

/* =========================================================
   💓 Emphasised Turning Age
   ========================================================= */

@Composable
private fun TurningAgeText(age: Int) {
    val infinite = rememberInfiniteTransition(label = "agePulse")

    val scale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Text(
        text = "Turning $age",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.scale(scale)
    )
}
