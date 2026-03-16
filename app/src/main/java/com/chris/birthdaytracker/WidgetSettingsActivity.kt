package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var preview: ImageView
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.widget_settings)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        preview = findViewById(R.id.preview_image)

        // Colour Rows
        setupColourRow(findViewById(R.id.number_color_container), "number_color")
        setupColourRow(findViewById(R.id.text_color_container), "text_color")
        setupColourRow(findViewById(R.id.number_glow_color_container), "number_glow_color")
        setupColourRow(findViewById(R.id.text_glow_color_container), "text_glow_color")

        // Checkboxes
        bindCheckBox(R.id.enable_number_glow, "number_glow")
        bindCheckBox(R.id.enable_text_glow, "text_glow")
        bindCheckBox(R.id.enable_background, "background_enabled")

        // Transparency Sliders
        bindSeekBar(R.id.number_alpha_seek, "number_alpha", 255)
        bindSeekBar(R.id.text_alpha_seek, "text_alpha", 255)
        bindSeekBar(R.id.transparency_seek, "transparency", 60)

        // Intensity Sliders
        bindSeekBar(R.id.number_glow_intensity_seek, "number_glow_intensity", 50)
        bindSeekBar(R.id.text_glow_intensity_seek, "text_glow_intensity", 50)

        findViewById<Button>(R.id.apply_button).setOnClickListener {
            BirthdayWidgetProvider.refreshAllWidgets(this)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        updatePreview()
    }

    private fun bindCheckBox(id: Int, key: String) {
        val box = findViewById<CheckBox>(id)
        box.isChecked = prefs.getBoolean(key, false)
        box.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(key, isChecked).apply()
            BirthdayWidgetProvider.refreshAllWidgets(this@WidgetSettingsActivity)
            updatePreview()
        }
    }

    private fun bindSeekBar(id: Int, key: String, default: Int) {
        val bar = findViewById<SeekBar>(id)
        bar.progress = prefs.getInt(key, default)
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt(key, progress).apply()
                BirthdayWidgetProvider.refreshAllWidgets(this@WidgetSettingsActivity)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupColourRow(container: LinearLayout, key: String) {
        val colours = listOf(
            0xFFFFFFFF.toInt(), 0xFF000000.toInt(),
            0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(),
            0xFFFFFF00.toInt(), 0xFFFF9800.toInt(), 0xFF9C27B0.toInt(),
            0xFF00BCD4.toInt(), 0xFF3F51B5.toInt(), 0xFFFFC107.toInt(),
            0xFFE91E63.toInt(), 0xFFB39DDB.toInt(), 0xFF80CBC4.toInt(),
            0xFFFFAB91.toInt(), 0xFFA5D6A7.toInt(), 0xFF90CAF9.toInt()
        )

        val selected = prefs.getInt(key, colours.first())
        container.removeAllViews()

        colours.forEach { colour ->
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(colour)
                setStroke(6, Color.BLACK)
                if (colour == selected) setStroke(12, Color.parseColor("#6200EE"))
            }

            val swatch = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply { setMargins(20, 20, 20, 20) }
                background = drawable
                setOnClickListener {
                    prefs.edit().putInt(key, colour).apply()
                    BirthdayWidgetProvider.refreshAllWidgets(this@WidgetSettingsActivity)
                    updatePreview()
                    setupColourRow(container, key)
                }
            }
            container.addView(swatch)
        }
    }

    private fun updatePreview() {
        preview.setImageBitmap(BirthdayWidgetProvider.generatePreviewBitmap(this))
    }
}
