package com.chris.birthdaytracker

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NextBirthdayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NextBirthdayWidget()
}
