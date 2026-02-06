package com.chris.birthdaytracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val birthdayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun CalendarScreen(
    contacts: List<ContactModel>
) {
    val months = generateMonths()
    val today = LocalDate.now()

    val currentMonthIndex = months.indexOfFirst {
        it.month == today.month && it.year == today.year
    }.coerceAtLeast(0)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = currentMonthIndex
    )

    val scope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val isCurrentMonthVisible by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any {
                it.index == currentMonthIndex
            }
        }
    }

    Box {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(months) { _, month ->
                MonthView(
                    month = month,
                    contacts = contacts,
                    today = today,
                    onDaySelected = { selectedDate = it }
                )
            }
        }

        if (!isCurrentMonthVisible) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(currentMonthIndex)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Today, contentDescription = "Jump to today")
            }
        }

        selectedDate?.let { date ->
            BirthdayDialog(
                date = date,
                contacts = contacts,
                onDismiss = { selectedDate = null }
            )
        }
    }
}

@Composable
fun MonthView(
    month: YearMonth,
    contacts: List<ContactModel>,
    today: LocalDate,
    onDaySelected: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    val birthdayDays = contacts.mapNotNull { contact ->
        contact.birthday?.let {
            val birthDate = LocalDate.parse(it, birthdayFormatter)
            if (birthDate.month == month.month) birthDate.dayOfMonth else null
        }
    }.toSet()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    " ${month.year}",
            style = MaterialTheme.typography.titleLarge
        )

        DaysOfWeekHeader()

        val offset = (firstDay.dayOfWeek.value % 7)
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        var day = 1

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) { column ->
                        val index = it * 7 + column

                        if (index < offset || day > daysInMonth) {
                            Spacer(modifier = Modifier.size(40.dp))
                        } else {
                            val date = month.atDay(day)
                            val isToday = date == today
                            val hasBirthday = birthdayDays.contains(day)

                            DayCell(
                                day = day,
                                isToday = isToday,
                                hasBirthday = hasBirthday,
                                onClick = {
                                    if (hasBirthday) onDaySelected(date)
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

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
            Text(
                text = it,
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    hasBirthday: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        hasBirthday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday)
                    MaterialTheme.colorScheme.primary
                else
                    bg,
                shape = MaterialTheme.shapes.small
            )
            .then(
                if (hasBirthday) Modifier.clickable { onClick() }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isToday)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BirthdayDialog(
    date: LocalDate,
    contacts: List<ContactModel>,
    onDismiss: () -> Unit
) {
    val birthdays = contacts.filter { contact ->
        contact.birthday?.let {
            val birthDate = LocalDate.parse(it, birthdayFormatter)
            birthDate.dayOfMonth == date.dayOfMonth &&
                    birthDate.month == date.month
        } == true
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    birthdays.forEach { contact ->
                        val age = contact.ageOnNextBirthday(date)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = contact.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            age?.let {
                                Text(
                                    text = "Turning $it",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            contact.photoUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }

                            contact.birthday?.let {
                                Text(text = "DOB: $it")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateMonths(): List<YearMonth> {
    val start = YearMonth.now().minusMonths(6)
    return (0..18).map { start.plusMonths(it.toLong()) }
}
