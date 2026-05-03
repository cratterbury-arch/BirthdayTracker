package com.chris.birthdaytracker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
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
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    contacts: List<ContactModel>,
    selectedContact: ContactModel?,
    onContactSelected: (contact: ContactModel) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPopupDismissed: () -> Unit
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()

    val months = remember {
        (-12..12).map { currentMonth.plusMonths(it.toLong()) }
    }

    val currentMonthIndex = months.indexOf(currentMonth).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentMonthIndex)
    val scope = rememberCoroutineScope()

    val showJumpFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex != currentMonthIndex }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            months.forEach { month ->
                item {
                    MonthSection(
                        month = month,
                        contacts = contacts,
                        today = today,
                        onContactClick = onContactSelected,
                        onDateClick = onDateSelected
                    )
                }
            }
        }

        if (showJumpFab) {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(currentMonthIndex)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 24.dp),
                icon = { Icon(Icons.Default.Today, contentDescription = null) },
                text = { Text("Back to Today") }
            )
        }

        selectedContact?.let {
            BirthdayPopup(
                contact = it,
                onDismiss = onPopupDismissed
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
    onContactClick: (contact: ContactModel) -> Unit,
    onDateClick: (LocalDate) -> Unit
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
            onContactClick = onContactClick,
            onDateClick = onDateClick
        )
    }
}

/* ---------- Grid ---------- */

@Composable
private fun CalendarGrid(
    month: YearMonth,
    birthdays: List<ContactModel>,
    today: LocalDate,
    onContactClick: (contact: ContactModel) -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7
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
                            val isToday = today.dayOfMonth == day &&
                                    today.month == month.month &&
                                    today.year == month.year
                            
                            val currentDate = month.atDay(day)

                            DayCell(
                                day = day,
                                isToday = isToday,
                                hasBirthday = contactsToday.isNotEmpty(),
                                onClick = {
                                    if (contactsToday.isNotEmpty()) {
                                        onContactClick(contactsToday.first())
                                    } else {
                                        onDateClick(currentDate)
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
            .clip(CircleShape)
            .background(
                when {
                    isToday -> highlight
                    hasBirthday -> highlight.copy(alpha = 0.25f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
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
    val context = LocalContext.current
    val birthday = contact.birthday ?: return
    val today = LocalDate.now()

    val next = birthday.withYear(today.year).let {
        if (it.isBefore(today)) it.plusYears(1) else it
    }

    val isToday = ChronoUnit.DAYS.between(today, next) == 0L
    val age = next.year - birthday.year

    val confettiEnabled by SettingsStore
        .confettiEnabled(context)
        .collectAsState(initial = true)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (isToday && confettiEnabled) {
                BirthdayCelebrationOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp)
            ) {
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Turning $age 🎂",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = birthday.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
