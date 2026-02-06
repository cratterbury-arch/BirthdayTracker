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

            // 🔐 Permission check (CRITICAL)
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

                val birthdaysToday = try {
                    BirthdayRepository
                        .getContactsWithBirthday(context)
                        .filter { it.isBirthdayOn(today) }
                } catch (e: Exception) {
                    emptyList()
                }

                if (birthdaysToday.isEmpty()) {
                    Text(
                        text = "🎂 No birthdays today",
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white)
                        )
                    )
                } else {
                    val contact = birthdaysToday.first()

                    Text(
                        text = "🎉 Today’s Birthday",
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

                    contact.ageToday()?.let {
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
