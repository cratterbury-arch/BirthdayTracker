package com.chris.birthdaytracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BirthdayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {

        private fun prefs(context: Context, widgetId: Int) =
            context.getSharedPreferences("widget_$widgetId", Context.MODE_PRIVATE)

        fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {

            val options = manager.getAppWidgetOptions(widgetId)

            val density = context.resources.displayMetrics.density

            val minWidthDp =
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeightDp =
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val width =
                (minWidthDp * density).toInt().coerceAtLeast(300)
            val height =
                (minHeightDp * density).toInt().coerceAtLeast(300)

            val bitmap = renderWidget(context, width, height, widgetId)

            val views =
                RemoteViews(context.packageName, R.layout.birthday_widget)

            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }

        private fun renderWidget(
            context: Context,
            width: Int,
            height: Int,
            widgetId: Int
        ): Bitmap {

            if (width <= 0 || height <= 0) {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }

            val prefs = prefs(context, widgetId)

            val numberColor =
                prefs.getInt("number_color", Color.WHITE)

            val textColor =
                prefs.getInt("text_color", Color.WHITE)

            val transparency =
                prefs.getInt("transparency", 60)

            val backgroundEnabled =
                prefs.getBoolean("background_enabled", true)

            val glowEnabled =
                prefs.getBoolean("glow_enabled", false)

            val bitmap =
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)

            val backgroundAlpha =
                255 - (transparency / 100f * 255).toInt()

            if (backgroundEnabled) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                bgPaint.color = Color.argb(backgroundAlpha, 30, 30, 30)
                canvas.drawRoundRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    80f,
                    80f,
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

            if (nextContact == null) {
                val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                fallbackPaint.color = Color.WHITE
                fallbackPaint.textSize = height * 0.15f
                fallbackPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    "No birthdays",
                    width / 2f,
                    height / 2f,
                    fallbackPaint
                )
                return bitmap
            }

            val birthDate = nextContact.birthday!!
            val nextBirthday = birthDate.withYear(today.year)
                .let { if (it.isBefore(today)) it.plusYears(1) else it }

            val days =
                ChronoUnit.DAYS.between(today, nextBirthday).toInt()

            val age =
                nextBirthday.year - birthDate.year

            val centerX = width / 2f
            val centerY = height / 2f

            val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            numberPaint.color = numberColor
            numberPaint.alpha = 220
            numberPaint.textSize = height * 0.75f
            numberPaint.textAlign = Paint.Align.CENTER
            numberPaint.typeface =
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            if (glowEnabled) {
                numberPaint.setShadowLayer(
                    40f,
                    0f,
                    0f,
                    numberColor
                )
            }

            val scriptTypeface =
                ResourcesCompat.getFont(context, R.font.sacramento)

            val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            scriptPaint.color = textColor
            scriptPaint.textSize = height * 0.18f
            scriptPaint.textAlign = Paint.Align.CENTER
            scriptPaint.typeface = scriptTypeface

            // Big number
            val numberY = centerY + (height * 0.2f)
            canvas.drawText(
                days.toString(),
                centerX,
                numberY,
                numberPaint
            )

// DAYS under number (tight spacing)
            val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            daysPaint.color = numberColor
            daysPaint.alpha = 200
            daysPaint.textSize = height * 0.12f
            daysPaint.textAlign = Paint.Align.CENTER
            daysPaint.typeface =
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            canvas.drawText(
                "DAYS",
                centerX,
                numberY + (height * 0.12f),
                daysPaint
            )

// Front script text centered
            canvas.drawText(
                "${nextContact.name} is $age in...",
                centerX,
                centerY,
                scriptPaint
            )

            return bitmap
        }
    }
}