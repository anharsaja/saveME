package com.saveme.app.ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val repository: LibraryRepository,
    val collectionId: Long,
) : ViewModel() {

    val collection: StateFlow<CollectionEntity?> = repository.collection(collectionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Urutan tampilan. Tautan yang dipin selalu naik ke atas apa pun urutannya. */
    enum class SortOrder(val label: String) {
        NEWEST("Newest first"),
        OLDEST("Oldest first"),
        TITLE("Title A-Z"),
    }

    private val sort = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = sort.asStateFlow()

    val links: StateFlow<List<LinkWithTags>> =
        combine(repository.linksIn(collectionId), sort) { items, order ->
            val pinnedFirst = compareByDescending<LinkWithTags> { it.link.isPinned }
            when (order) {
                SortOrder.NEWEST -> items.sortedWith(pinnedFirst.thenByDescending { it.link.createdAt })
                SortOrder.OLDEST -> items.sortedWith(pinnedFirst.thenBy { it.link.createdAt })
                SortOrder.TITLE -> items.sortedWith(pinnedFirst.thenBy { it.link.title.lowercase() })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortOrder(order: SortOrder) {
        sort.value = order
    }

    val children: StateFlow<List<CollectionSummary>> = repository.childCollections(collectionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCollections: StateFlow<List<CollectionEntity>> = repository.allCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var selectionMode by mutableStateOf(false)
        private set

    var selected by mutableStateOf<Set<Long>>(emptySet())
        private set

    var banner by mutableStateOf<String?>(null)

    fun enterSelection(initialId: Long? = null) {
        selectionMode = true
        selected = setOfNotNull(initialId)
    }

    fun exitSelection() {
        selectionMode = false
        selected = emptySet()
    }

    fun toggleSelect(id: Long) {
        selected = if (id in selected) selected - id else selected + id
    }

    fun selectAll() {
        selected = links.value.map { it.link.id }.toSet()
    }

    fun deleteSelected() {
        val ids = selected.toList()
        viewModelScope.launch {
            repository.deleteLinks(ids)
            banner = if (ids.size == 1) "Link deleted" else "${ids.size} links deleted"
            exitSelection()
        }
    }

    fun moveSelected(targetCollectionId: Long, targetName: String) {
        val ids = selected.toList()
        viewModelScope.launch {
            repository.moveLinks(ids, targetCollectionId)
            banner = "Moved to $targetName"
            exitSelection()
        }
    }

    fun addLink(url: String, onInvalid: () -> Unit) {
        viewModelScope.launch {
            when (val result = repository.saveLink(url, collectionId)) {
                is LibraryRepository.SaveResult.Saved -> banner = "Link saved"
                is LibraryRepository.SaveResult.Duplicate ->
                    banner = result.collectionName
                        ?.let { "Already saved in $it" }
                        ?: "Already in your library"
                LibraryRepository.SaveResult.InvalidUrl -> onInvalid()
            }
        }
    }

    fun createSubcollection(name: String, colorKey: String, iconKey: String) {
        viewModelScope.launch {
            repository.createCollection(name, colorKey, iconKey, collectionId)
            banner = "Subcollection created"
        }
    }

    fun updateCollection(name: String, colorKey: String, iconKey: String) {
        val current = collection.value ?: return
        viewModelScope.launch {
            repository.updateCollection(
                current.copy(name = name, colorKey = colorKey, iconKey = iconKey),
            )
        }
    }

    fun deleteCollection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
            onDeleted()
        }
    }

    companion object {
        fun factory(collectionId: Long) = appViewModelFactory { app: SaveMeApp ->
            CollectionViewModel(app.repository, collectionId)
        }
    }
}
