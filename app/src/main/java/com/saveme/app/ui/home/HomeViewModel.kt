package com.saveme.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.repo.LibraryRepository
import com.saveme.app.util.UrlUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: LibraryRepository) : ViewModel() {

    val collections: StateFlow<List<CollectionSummary>> = repository.rootCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCollections: StateFlow<List<CollectionEntity>> = repository.allCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var quickUrl by mutableStateOf("")
        private set

    /** Pesan hijau sesaat di bawah layar. */
    var banner by mutableStateOf<String?>(null)

    /** Peringatan bertombol satu, misalnya saat kolom tautan kosong. */
    var alert by mutableStateOf<AlertContent?>(null)

    data class AlertContent(val title: String, val message: String)

    fun onQuickUrlChange(value: String) {
        quickUrl = value
    }

    /**
     * Memeriksa isi kolom tempel sebelum membuka pemilih koleksi, supaya
     * pengguna tidak memilih tujuan untuk tautan yang ternyata tidak valid.
     */
    fun quickUrlIsUsable(): Boolean {
        if (UrlUtil.normalize(quickUrl) != null) return true
        alert = if (quickUrl.isBlank()) {
            AlertContent("No URL", "Please enter a link to save")
        } else {
            AlertContent("That's not a link", "Check the address and try again")
        }
        return false
    }

    fun saveQuickUrl(collectionId: Long, collectionName: String) {
        val url = quickUrl
        viewModelScope.launch {
            when (val result = repository.saveLink(url, collectionId)) {
                is LibraryRepository.SaveResult.Saved -> {
                    quickUrl = ""
                    banner = "Saved to $collectionName"
                }

                is LibraryRepository.SaveResult.Duplicate -> {
                    quickUrl = ""
                    banner = duplicateMessage(result.collectionName)
                }

                LibraryRepository.SaveResult.InvalidUrl ->
                    alert = AlertContent("That's not a link", "Check the address and try again")
            }
        }
    }


    private fun duplicateMessage(collectionName: String?): String =
        if (collectionName.isNullOrBlank()) {
            "Already in your library"
        } else {
            "Already saved in $collectionName"
        }

    fun createCollection(name: String, colorKey: String, iconKey: String, parentId: Long? = null) {
        viewModelScope.launch {
            repository.createCollection(name, colorKey, iconKey, parentId)
            banner = "Collection created"
        }
    }

    fun updateCollection(collection: CollectionEntity, name: String, colorKey: String, iconKey: String) {
        viewModelScope.launch {
            repository.updateCollection(
                collection.copy(name = name, colorKey = colorKey, iconKey = iconKey),
            )
        }
    }

    fun deleteCollection(id: Long) {
        viewModelScope.launch {
            repository.deleteCollection(id)
            banner = "Collection deleted"
        }
    }

    companion object {
        val Factory = appViewModelFactory { app: SaveMeApp -> HomeViewModel(app.repository) }
    }
}
