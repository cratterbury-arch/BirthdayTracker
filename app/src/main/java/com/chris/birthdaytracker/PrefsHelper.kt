package com.chris.birthdaytracker

import android.content.Context
import android.graphics.Color

object PrefsHelper {

    private const val PREFS = "widget_prefs"

    private const val KEY_OPACITY = "opacity"
    private const val KEY_BG_COLOR = "bg_color"
    private const val KEY_FONT_COLOR = "font_color"
    private const val KEY_USE_SYSTEM_FONT = "system_font"
    private const val KEY_RING_MODE = "ring_mode"

    fun getOpacity(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_OPACITY, 200)

    fun setOpacity(context: Context, value: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_OPACITY, value).apply()

    fun getBackgroundColor(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_BG_COLOR, Color.parseColor("#111827"))

    fun setBackgroundColor(context: Context, color: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_BG_COLOR, color).apply()

    fun getFontColor(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_FONT_COLOR, Color.WHITE)

    fun setFontColor(context: Context, color: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_FONT_COLOR, color).apply()

    fun useSystemFont(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_USE_SYSTEM_FONT, true)

    fun setUseSystemFont(context: Context, value: Boolean) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_USE_SYSTEM_FONT, value).apply()

    fun getRingMode(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_RING_MODE, 0)

    fun setRingMode(context: Context, mode: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_RING_MODE, mode).apply()
}