package com.chris.birthdaytracker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val presetColors = listOf(
        Color.WHITE,
        Color.BLACK,
        Color.parseColor("#FF3B30"),
        Color.parseColor("#34C759"),
        Color.parseColor("#007AFF"),
        Color.parseColor("#AF52DE"),
        Color.parseColor("#FF9500"),
        Color.parseColor("#FFD60A")
    )

    private var numberColor = Color.WHITE
    private var textColor = Color.WHITE
    private var numberGlowColor = Color.WHITE
    private var textGlowColor = Color.WHITE
    private var numberGlow = false
    private var textGlow = false
    private var numberAlpha = 255
    private var textAlpha = 255
    private var transparency = 60
    private var backgroundEnabled = true

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

        prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        // Load preferences
        numberColor = prefs.getInt("number_color", Color.WHITE)
        textColor = prefs.getInt("text_color", Color.WHITE)
        numberGlowColor = prefs.getInt("number_glow_color", numberColor)
        textGlowColor = prefs.getInt("text_glow_color", textColor)
        numberGlow = prefs.getBoolean("number_glow", false)
        textGlow = prefs.getBoolean("text_glow", false)
        numberAlpha = prefs.getInt("number_alpha", 255)
        textAlpha = prefs.getInt("text_alpha", 255)
        transparency = prefs.getInt("transparency", 60)
        backgroundEnabled = prefs.getBoolean("background_enabled", true)

        val numberContainer = findViewById<LinearLayout>(R.id.number_color_container)
        val textContainer = findViewById<LinearLayout>(R.id.text_color_container)
        val numberGlowContainer = findViewById<LinearLayout>(R.id.number_glow_container)
        val textGlowContainer = findViewById<LinearLayout>(R.id.text_glow_container)

        createSwatches(numberContainer, numberColor) { numberColor = it }
        createSwatches(textContainer, textColor) { textColor = it }
        createSwatches(numberGlowContainer, numberGlowColor) { numberGlowColor = it }
        createSwatches(textGlowContainer, textGlowColor) { textGlowColor = it }

        findViewById<CheckBox>(R.id.enable_number_glow).apply {
            isChecked = numberGlow
            setOnCheckedChangeListener { _, isChecked -> numberGlow = isChecked }
        }

        findViewById<CheckBox>(R.id.enable_text_glow).apply {
            isChecked = textGlow
            setOnCheckedChangeListener { _, isChecked -> textGlow = isChecked }
        }

        findViewById<SeekBar>(R.id.number_alpha_seek).apply {
            progress = numberAlpha
            max = 255
            setOnSeekBarChangeListener(simpleSeek { numberAlpha = it })
        }

        findViewById<SeekBar>(R.id.text_alpha_seek).apply {
            progress = textAlpha
            max = 255
            setOnSeekBarChangeListener(simpleSeek { textAlpha = it })
        }

        findViewById<CheckBox>(R.id.enable_background).apply {
            isChecked = backgroundEnabled
            setOnCheckedChangeListener { _, isChecked -> backgroundEnabled = isChecked }
        }

        findViewById<SeekBar>(R.id.transparency_seek).apply {
            progress = transparency
            max = 100
            setOnSeekBarChangeListener(simpleSeek { transparency = it })
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {

            prefs.edit()
                .putInt("number_color", numberColor)
                .putInt("text_color", textColor)
                .putInt("number_glow_color", numberGlowColor)
                .putInt("text_glow_color", textGlowColor)
                .putBoolean("number_glow", numberGlow)
                .putBoolean("text_glow", textGlow)
                .putInt("number_alpha", numberAlpha)
                .putInt("text_alpha", textAlpha)
                .putInt("transparency", transparency)
                .putBoolean("background_enabled", backgroundEnabled)
                .apply()

            BirthdayWidgetProvider.refreshAllWidgets(this)

            // Detect if launched as widget configuration
            val resultIntent = Intent()
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultIntent)

            // Go to home screen explicitly
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)

            finish()
        }
    }

    private fun simpleSeek(onChange: (Int) -> Unit) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onChange(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

    private fun createSwatches(
        container: LinearLayout,
        selectedColor: Int,
        onSelect: (Int) -> Unit
    ) {
        container.removeAllViews()

        presetColors.forEach { color ->

            val swatch = View(this)
            val size = (40 * resources.displayMetrics.density).toInt()

            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(12, 12, 12, 12)
            swatch.layoutParams = params

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(color)
                setStroke(
                    if (color == selectedColor) 6 else 4,
                    if (color == selectedColor) Color.GRAY else Color.TRANSPARENT
                )
            }

            swatch.background = drawable

            swatch.setOnClickListener {
                onSelect(color)
                highlightSelection(container, color)
            }

            container.addView(swatch)
        }
    }

    private fun highlightSelection(container: LinearLayout, selectedColor: Int) {
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val drawable = view.background as GradientDrawable
            val color = presetColors[i]

            if (color == selectedColor) {
                drawable.setStroke(6, Color.GRAY)
            } else {
                drawable.setStroke(4, Color.TRANSPARENT)
            }
        }
    }
}