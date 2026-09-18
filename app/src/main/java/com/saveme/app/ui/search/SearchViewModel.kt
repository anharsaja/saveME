package com.saveme.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveme.app.SaveMeApp
import com.saveme.app.appViewModelFactory
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.db.LibraryStats
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.db.TagEntity
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _activeTagId = MutableStateFlow<Long?>(null)
    val activeTagId: StateFlow<Long?> = _activeTagId.asStateFlow()

    val stats: StateFlow<LibraryStats> = repository.stats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryStats(0, 0, 0, 0))

    val recent: StateFlow<List<LinkWithTags>> = repository.recentLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCollections: StateFlow<List<CollectionEntity>> = repository.allCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tags: StateFlow<List<TagEntity>> = repository.allTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Hasil link: mengikuti filter tag bila ada, kalau tidak mengikuti kata kunci. */
    val linkResults: StateFlow<List<LinkWithTags>> =
        combine(_query, _activeTagId) { q, tagId -> q.trim() to tagId }
            .flatMapLatest { (q, tagId) ->
                when {
                    tagId != null -> repository.linksWithTag(tagId)
                    q.isBlank() -> flowOf(emptyList())
                    else -> repository.searchLinks(q)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val collectionResults: StateFlow<List<CollectionSummary>> = _query
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.searchCollections(q.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
        if (value.isNotBlank()) _activeTagId.value = null
    }

    fun toggleTag(tagId: Long) {
        _activeTagId.value = if (_activeTagId.value == tagId) null else tagId
        if (_activeTagId.value != null) _query.value = ""
    }

    fun clear() {
        _query.value = ""
        _activeTagId.value = null
    }

    companion object {
        val Factory = appViewModelFactory { app: SaveMeApp -> SearchViewModel(app.repository) }
    }
}
