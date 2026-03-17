package com.chris.birthdaytracker

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object WidgetRenderer {

    suspend fun render(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        widgetId: Int
    ): Bitmap = withContext(Dispatchers.IO) {

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

        val transparency = prefs.getInt("transparency_$widgetId", 50)
        val glowEnabled = prefs.getBoolean("glow_$widgetId", false)
        val numberColor = prefs.getInt("number_color_$widgetId", Color.WHITE)
        val scriptColor = prefs.getInt("script_color_$widgetId", Color.WHITE)

        canvas.drawColor(Color.argb(transparency, 0, 0, 0))

        val contacts = ContactsRepository(context).getAllContacts()
        val today = LocalDate.now()

        val nextContact = contacts
            .filter { it.birthday != null }
            .minByOrNull { contact ->
                val next = contact.birthday!!
                    .withYear(today.year)
                    .let { if (it.isBefore(today)) it.plusYears(1) else it }
                ChronoUnit.DAYS.between(today, next)
            } ?: return@withContext bitmap

        val birthday = nextContact.birthday!!
        val nextBirthday = birthday.withYear(today.year)
            .let { if (it.isBefore(today)) it.plusYears(1) else it }

        val days = ChronoUnit.DAYS.between(today, nextBirthday).toInt()
        val age = nextBirthday.year - birthday.year

        val centerX = widthPx / 2f
        val centerY = heightPx / 2f

        // BIG NUMBER
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = numberColor
            textSize = heightPx * 0.75f
            typeface = Typeface.DEFAULT_BOLD

            if (glowEnabled) {
                setShadowLayer(40f, 0f, 0f, numberColor)
            }
        }

        canvas.drawText(days.toString(), centerX, centerY + heightPx * 0.2f, numberPaint)

        // DAYS LABEL
        val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = numberColor
            textSize = heightPx * 0.15f
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawText("DAYS", centerX, centerY + heightPx * 0.32f, daysPaint)

        // SCRIPT TEXT
        val scriptTypeface =
            ResourcesCompat.getFont(context, R.font.sacramento)

        val scriptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = scriptColor
            textSize = heightPx * 0.13f
            typeface = scriptTypeface
        }

        canvas.drawText(
            "${nextContact.name} is $age in",
            centerX,
            centerY,
            scriptPaint
        )

        bitmap
    }
}