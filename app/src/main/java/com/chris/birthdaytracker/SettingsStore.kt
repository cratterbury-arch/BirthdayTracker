package com.chris.birthdaytracker

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object SettingsStore {
    val SOUND = booleanPreferencesKey("sound_enabled")
    val HAPTIC = booleanPreferencesKey("haptic_enabled")

    fun soundEnabled(context: Context) =
        context.dataStore.data.map { it[SOUND] ?: true }

    fun hapticEnabled(context: Context) =
        context.dataStore.data.map { it[HAPTIC] ?: true }
}
