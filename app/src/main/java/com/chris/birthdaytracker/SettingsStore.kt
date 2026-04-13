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
    val THEME = stringPreferencesKey("theme")
    val ENABLED_CALENDAR_ACCOUNTS = stringSetPreferencesKey("enabled_calendar_accounts")
    val DISABLED_PHONE_ACCOUNTS = stringSetPreferencesKey("disabled_phone_accounts")
    val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
    val PREFERRED_SOURCES = stringSetPreferencesKey("preferred_sources") // Format: "duplicateKey|preferredContactId"

    fun soundEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SOUND] ?: true }

    fun confettiEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[CONFETTI] ?: true }

    fun notificationsEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[NOTIFICATIONS] ?: true }

    fun getTheme(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME] ?: "system" }

    fun enabledCalendarAccounts(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { it[ENABLED_CALENDAR_ACCOUNTS] ?: emptySet() }

    fun disabledPhoneAccounts(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { it[DISABLED_PHONE_ACCOUNTS] ?: emptySet() }

    fun isFirstRun(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[IS_FIRST_RUN] ?: true }

    fun preferredSources(context: Context): Flow<Map<String, String>> =
        context.dataStore.data.map { prefs ->
            val set = prefs[PREFERRED_SOURCES] ?: emptySet()
            set.mapNotNull {
                val parts = it.split("|")
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
        }

    suspend fun setSound(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SOUND] = enabled }
    }

    suspend fun setConfetti(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[CONFETTI] = enabled }
    }

    suspend fun setNotifications(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS] = enabled }
    }

    suspend fun setTheme(context: Context, theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    suspend fun setFirstRunCompleted(context: Context) {
        context.dataStore.edit { it[IS_FIRST_RUN] = false }
    }

    suspend fun setPreferredSource(context: Context, duplicateKey: String, contactId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PREFERRED_SOURCES] ?: emptySet()
            // Remove any existing preference for this key
            val filtered = current.filter { !it.startsWith("$duplicateKey|") }.toSet()
            prefs[PREFERRED_SOURCES] = filtered + "$duplicateKey|$contactId"
        }
    }

    suspend fun toggleCalendarAccount(context: Context, accountName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[ENABLED_CALENDAR_ACCOUNTS] ?: emptySet()
            if (current.contains(accountName)) {
                prefs[ENABLED_CALENDAR_ACCOUNTS] = current - accountName
            } else {
                prefs[ENABLED_CALENDAR_ACCOUNTS] = current + accountName
            }
        }
    }

    suspend fun togglePhoneAccount(context: Context, accountName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[DISABLED_PHONE_ACCOUNTS] ?: emptySet()
            if (current.contains(accountName)) {
                prefs[DISABLED_PHONE_ACCOUNTS] = current - accountName
            } else {
                prefs[DISABLED_PHONE_ACCOUNTS] = current + accountName
            }
        }
    }
}
