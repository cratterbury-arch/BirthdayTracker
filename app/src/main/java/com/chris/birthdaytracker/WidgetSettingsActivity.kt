package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var transparency: Int = 60
    private var backgroundEnabled = true
    private var glowEnabled = false

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

        prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        transparency = prefs.getInt("transparency", 60)
        backgroundEnabled = prefs.getBoolean("background_enabled", true)
        glowEnabled = prefs.getBoolean("glow_enabled", false)

        val seek = findViewById<SeekBar>(R.id.transparency_seek)
        seek.progress = transparency

        findViewById<CheckBox>(R.id.enable_background).apply {
            isChecked = backgroundEnabled
            setOnCheckedChangeListener { _, checked -> backgroundEnabled = checked }
        }

        findViewById<CheckBox>(R.id.enable_glow).apply {
            isChecked = glowEnabled
            setOnCheckedChangeListener { _, checked -> glowEnabled = checked }
        }

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                transparency = p
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        findViewById<Button>(R.id.save_button).setOnClickListener {
            prefs.edit()
                .putInt("transparency", transparency)
                .putBoolean("background_enabled", backgroundEnabled)
                .putBoolean("glow_enabled", glowEnabled)
                .apply()

            BirthdayWidgetProvider.refreshAllWidgets(this)

            val resultIntent = Intent()
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}