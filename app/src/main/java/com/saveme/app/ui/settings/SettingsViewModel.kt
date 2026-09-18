package com.saveme.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.backup.BackupManager
import com.saveme.app.data.db.LibraryStats
import com.saveme.app.data.prefs.AppSettings
import com.saveme.app.data.prefs.SettingsStore
import com.saveme.app.data.prefs.ThemeMode
import com.saveme.app.data.repo.LibraryRepository
import com.saveme.app.work.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: LibraryRepository,
    private val store: SettingsStore,
    private val backup: BackupManager,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val stats: StateFlow<LibraryStats> = repository.stats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryStats(0, 0, 0, 0))

    var banner by mutableStateOf<String?>(null)
    var working by mutableStateOf(false)
        private set

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { store.setHaptics(enabled) }
    }

    fun setReminders(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            store.setReminders(enabled)
            if (enabled) {
                ReminderScheduler.schedule(context, store.settings.first().reminderHour)
                banner = "Reminders on"
            } else {
                ReminderScheduler.cancel(context)
                banner = "Reminders off"
            }
        }
    }

    fun setReminderHour(context: Context, hour: Int) {
        viewModelScope.launch {
            store.setReminderHour(hour)
            if (store.settings.first().remindersEnabled) {
                ReminderScheduler.schedule(context, hour)
            }
        }
    }

    fun setFocusUnreadOnly(enabled: Boolean) {
        viewModelScope.launch { store.setFocusUnreadOnly(enabled) }
    }

    fun suggestedBackupName(): String = backup.suggestedFileName()

    fun export(uri: Uri) {
        working = true
        viewModelScope.launch {
            val result = backup.export(uri)
            working = false
            banner = result.fold(
                onSuccess = { count ->
                    store.setLastBackupAt(System.currentTimeMillis())
                    "Backed up $count links"
                },
                onFailure = { "Backup failed: ${it.message}" },
            )
        }
    }

    fun import(uri: Uri) {
        working = true
        viewModelScope.launch {
            val result = backup.import(uri)
            working = false
            banner = result.fold(
                onSuccess = { count -> "Restored $count links" },
                onFailure = { "Restore failed: ${it.message}" },
            )
        }
    }

    fun clearEverything() {
        working = true
        viewModelScope.launch {
            repository.clearEverything()
            working = false
            banner = "Everything cleared"
        }
    }

    companion object {
        val Factory = appViewModelFactory { app: SaveMeApp ->
            SettingsViewModel(app.repository, app.settings, app.backup)
        }
    }
}
