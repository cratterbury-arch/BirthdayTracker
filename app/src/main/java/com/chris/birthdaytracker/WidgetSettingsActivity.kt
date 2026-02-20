package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_settings)

        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(Activity.RESULT_CANCELED)

        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {

            // Example save (we’ll expand shortly)
            val prefs = getSharedPreferences("widget_$widgetId", Context.MODE_PRIVATE)

            prefs.edit()
                .putInt("number_color", 0xFFFFFFFF.toInt())
                .putInt("text_color", 0xFFFFFFFF.toInt())
                .putInt("transparency", 60)
                .putBoolean("background_enabled", true)
                .putBoolean("glow_enabled", false)
                .apply()

            BirthdayWidgetProvider.updateWidget(
                this,
                AppWidgetManager.getInstance(this),
                widgetId
            )

            val resultIntent = Intent()
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultIntent)

            finish()
        }
    }
}