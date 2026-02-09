package com.chris.birthdaytracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val today = LocalDate.now()

    var selectedContact by remember { mutableStateOf<ContactModel?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val months = remember {
        (-12..12).map { YearMonth.now().plusMonths(it.toLong()) }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(YearMonth.now()).coerceAtLeast(0)
    )

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
                    onBirthdayClick = { contact, date ->
                        selectedContact = contact
                        selectedDate = date
                    }
                )
            }
        }
    }

    /* ---------- Bottom sheet ---------- */

    if (selectedContact != null && selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedContact = null
                selectedDate = null
            }
        ) {
            BirthdayDetailSheet(
                contact = selectedContact!!,
                date = selectedDate!!
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
    onBirthdayClick: (ContactModel, LocalDate) -> Unit
) {
    val birthdaysByDay = remember(contacts, month) {
        contacts
            .filter { it.birthday?.month == month.month }
            .groupBy { it.birthday!!.dayOfMonth }
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
            today = today,
            birthdaysByDay = birthdaysByDay,
            onBirthdayClick = onBirthdayClick
        )
    }
}

/* ---------- Grid ---------- */

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    birthdaysByDay: Map<Int, List<ContactModel>>,
    onBirthdayClick: (ContactModel, LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7

    Column {
        Row(Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach {
                Text(
                    text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        var day = 1

        repeat(6) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val index = row * 7 + column

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index >= startOffset && day <= daysInMonth) {
                            val date = month.atDay(day)
                            val birthdays = birthdaysByDay[day]

                            DayCell(
                                day = day,
                                isToday = date == today,
                                hasBirthday = birthdays != null,
                                onClick = {
                                    birthdays?.firstOrNull()?.let {
                                        onBirthdayClick(it, date)
                                    }
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
            .background(
                when {
                    isToday -> highlight
                    hasBirthday -> highlight.copy(alpha = 0.25f)
                    else -> Color.Transparent
                },
                CircleShape
            )
            .then(
                if (hasBirthday) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday || hasBirthday) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/* ---------- Bottom sheet content ---------- */

@Composable
private fun BirthdayDetailSheet(
    contact: ContactModel,
    date: LocalDate
) {
    val birthday = contact.birthday ?: return
    val age = date.year - birthday.year

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = contact.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Turning $age 🎂",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Born ${birthday.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
    }
}
