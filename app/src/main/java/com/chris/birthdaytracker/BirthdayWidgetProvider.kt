package com.chris.birthdaytracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BirthdayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                for (id in appWidgetIds) {
                    updateWidget(context, manager, id)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, BirthdayWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)

            CoroutineScope(Dispatchers.Main).launch {
                for (id in ids) {
                    updateWidget(context, manager, id)
                }
            }
        }

        suspend fun generatePreviewBitmap(context: Context): Bitmap {
            return renderWidget(context, 800, 500)
        }

        private suspend fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val options = manager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            
            // Generate bitmap based on widget size to ensure it scales correctly
            val width = (minWidth * density * 2.5f).toInt().coerceAtLeast(300)
            val height = (minHeight * density * 2.5f).toInt().coerceAtLeast(200)

            val bitmap = renderWidget(context, width, height)

            val views = RemoteViews(context.packageName, R.layout.birthday_widget)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val settingsIntent = Intent(context, WidgetSettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }

        private fun getAlphaColor(prefs: SharedPreferences, colorKey: String, alphaKey: String, defaultColor: Int): Int {
            val baseColor = prefs.getInt(colorKey, defaultColor)
            val alpha = prefs.getInt(alphaKey, 255)
            return Color.argb(
                alpha,
                Color.red(baseColor),
                Color.green(baseColor),
                Color.blue(baseColor)
            )
        }

        private fun extractFirstName(fullName: String): String {
            return fullName.trim()
                .split(Regex("[\\s\\u00A0\\t\\r\\n]+"))
                .firstOrNull { it.isNotEmpty() } ?: fullName
        }

        private suspend fun renderWidget(
            context: Context,
            width: Int,
            height: Int
        ): Bitmap = withContext(Dispatchers.IO) {

            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

            val numberColor = getAlphaColor(prefs, "number_color", "number_alpha", Color.WHITE)
            val textColor = getAlphaColor(prefs, "text_color", "text_alpha", Color.WHITE)
            
            val transparency = prefs.getInt("transparency", 70)
            val backgroundEnabled = prefs.getBoolean("background_enabled", true)
            
            val numberGlowEnabled = prefs.getBoolean("number_glow", false)
            val textGlowEnabled = prefs.getBoolean("text_glow", false)
            
            val numberRadius = (prefs.getInt("number_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)
            val textRadius = (prefs.getInt("text_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (backgroundEnabled) {
                val alpha = (transparency / 100f * 255).toInt()
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(alpha, 30, 30, 30)
                }
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 80f, 80f, bgPaint)
            }

            val today = LocalDate.now()
            val contactsRepo = ContactsRepository(context)
            val allContacts = contactsRepo.getAllContacts().filter { it.birthday != null }
            
            val upcoming = allContacts.map { contact ->
                val bday = contact.birthday!!
                val next = bday.withYear(today.year).let { if (it.isBefore(today)) it.plusYears(1) else it }
                contact to next
            }.sortedBy { ChronoUnit.DAYS.between(today, it.second) }

            val nextBirthdays = if (upcoming.isNotEmpty()) {
                val closestDate = upcoming.first().second
                upcoming.filter { it.second.isEqual(closestDate) }
            } else emptyList()

            val scriptTypeface = ResourcesCompat.getFont(context, R.font.sacramento)
            val centerX = width / 2f
            val centerY = height / 2f

            if (nextBirthdays.isEmpty()) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = height * 0.1f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("No Birthdays", centerX, centerY, paint)
                return@withContext bitmap
            }

            val days = ChronoUnit.DAYS.between(today, nextBirthdays.first().second).toInt()
            val isToday = days == 0

            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = numberColor
                textAlign = Paint.Align.CENTER
                typeface = scriptTypeface
                if (isToday) {
                    textSize = height * 0.45f
                } else {
                    textSize = height * 0.65f
                }
                if (numberGlowEnabled) {
                    val glowColor = prefs.getInt("number_glow_color", prefs.getInt("number_color", Color.WHITE))
                    setShadowLayer(numberRadius, 0f, 0f, glowColor)
                }
            }

            // Auto-scale "TODAY" or the number if they're too wide
            val maxTextWidth = width * 0.85f
            val textToMeasure = if (isToday) "TODAY" else days.toString()
            var textWidth = numberPaint.measureText(textToMeasure)
            while (textWidth > maxTextWidth && numberPaint.textSize > 10f) {
                numberPaint.textSize -= 2f
                textWidth = numberPaint.measureText(textToMeasure)
            }

            val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = if (isToday) height * 0.22f else height * 0.14f
                textAlign = Paint.Align.CENTER
                typeface = scriptTypeface
                if (textGlowEnabled) {
                   val glowColor = prefs.getInt("text_glow_color", prefs.getInt("text_color", Color.WHITE))
                   setShadowLayer(textRadius, 0f, 0f, glowColor)
                }
            }

            val nameText = when {
                nextBirthdays.size == 1 -> {
                    val contact = nextBirthdays[0].first
                    val age = nextBirthdays[0].second.year - contact.birthday!!.year
                    val firstName = extractFirstName(contact.name)
                    if (isToday) "$firstName is $age" else "$firstName is $age in"
                }
                nextBirthdays.size == 2 -> {
                    val c1 = nextBirthdays[0].first
                    val c2 = nextBirthdays[1].first
                    val n1 = extractFirstName(c1.name)
                    val n2 = extractFirstName(c2.name)
                    if (isToday) "$n1 & $n2" else "$n1 & $n2 in"
                }
                else -> {
                    if (isToday) "${nextBirthdays.size} birthdays" else "${nextBirthdays.size} birthdays in"
                }
            }

            // Auto-scale nameText if it's too wide
            val maxScriptNameWidth = width * 0.9f
            var scriptNameWidth = scriptPaint.measureText(nameText)
            while (scriptNameWidth > maxScriptNameWidth && scriptPaint.textSize > 10f) {
                scriptPaint.textSize -= 2f
                scriptNameWidth = scriptPaint.measureText(nameText)
            }

            if (isToday) {
                val todayY = centerY + (numberPaint.textSize * 0.35f)
                val nameY = todayY - (numberPaint.textSize * 0.85f)
                canvas.drawText(nameText, centerX, nameY, scriptPaint)
                canvas.drawText("TODAY", centerX, todayY, numberPaint)
            } else {
                canvas.drawText(days.toString(), centerX, centerY + (height * 0.65f) / 3, numberPaint)
                val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = numberColor
                    textSize = height * 0.08f
                    textAlign = Paint.Align.CENTER
                    typeface = scriptTypeface
                    if (numberGlowEnabled) {
                        val glowColor = prefs.getInt("number_glow_color", prefs.getInt("number_color", Color.WHITE))
                        setShadowLayer(numberRadius, 0f, 0f, glowColor)
                    }
                }
                canvas.drawText("DAYS", centerX, centerY + (height * 0.65f) / 3 + daysPaint.textSize * 1.1f, daysPaint)
                canvas.drawText(nameText, centerX, centerY + (height * 0.65f) / 12f, scriptPaint)
            }

            bitmap
        }
    }
}
