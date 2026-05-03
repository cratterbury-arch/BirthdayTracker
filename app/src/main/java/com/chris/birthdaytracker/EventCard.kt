package com.chris.birthdaytracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun EventCard(
    event: EventModel,
    onToggleFavorite: (Boolean) -> Unit = {}
) {
    val today = LocalDate.now()
    val eventDate = event.date

    // Logic for recurring events (like anniversaries) or one-off events
    // For now, let's assume they might be recurring if we want "days until" to always be positive
    var nextDate = eventDate.withYear(today.year)
    if (nextDate.isBefore(today)) {
        nextDate = nextDate.plusYears(1)
    }
    
    val daysToGo = ChronoUnit.DAYS.between(today, nextDate).toInt()
    val isToday = daysToGo == 0

    val cardModifier = if (isToday) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(event.type.lowercase()) {
                        "anniversary" -> Icons.Default.Favorite
                        "pet" -> Icons.Default.Pets
                        "show" -> Icons.Default.ConfirmationNumber
                        "christening" -> Icons.Default.ChildCare
                        else -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 72.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onToggleFavorite(!event.isFavorite) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (event.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (event.isFavorite) Color.Red else LocalContentColor.current,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = event.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = eventDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isToday) {
                Text("TODAY!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                DaysCircle(daysToGo)
            }
        }
    }
}

@Composable
private fun DaysCircle(days: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = days.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "days", fontSize = 10.sp)
        }
    }
}
