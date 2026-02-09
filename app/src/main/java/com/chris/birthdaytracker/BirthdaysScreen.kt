package com.chris.birthdaytracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Composable
fun BirthdaysScreen(
    contacts: List<ContactModel>
) {
    val today = LocalDate.now()
    val context = LocalContext.current

    // 🎂 Birthdays today
    val birthdaysToday = remember(contacts) {
        contacts.filter {
            it.birthday?.month == today.month &&
                    it.birthday?.dayOfMonth == today.dayOfMonth
        }
    }

    // 🔊 Sound — fires once when entering screen if there is a birthday today
    LaunchedEffect(birthdaysToday.isNotEmpty()) {
        if (birthdaysToday.isNotEmpty()) {
            playBirthdaySound(context)
        }
    }

    val sortedContacts = contacts
        .filter { it.birthday != null }
        .sortedBy { contact ->
            val next = contact.birthday!!
                .withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }

            ChronoUnit.DAYS.between(today, next)
        }

    Box(modifier = Modifier.fillMaxSize()) {

        // 📜 Birthday list / empty state
        if (sortedContacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No upcoming birthdays")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(sortedContacts) { contact ->
                    UpcomingBirthdayCard(contact = contact)
                }
            }
        }

        // 🎉 CONFETTI — ON TOP OF EVERYTHING
        if (birthdaysToday.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f), // ⬅️ THIS is the critical fix
                parties = listOf(


                    // 🎊 MAIN CELEBRATION BURST (top center)
                    Party(
                        speed = 10f,
                        maxSpeed = 45f,
                        damping = 0.85f,
                        spread = 360,
                        colors = listOf(
                            0xfce18a,
                            0xff726d,
                            0xf4306d,
                            0xb48def,
                            0x6A4C93
                        ),
                        emitter = Emitter(
                            duration = 2,
                            TimeUnit.SECONDS
                        ).perSecond(220), // 🔥 MORE CONFETTI
                        position = Position.Relative(0.5, 0.0)
                    ),

                    // 🎊 LEFT SIDE SPRAY
                    Party(
                        speed = 8f,
                        maxSpeed = 35f,
                        damping = 0.9f,
                        spread = 120,
                        colors = listOf(0xff726d, 0xf4306d, 0xb48def),
                        emitter = Emitter(
                            duration = 2,
                            TimeUnit.SECONDS
                        ).perSecond(120),
                        position = Position.Relative(0.0, 0.3)
                    ),

                    // 🎊 RIGHT SIDE SPRAY
                    Party(
                        speed = 8f,
                        maxSpeed = 35f,
                        damping = 0.9f,
                        spread = 90,
                        angle = 180,
                        colors = listOf(0xff726d, 0xf4306d, 0xb48def),
                        emitter = Emitter(
                            duration = 2,
                            TimeUnit.SECONDS
                        ).perSecond(140),
                        position = Position.Relative(1.0, 0.3)
                    )
                )
            )
        }
    }
}
