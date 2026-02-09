package com.chris.birthdaytracker

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val today = LocalDate.now()
    val months = remember {
        (-12..12).map { YearMonth.now().plusMonths(it.toLong()) }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(YearMonth.now()).coerceAtLeast(0)
    )

    var selectedContact by remember { mutableStateOf<ContactModel?>(null) }

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            months.forEach { month ->
                item {
                    MonthSection(
                        month = month,
                        contacts = contacts,
                        today = today,
                        onContactClick = { selectedContact = it }
                    )
                }
            }
        }

        selectedContact?.let {
            BirthdayPopup(
                contact = it,
                onDismiss = { selectedContact = null }
            )
        }
    }
}

/* ---------- Month ---------- */

@Composable
private fun MonthSection(
    month: YearMonth,
    contacts: List<ContactModel>,
    today: LocalDate,
    onContactClick: (ContactModel) -> Unit
) {
    val birthdaysThisMonth = remember(contacts, month) {
        contacts.filter { it.birthday?.month == month.month }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        CalendarGrid(
            month = month,
            birthdays = birthdaysThisMonth,
            today = today,
            onContactClick = onContactClick
        )
    }
}

/* ---------- Grid ---------- */

@Composable
private fun CalendarGrid(
    month: YearMonth,
    birthdays: List<ContactModel>,
    today: LocalDate,
    onContactClick: (ContactModel) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = (firstDay.dayOfWeek.value % 7)

    val birthdayMap = birthdays.groupBy { it.birthday!!.dayOfMonth }

    Column {
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach {
                Text(
                    text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        var day = 1
        repeat(6) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val index = row * 7 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index >= startOffset && day <= daysInMonth) {
                            val contactsToday = birthdayMap[day].orEmpty()
                            DayCell(
                                day = day,
                                isToday = today.dayOfMonth == day &&
                                        today.month == month.month &&
                                        today.year == month.year,
                                hasBirthday = contactsToday.isNotEmpty(),
                                onClick = {
                                    contactsToday.firstOrNull()?.let(onContactClick)
                                }
                            )
                            day++
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Day cell ---------- */

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    hasBirthday: Boolean,
    onClick: () -> Unit
) {
    val highlight = Color(0xFF6A4C93)

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> highlight
                    hasBirthday -> highlight.copy(alpha = 0.25f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = hasBirthday) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday || hasBirthday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

/* ---------- Popup ---------- */

@Composable
private fun BirthdayPopup(
    contact: ContactModel,
    onDismiss: () -> Unit
) {
    val birthday = contact.birthday ?: return
    val today = LocalDate.now()
    val next = birthday.withYear(today.year).let {
        if (it.isBefore(today)) it.plusYears(1) else it
    }
    val age = next.year - birthday.year
    val days = ChronoUnit.DAYS.between(today, next)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // 🔑 KEY CHANGE: Box wrapper
            Box {
                // 🎉 Konfetti only if birthday is today
                if (days == 0L) {
                    BirthdayKonfetti()
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = contact.photoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Text(
                        text = "Turning $age 🎂",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        birthday.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
