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

        val contacts = if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ContactsRepository(context).getAllContacts()
        } else emptyList()

        val today = LocalDate.now()

        val nextContact = contacts
            .filter { it.birthday != null }
            .minByOrNull { contact ->
                val next = contact.birthday!!
                    .withYear(today.year)
                    .let { if (it.isBefore(today)) it.plusYears(1) else it }
                ChronoUnit.DAYS.between(today, next)
            }

        provideContent {
            WidgetContent(nextContact)
        }
    }

    @Composable
    private fun WidgetContent(contact: ContactModel?) {

        val size = LocalSize.current

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(6.dp) // minimal padding
                .clickable(actionStartActivity<MainActivity>())
        ) {

            if (contact == null) {
                Text("No birthdays")
                return@Box
            }

            val (days, age) = calculate(contact)

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

            // Foreground elegant line
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = if (days == 0)
                        "${contact.name} is $age today"
                    else
                        "${contact.name} is $age in",
                    style = TextStyle(
                        fontSize = frontSize,
                        fontWeight = FontWeight.Medium,
                        color = frontColor
                    )
                )
            }
        }
    }
}

private fun calculate(contact: ContactModel): Pair<Int, Int> {

    val today = LocalDate.now()
    val birthday = contact.birthday!!

    val nextBirthday = birthday.withYear(today.year)
        .let { if (it.isBefore(today)) it.plusYears(1) else it }

    val days = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
    val age = nextBirthday.year - birthday.year

    return Pair(days, age)
}