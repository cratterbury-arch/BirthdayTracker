package com.chris.birthdaytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

class BirthdayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {

            val hasContactsPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!hasContactsPermission) {
                    Text(
                        text = "🔒 Open app to allow contacts",
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white)
                        )
                    )
                    return@Column
                }

                val today = LocalDate.now()

                val contacts = try {
                    BirthdayRepository.getContactsWithBirthday(context)
                } catch (e: Exception) {
                    emptyList()
                }

                if (contacts.isEmpty()) {
                    Text(
                        text = "🎂 No birthdays found",
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white)
                        )
                    )
                    return@Column
                }

                // 🎉 Priority 1: Birthday today
                val todayBirthday = contacts.firstOrNull {
                    it.isBirthdayOn(today)
                }

                if (todayBirthday != null) {
                    Text(
                        text = "🎉 Today’s Birthday",
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white)
                        )
                    )

                    Text(
                        text = todayBirthday.displayName,
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white)
                        )
                    )

                    todayBirthday.ageToday()?.let {
                        Text(
                            text = "Turning $it",
                            style = TextStyle(
                                color = ColorProvider(android.R.color.white)
                            )
                        )
                    }

                } else {
                    // ⏭️ Priority 2: Next upcoming birthday
                    val next = contacts
                        .mapNotNull { contact ->
                            contact.daysUntilBirthday(today)?.let {
                                contact to it
                            }
                        }
                        .minByOrNull { it.second }

                    if (next == null) {
                        Text(
                            text = "🎂 No upcoming birthdays",
                            style = TextStyle(
                                color = ColorProvider(android.R.color.white)
                            )
                        )
                    } else {
                        val (contact, days) = next

                        Text(
                            text = "🎉 Next Birthday",
                            style = TextStyle(
                                color = ColorProvider(android.R.color.white)
                            )
                        )

                        Text(
                            text = contact.displayName,
                            style = TextStyle(
                                color = ColorProvider(android.R.color.white)
                            )
                        )

                        Text(
                            text = "In $days days",
                            style = TextStyle(
                                color = ColorProvider(android.R.color.white)
                            )
                        )

                        contact.ageOnNextBirthday()?.let {
                            Text(
                                text = "Turning $it",
                                style = TextStyle(
                                    color = ColorProvider(android.R.color.white)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
