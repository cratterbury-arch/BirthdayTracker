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

    val NOTIFICATION_DAYS = stringSetPreferencesKey("notification_days") // "0", "1", "2" etc.
    val NOTIFICATION_TIME = stringPreferencesKey("notification_time") // "09:30"
    val FAVORITES_ONLY_NOTIFICATIONS = booleanPreferencesKey("favorites_only_notifications")
    val NOTIFICATION_SOUND_URI = stringPreferencesKey("notification_sound_uri")
    val SORT_BY_SURNAME = booleanPreferencesKey("sort_by_surname")

    val SHOW_ZODIAC = booleanPreferencesKey("show_zodiac")
    val SHOW_CHINESE_YEAR = booleanPreferencesKey("show_chinese_year")
    val GOOGLE_SYNC_ENABLED = booleanPreferencesKey("google_sync_enabled")

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

    fun notificationDays(context: Context): Flow<Set<String>> =
        context.dataStore.data.map { it[NOTIFICATION_DAYS] ?: setOf("0") }

    fun notificationTime(context: Context): Flow<String> =
        context.dataStore.data.map { it[NOTIFICATION_TIME] ?: "09:00" }

    fun favoritesOnlyNotifications(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[FAVORITES_ONLY_NOTIFICATIONS] ?: false }

    fun notificationSoundUri(context: Context): Flow<String?> =
        context.dataStore.data.map { it[NOTIFICATION_SOUND_URI] }

    fun sortBySurname(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SORT_BY_SURNAME] ?: false }

    fun showZodiac(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SHOW_ZODIAC] ?: false }

    fun showChineseYear(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[SHOW_CHINESE_YEAR] ?: false }

    fun googleSyncEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[GOOGLE_SYNC_ENABLED] ?: false }

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

    suspend fun setNotificationDays(context: Context, days: Set<String>) {
        context.dataStore.edit { it[NOTIFICATION_DAYS] = days }
    }

    suspend fun setNotificationTime(context: Context, time: String) {
        context.dataStore.edit { it[NOTIFICATION_TIME] = time }
    }

    suspend fun setFavoritesOnlyNotifications(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[FAVORITES_ONLY_NOTIFICATIONS] = enabled }
    }

    suspend fun setNotificationSoundUri(context: Context, uri: String?) {
        context.dataStore.edit {
            if (uri == null) it.remove(NOTIFICATION_SOUND_URI)
            else it[NOTIFICATION_SOUND_URI] = uri
        }
    }

    suspend fun setSortBySurname(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SORT_BY_SURNAME] = enabled }
    }

    suspend fun setShowZodiac(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SHOW_ZODIAC] = enabled }
    }

    suspend fun setShowChineseYear(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[SHOW_CHINESE_YEAR] = enabled }
    }

    suspend fun setGoogleSyncEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[GOOGLE_SYNC_ENABLED] = enabled }
    }
}
