package com.chris.birthdaytracker

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object SettingsStore {

    private val SOUND = booleanPreferencesKey("sound_enabled")
    private val CONFETTI = booleanPreferencesKey("confetti_enabled")
    private val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

    /* ---------- READ ---------- */

    fun soundEnabled(context: Context) =
        context.dataStore.data.map { it[SOUND] ?: true }

    fun confettiEnabled(context: Context) =
        context.dataStore.data.map { it[CONFETTI] ?: true }

    fun notificationsEnabled(context: Context) =
        context.dataStore.data.map { it[NOTIFICATIONS] ?: true }

    /* ---------- WRITE ---------- */

    suspend fun setSound(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SOUND] = enabled }
    }

    suspend fun setConfetti(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[CONFETTI] = enabled }
    }

    suspend fun setNotifications(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS] = enabled }
    }
}
