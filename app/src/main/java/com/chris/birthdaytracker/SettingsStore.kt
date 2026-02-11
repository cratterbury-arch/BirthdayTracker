package com.chris.birthdaytracker

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object SettingsStore {

    val SOUND = booleanPreferencesKey("sound_enabled")
    val CONFETTI = booleanPreferencesKey("confetti_enabled")
    val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

    fun soundEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SOUND] ?: true }

    fun confettiEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[CONFETTI] ?: true }

    fun notificationsEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[NOTIFICATIONS] ?: true }

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
