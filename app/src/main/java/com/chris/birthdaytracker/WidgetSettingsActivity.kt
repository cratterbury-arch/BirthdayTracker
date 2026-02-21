package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WidgetSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var previewImage: ImageView

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var numberColor = Color.WHITE
    private var textColor = Color.WHITE
    private var numberGlow = false
    private var textGlow = false
    private var backgroundEnabled = true
    private var transparency = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_settings)

        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        setResult(Activity.RESULT_OK, Intent().putExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            widgetId
        ))

        prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        previewImage = findViewById(R.id.preview_image)

        val numberContainer = findViewById<LinearLayout>(R.id.number_color_container)
        val textContainer = findViewById<LinearLayout>(R.id.text_color_container)
        val numberGlowToggle = findViewById<CheckBox>(R.id.enable_number_glow)
        val textGlowToggle = findViewById<CheckBox>(R.id.enable_text_glow)
        val bgToggle = findViewById<CheckBox>(R.id.enable_background)
        val transparencySeek = findViewById<SeekBar>(R.id.transparency_seek)

        val swatches = listOf(
            Color.WHITE, Color.BLACK, Color.RED, Color.BLUE,
            Color.GREEN, Color.CYAN, Color.MAGENTA, Color.YELLOW
        )

        swatches.forEach { color ->
            numberContainer.addView(createSwatch(color) {
                numberColor = color
                applyChanges()
            })

            textContainer.addView(createSwatch(color) {
                textColor = color
                applyChanges()
            })
        }

        numberGlowToggle.setOnCheckedChangeListener { _, isChecked ->
            numberGlow = isChecked
            applyChanges()
        }

        textGlowToggle.setOnCheckedChangeListener { _, isChecked ->
            textGlow = isChecked
            applyChanges()
        }

        bgToggle.setOnCheckedChangeListener { _, isChecked ->
            backgroundEnabled = isChecked
            applyChanges()
        }

        transparencySeek.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                transparency = progress
                applyChanges()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        applyChanges()
    }

    private fun applyChanges() {
        prefs.edit()
            .putInt("number_color", numberColor)
            .putInt("text_color", textColor)
            .putBoolean("number_glow", numberGlow)
            .putBoolean("text_glow", textGlow)
            .putBoolean("background_enabled", backgroundEnabled)
            .putInt("transparency", transparency)
            .apply()

        BirthdayWidgetProvider.refreshAllWidgets(this)
        previewImage.setImageBitmap(
            BirthdayWidgetProvider.generatePreviewBitmap(this)
        )
    }

    private fun createSwatch(color: Int, onClick: () -> Unit): ImageView {
        val size = 100
        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                rightMargin = 16
            }
            setBackgroundColor(color)
            setOnClickListener { onClick() }
        }
    }
}