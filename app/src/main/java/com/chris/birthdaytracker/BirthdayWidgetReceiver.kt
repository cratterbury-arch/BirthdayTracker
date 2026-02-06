package com.chris.birthdaytracker

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BirthdayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BirthdayWidget()
}
