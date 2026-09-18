package com.saveme.app.ui.link

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LinkDetailViewModel(
    private val repository: LibraryRepository,
    private val linkId: Long,
) : ViewModel() {

    val item: StateFlow<LinkWithTags?> = repository.link(linkId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val allCollections: StateFlow<List<CollectionEntity>> = repository.allCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Nama koleksi induk, ditampilkan di baris meta di bawah judul. */
    val collectionName: StateFlow<String> =
        combine(item, allCollections) { link, collections ->
            collections.firstOrNull { it.id == link?.link?.collectionId }?.name.orEmpty()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    var refreshing by mutableStateOf(false)
        private set

    var banner by mutableStateOf<String?>(null)

    fun markRead() {
        viewModelScope.launch { repository.setRead(linkId, true) }
    }

    fun refresh() {
        if (refreshing) return
        refreshing = true
        viewModelScope.launch {
            repository.enrich(linkId)
            refreshing = false
            banner = "Preview refreshed"
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            repository.togglePin(linkId)
            banner = if (item.value?.link?.isPinned == true) "Unpinned" else "Pinned"
        }
    }

    fun move(collectionId: Long, name: String) {
        viewModelScope.launch {
            repository.moveLinks(listOf(linkId), collectionId)
            banner = "Moved to $name"
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteLink(linkId)
            onDeleted()
        }
    }

    /** Mengganti pratinjau dengan gambar dari galeri. */
    fun setCustomPreview(source: Uri) {
        viewModelScope.launch {
            banner = if (repository.setCustomPreview(linkId, source)) {
                "Preview updated"
            } else {
                "That image could not be read"
            }
        }
    }

    /** Membuang gambar pilihan sendiri dan kembali ke pratinjau bawaan konten. */
    fun clearCustomPreview() {
        viewModelScope.launch {
            repository.clearCustomPreview(linkId)
            banner = "Back to the original preview"
        }
    }

    fun saveNotes(text: String) {
        viewModelScope.launch { repository.setNotes(linkId, text) }
    }

    fun updateDetails(title: String, description: String?) {
        viewModelScope.launch { repository.updateLinkDetails(linkId, title, description) }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            if (!repository.addTag(linkId, name)) banner = "Tag name can't be empty"
        }
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch { repository.removeTag(linkId, tagId) }
    }

    companion object {
        fun factory(linkId: Long) = appViewModelFactory { app: SaveMeApp ->
            LinkDetailViewModel(app.repository, linkId)
        }
    }
}
