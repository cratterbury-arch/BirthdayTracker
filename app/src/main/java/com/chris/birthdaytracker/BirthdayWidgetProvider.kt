package com.chris.birthdaytracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

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

        private const val PREFS = "widget_prefs"

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
            val density = context.resources.displayMetrics.density

            val widthPx = max(
                (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) * density).toInt(),
                300
            )

            val heightPx = max(
                (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) * density).toInt(),
                300
            )

            val bitmap = renderWidget(context, widthPx, heightPx)

            val views = RemoteViews(context.packageName, R.layout.birthday_widget)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val settingsIntent = Intent(context, WidgetSettingsActivity::class.java)
            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            settingsIntent.putExtra("from_existing_widget", true)

            val settingsPending = PendingIntent.getActivity(
                context,
                widgetId + 1000,
                settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.settings_area, settingsPending)

            manager.updateAppWidget(widgetId, views)
        }

        private fun renderWidget(context: Context, width: Int, height: Int): Bitmap {

            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

            val numberColor = prefs.getInt("number_color", Color.WHITE)
            val textColor = prefs.getInt("text_color", Color.WHITE)
            val numberGlowColor = prefs.getInt("number_glow_color", numberColor)
            val textGlowColor = prefs.getInt("text_glow_color", textColor)

            val numberGlow = prefs.getBoolean("number_glow", false)
            val textGlow = prefs.getBoolean("text_glow", false)

            val numberAlpha = prefs.getInt("number_alpha", 255)
            val textAlpha = prefs.getInt("text_alpha", 255)

            val glowIntensity = prefs.getInt("glow_intensity", 50)

            val backgroundEnabled = prefs.getBoolean("background_enabled", true)
            val transparency = prefs.getInt("transparency", 60)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgAlpha = (transparency / 100f * 255).toInt()

            if (backgroundEnabled) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                bgPaint.color = Color.argb(bgAlpha, 30, 30, 30)
                canvas.drawRoundRect(
                    0f, 0f,
                    width.toFloat(),
                    height.toFloat(),
                    height * 0.08f,
                    height * 0.08f,
                    bgPaint
                )
            }

            val today = LocalDate.now()
            val contacts = ContactsRepository(context).getAllContacts()

            val nextContact = contacts
                .filter { it.birthday != null }
                .minByOrNull { contact ->
                    val next = contact.birthday!!
                        .withYear(today.year)
                        .let { if (it.isBefore(today)) it.plusYears(1) else it }
                    ChronoUnit.DAYS.between(today, next)
                }

            if (nextContact == null) return bitmap

            val birthDate = nextContact.birthday!!
            val nextBirthday = birthDate.withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }

            val days = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
            val age = nextBirthday.year - birthDate.year

            val centerX = width / 2f
            val centerY = height / 2f

            val adaptiveGlowRadius = (height * 0.05f) * (glowIntensity / 100f)

            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = numberColor
                alpha = numberAlpha
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = height * 0.88f
            }

            if (numberGlow) {
                numberPaint.setShadowLayer(adaptiveGlowRadius, 0f, 0f, numberGlowColor)
            }

            canvas.drawText(days.toString(), centerX, centerY + (height * 0.22f), numberPaint)

            val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = numberColor
                alpha = numberAlpha
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textSize = height * 0.08f
            }

            canvas.drawText("DAYS", centerX, centerY + (height * 0.32f), daysPaint)

            val scriptTypeface = ResourcesCompat.getFont(context, R.font.sacramento)

            val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                alpha = textAlpha
                textAlign = Paint.Align.CENTER
                typeface = scriptTypeface
                textSize = height * 0.15f
            }

            if (textGlow) {
                scriptPaint.setShadowLayer(adaptiveGlowRadius * 0.6f, 0f, 0f, textGlowColor)
            }

            canvas.drawText("${nextContact.name} is $age in", centerX, centerY, scriptPaint)

            return bitmap
        }

        fun generatePreviewBitmap(context: Context): Bitmap {
            return renderWidget(context, 800, 500)
        }
    }
}