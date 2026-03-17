package com.chris.birthdaytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.color.ColorProvider
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BirthdayWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp),
            DpSize(200.dp, 140.dp),
            DpSize(250.dp, 200.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ContactsRepository(context)
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val allContacts = if (hasPermission) {
            repository.getAllContacts()
        } else {
            repository.getLocalContacts()
        }

        val today = LocalDate.now()
        
        // Ensure we handle dates correctly even if year is missing (phone contacts)
        val upcoming = allContacts
            .filter { it.birthday != null }
            .map { 
                val bday = it.birthday!!
                // Use a consistent comparison year to avoid logic issues
                val next = bday.withYear(today.year).let { date ->
                    if (date.isBefore(today)) date.plusYears(1) else date
                }
                it to next
            }
            .sortedBy { ChronoUnit.DAYS.between(today, it.second) }

        val nextBirthdays = if (upcoming.isNotEmpty()) {
            val closestDate = upcoming.first().second
            upcoming.filter { it.second.isEqual(closestDate) }.map { it.first }
        } else emptyList()

        provideContent {
            WidgetContent(nextBirthdays)
        }
    }

    @Composable
    private fun WidgetContent(contacts: List<ContactModel>) {
        val size = LocalSize.current

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(6.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            if (contacts.isEmpty()) {
                Text("No birthdays")
                return@Box
            }

            val firstContact = contacts.first()
            val (days, age) = calculate(firstContact)

            val bigSize = when {
                size.width < 150.dp -> 64.sp
                size.width < 230.dp -> 100.sp
                else -> 160.sp
            }

            val daysLabelSize = (bigSize.value * 0.22f).sp
            val frontSize = when {
                size.width < 150.dp -> 14.sp
                size.width < 230.dp -> 18.sp
                else -> 22.sp
            }

            val backgroundColor = ColorProvider(
                day = Color.White.copy(alpha = 0.20f),
                night = Color.White.copy(alpha = 0.20f)
            )

            val frontColor = ColorProvider(
                day = Color.White,
                night = Color.White
            )

            // Background giant number
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (days == 0) "0" else "$days",
                    style = TextStyle(
                        fontSize = bigSize,
                        fontWeight = FontWeight.Normal,
                        color = backgroundColor
                    )
                )

                Text(
                    text = "DAYS",
                    style = TextStyle(
                        fontSize = daysLabelSize,
                        fontWeight = FontWeight.Medium,
                        color = backgroundColor
                    )
                )
            }

            // Foreground content
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val nameText = when {
                    contacts.size == 1 -> {
                        val name = extractFirstName(contacts[0].name)
                        if (days == 0) "$name is $age today" else "$name is $age in"
                    }
                    contacts.size == 2 -> {
                        val name1 = extractFirstName(contacts[0].name)
                        val name2 = extractFirstName(contacts[1].name)
                        if (days == 0) "$name1 & $name2 today!" else "$name1 & $name2 in"
                    }
                    else -> {
                        if (days == 0) "${contacts.size} birthdays today!" else "${contacts.size} birthdays in"
                    }
                }

                Text(
                    text = nameText,
                    style = TextStyle(
                        fontSize = frontSize,
                        fontWeight = FontWeight.Medium,
                        color = frontColor,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }

    private fun extractFirstName(fullName: String): String {
        // More aggressive split to handle various whitespace characters
        return fullName.trim()
            .split(Regex("[\\s\\u00A0\\t\\r\\n]+"))
            .firstOrNull { it.isNotEmpty() } ?: fullName
    }

    private fun calculate(contact: ContactModel): Pair<Int, Int> {
        val today = LocalDate.now()
        val birthday = contact.birthday!!
        val nextBirthday = birthday.withYear(today.year).let { if (it.isBefore(today)) it.plusYears(1) else it }
        val days = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
        val age = nextBirthday.year - birthday.year
        return Pair(days, age)
    }
}
