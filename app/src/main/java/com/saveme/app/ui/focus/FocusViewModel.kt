package com.saveme.app.ui.focus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.prefs.SettingsStore
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mode fokus: satu link di satu waktu, untuk membereskan tumpukan bacaan
 * tanpa tergoda petak kartu yang penuh.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModel(
    private val repository: LibraryRepository,
    store: SettingsStore,
) : ViewModel() {

    val queue: StateFlow<List<LinkWithTags>> = store.settings
        .map { it.focusUnreadOnly }
        .flatMapLatest { unreadOnly ->
            if (unreadOnly) repository.unreadLinks() else repository.recentLinks(limit = 100)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Link yang sudah dilewati tidak muncul lagi selama sesi ini berlangsung. */
    var skipped by mutableStateOf<Set<Long>>(emptySet())
        private set

    var doneCount by mutableStateOf(0)
        private set

    fun skip(id: Long) {
        skipped = skipped + id
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            repository.setRead(id, true)
            doneCount++
        }
    }

    fun reset() {
        skipped = emptySet()
        doneCount = 0
    }

    companion object {
        val Factory = appViewModelFactory { app: SaveMeApp ->
            FocusViewModel(app.repository, app.settings)
        }
    }
}
