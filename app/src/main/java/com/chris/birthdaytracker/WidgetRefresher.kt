package com.chris.birthdaytracker

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetRefresher {

    fun refresh(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(BirthdayWidget::class.java)

            ids.forEach { id ->
                BirthdayWidget().update(context, id)
            }
        }
    }
}
