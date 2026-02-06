package com.chris.birthdaytracker

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text
import java.time.LocalDate

class BirthdayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        val nextBirthday = BirthdayData(
            name = "Next birthday",
            date = LocalDate.now().plusDays(5)
        )

        Column(modifier = GlanceModifier) {
            Text(text = "🎂 Upcoming Birthday")
            Text(text = nextBirthday.name)
            Text(text = "${nextBirthday.date.daysUntilNextBirthday()} days to go")
        }
    }
}

class BirthdayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BirthdayWidget()
}
