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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BirthdayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
    }

    companion object {

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, BirthdayWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)

            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {

            val options = manager.getAppWidgetOptions(widgetId)
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val density = context.resources.displayMetrics.density
            val width = (widthDp * density).toInt().coerceAtLeast(300)
            val height = (heightDp * density).toInt().coerceAtLeast(250)

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

        private fun renderWidget(
            context: Context,
            width: Int,
            height: Int
        ): Bitmap {

            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

            val numberColor = getAlphaColor(prefs, "number_color", "number_alpha", Color.WHITE)
            val textColor = getAlphaColor(prefs, "text_color", "text_alpha", Color.WHITE)
            
            val transparency = prefs.getInt("transparency", 70)
            val backgroundEnabled = prefs.getBoolean("background_enabled", true)
            
            val numberGlowEnabled = prefs.getBoolean("number_glow", false)
            val textGlowEnabled = prefs.getBoolean("text_glow", false)
            
            // Scaling intensity for better visibility (0-100 range -> 0.1-25 radius)
            val numberRadius = (prefs.getInt("number_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)
            val textRadius = (prefs.getInt("text_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (backgroundEnabled) {
                val alpha = (transparency / 100f * 255).toInt()
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(alpha, 30, 30, 30)
                }

                canvas.drawRoundRect(
                    0f, 0f, width.toFloat(), height.toFloat(), 80f, 80f, bgPaint
                )
            }

            val today = LocalDate.now()
            val contactsRepo = ContactsRepository(context)
            val contacts = contactsRepo.getAllContacts().filter { it.birthday != null }
            
            val nearestBirthday = contacts.mapNotNull { contact ->
                val birthDate = contact.birthday!!
                var next = birthDate.withYear(today.year)
                if (next.isBefore(today)) {
                    next = next.withYear(today.year + 1)
                }
                val age = next.year - birthDate.year
                Triple(contact.name, next, age)
            }.minByOrNull { ChronoUnit.DAYS.between(today, it.second) }

            val (name, nextDate, age) = nearestBirthday ?: Triple("No Birthdays", today, 0)
            val days = ChronoUnit.DAYS.between(today, nextDate).toInt()
            val isToday = days == 0

            val scriptTypeface = ResourcesCompat.getFont(context, R.font.sacramento)
            val numberSize = height * 0.65f
            val scriptSize = height * 0.14f

            val centerX = width / 2f
            val centerY = height / 2f

            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = numberColor
                textAlign = Paint.Align.CENTER
                
                if (isToday) {
                    typeface = scriptTypeface
                    isFakeBoldText = true 
                    textSize = height * 0.35f
                    val textWidth = measureText("TODAY")
                    if (textWidth > width * 0.85f) {
                        textSize *= (width * 0.85f / textWidth)
                    }
                } else {
                    textSize = numberSize
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }

                if (numberGlowEnabled) {
                    val glowColor = prefs.getInt("number_glow_color", prefs.getInt("number_color", Color.WHITE))
                    setShadowLayer(numberRadius, 0f, 0f, glowColor)
                }
            }

            val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = scriptSize
                textAlign = Paint.Align.CENTER
                typeface = scriptTypeface
                
                if (textGlowEnabled) {
                   // Use opaque color for glow default to ensure visibility
                   val glowColor = prefs.getInt("text_glow_color", prefs.getInt("text_color", Color.WHITE))
                   setShadowLayer(textRadius, 0f, 0f, glowColor)
                }
            }

            if (isToday) {
                canvas.drawText("TODAY", centerX, centerY + (numberPaint.textSize / 2), numberPaint)
                canvas.drawText("$name is $age", centerX, centerY - (numberPaint.textSize / 2.5f), scriptPaint)
            } else {
                canvas.drawText(days.toString(), centerX, centerY + numberSize / 3, numberPaint)
                
                val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = numberColor
                    textSize = height * 0.08f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    if (numberGlowEnabled) {
                        val glowColor = prefs.getInt("number_glow_color", prefs.getInt("number_color", Color.WHITE))
                        setShadowLayer(numberRadius, 0f, 0f, glowColor)
                    }
                }
                canvas.drawText("DAYS", centerX, centerY + numberSize / 3 + daysPaint.textSize * 1.1f, daysPaint)
                canvas.drawText("$name is $age in", centerX, centerY + numberSize / 12f, scriptPaint)
            }

            return bitmap
        }

        fun generatePreviewBitmap(context: Context): Bitmap {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            
            val numberColor = getAlphaColor(prefs, "number_color", "number_alpha", Color.WHITE)
            val textColor = getAlphaColor(prefs, "text_color", "text_alpha", Color.WHITE)
            
            val transparency = prefs.getInt("transparency", 60)
            val backgroundEnabled = prefs.getBoolean("background_enabled", true)
            val numberGlowEnabled = prefs.getBoolean("number_glow", false)
            val textGlowEnabled = prefs.getBoolean("text_glow", false)
            
            // Scaling intensity for better visibility (0-100 range -> 0.1-25 radius)
            val numberRadius = (prefs.getInt("number_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)
            val textRadius = (prefs.getInt("text_glow_intensity", 50) / 4f).coerceAtLeast(0.1f)

            val width = 600
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (backgroundEnabled) {
                val alpha = (transparency / 100f * 255).toInt()
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(alpha, 30, 30, 30) }
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 80f, 80f, bgPaint)
            }

            val scriptTypeface = ResourcesCompat.getFont(context, R.font.sacramento)
            val numberSize = height * 0.65f
            
            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = numberColor
                textSize = numberSize
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                if (numberGlowEnabled) {
                    val glowColor = prefs.getInt("number_glow_color", prefs.getInt("number_color", Color.WHITE))
                    setShadowLayer(numberRadius, 0f, 0f, glowColor)
                }
            }

            val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                textSize = height * 0.14f
                typeface = scriptTypeface
                textAlign = Paint.Align.CENTER
                if (textGlowEnabled) {
                    val glowColor = prefs.getInt("text_glow_color", prefs.getInt("text_color", Color.WHITE))
                    setShadowLayer(textRadius, 0f, 0f, glowColor)
                }
            }

            canvas.drawText("9", 300f, 200f + numberSize / 3, numberPaint)
            canvas.drawText("Awel is 4 in", 300f, 200f + numberSize / 12f, scriptPaint)

            return bitmap
        }
    }
}
