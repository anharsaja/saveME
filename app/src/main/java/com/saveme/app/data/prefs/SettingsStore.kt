package com.saveme.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saveme_settings")

/** Pilihan tampilan: ikut sistem, selalu terang, atau selalu gelap. */
enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Always light"),
    DARK("Always dark"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
    val remindersEnabled: Boolean = true,
    /** Jam (0-23) saat pengingat link belum dibaca dikirim. */
    val reminderHour: Int = 19,
    val focusUnreadOnly: Boolean = true,
    val lastBackupAt: Long = 0L,
)

class SettingsStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]
                ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
                ?: ThemeMode.SYSTEM,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            remindersEnabled = prefs[Keys.REMINDERS] ?: true,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 19,
            focusUnreadOnly = prefs[Keys.FOCUS_UNREAD_ONLY] ?: true,
            lastBackupAt = prefs[Keys.LAST_BACKUP] ?: 0L,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }

    suspend fun setHaptics(enabled: Boolean) = edit { it[Keys.HAPTICS] = enabled }

    suspend fun setReminders(enabled: Boolean) = edit { it[Keys.REMINDERS] = enabled }

    suspend fun setReminderHour(hour: Int) = edit { it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23) }

    suspend fun setFocusUnreadOnly(enabled: Boolean) = edit { it[Keys.FOCUS_UNREAD_ONLY] = enabled }

    suspend fun setLastBackupAt(timestamp: Long) = edit { it[Keys.LAST_BACKUP] = timestamp }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val HAPTICS = booleanPreferencesKey("haptics")
        val REMINDERS = booleanPreferencesKey("reminders")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val FOCUS_UNREAD_ONLY = booleanPreferencesKey("focus_unread_only")
        val LAST_BACKUP = longPreferencesKey("last_backup")
    }
}

/** Menerjemahkan pilihan tema jadi jawaban ya/tidak untuk layar gelap. */
@androidx.compose.runtime.Composable
fun ThemeMode.resolveDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
