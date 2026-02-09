package com.chris.birthdaytracker

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val today = LocalDate.now()
    val months = remember {
        // 12 months before → 12 months after
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
                    today = today
                )
            }
        }
    }
}

/* ---------- Month ---------- */

@Composable
private fun MonthSection(
    month: YearMonth,
    contacts: List<ContactModel>,
    today: LocalDate
) {
    val birthdaysThisMonth = remember(contacts, month) {
        contacts
            .mapNotNull { it.birthday }
            .filter { it.month == month.month }
            .map { it.dayOfMonth }
            .toSet()
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
            today = today
        )
    }
}

/* ---------- Grid ---------- */

@Composable
private fun CalendarGrid(
    month: YearMonth,
    birthdays: Set<Int>,
    today: LocalDate
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = (firstDay.dayOfWeek.value % 7)

    Column {
        // Weekday headers
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DayOfWeek.values().forEach {
                Text(
                    text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        var day = 1
        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { column ->
                    val index = it * 7 + column
                    if (index < startOffset || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(40.dp))
                    } else {
                        DayCell(
                            day = day,
                            isToday = today.dayOfMonth == day &&
                                    today.month == month.month &&
                                    today.year == month.year,
                            hasBirthday = birthdays.contains(day)
                        )
                        day++
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
    hasBirthday: Boolean
) {
    val highlight = Color(0xFF6A4C93)

    Box(
        modifier = Modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
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
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || hasBirthday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
