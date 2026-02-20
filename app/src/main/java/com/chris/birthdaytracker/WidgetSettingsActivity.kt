package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_settings)

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        prefs = getSharedPreferences("widget_$widgetId", Context.MODE_PRIVATE)

        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {

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

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)

            finish()
        }
    }
}