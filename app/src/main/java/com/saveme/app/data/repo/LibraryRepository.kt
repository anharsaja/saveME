package com.saveme.app.data.repo

import android.content.Context
import android.net.Uri
import com.saveme.app.data.db.AppDatabase
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.db.LibraryStats
import com.saveme.app.data.db.LinkEntity
import com.saveme.app.data.db.LinkTagCrossRef
import com.saveme.app.data.db.LinkWithTags
import com.saveme.app.data.db.TagEntity
import com.saveme.app.data.meta.LinkMetadataFetcher
import com.saveme.app.util.UrlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Satu-satunya pintu ke penyimpanan lokal. Tidak ada akun, tidak ada server:
 * semua isi aplikasi hidup di database Room dan folder internal perangkat.
 */
class LibraryRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext)
    private val collections = db.collectionDao()
    private val links = db.linkDao()
    private val tags = db.tagDao()
    private val fetcher = LinkMetadataFetcher(appContext)

    /** Pengambilan metadata harus selesai walau layar yang memicunya sudah ditutup. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---------------------------------------------------------------- bacaan

    fun rootCollections(): Flow<List<CollectionSummary>> = collections.observeRoots()

    fun childCollections(parentId: Long): Flow<List<CollectionSummary>> =
        collections.observeChildren(parentId)

    fun allCollections(): Flow<List<CollectionEntity>> = collections.observeAll()

    fun collection(id: Long): Flow<CollectionEntity?> = collections.observeById(id)

    fun linksIn(collectionId: Long): Flow<List<LinkWithTags>> =
        links.observeInCollection(collectionId)

    fun recentLinks(limit: Int = 12): Flow<List<LinkWithTags>> = links.observeRecent(limit)

    fun unreadLinks(): Flow<List<LinkWithTags>> = links.observeUnread()

    fun link(id: Long): Flow<LinkWithTags?> = links.observeById(id)

    fun linksWithTag(tagId: Long): Flow<List<LinkWithTags>> = links.observeByTag(tagId)

    fun allTags(): Flow<List<TagEntity>> = tags.observeAll()

    fun searchLinks(query: String): Flow<List<LinkWithTags>> = links.search(query)

    fun searchCollections(query: String): Flow<List<CollectionSummary>> = collections.search(query)

    fun stats(): Flow<LibraryStats> = combine(
        collections.observeAll().map { it.size },
        links.observeCount(),
        links.observePinnedCount(),
        tags.observeCount(),
    ) { collectionCount, linkCount, pinned, tagCount ->
        LibraryStats(collectionCount, linkCount, pinned, tagCount)
    }

    // ------------------------------------------------------------- koleksi

    suspend fun createCollection(
        name: String,
        colorKey: String,
        iconKey: String,
        parentId: Long? = null,
    ): Long {
        val position = collections.nextPosition(parentId)
        return collections.insert(
            CollectionEntity(
                name = name.trim(),
                colorKey = colorKey,
                iconKey = iconKey,
                parentId = parentId,
                position = position,
            ),
        )
    }

    suspend fun updateCollection(collection: CollectionEntity) = collections.update(collection)

    /** Menghapus koleksi beserta subkoleksi, link, dan berkas thumbnail-nya. */
    suspend fun deleteCollection(id: Long) {
        val target = collections.byId(id) ?: return
        val doomed = (descendantsOf(id) + id).toSet()
        links.allOnce()
            .filter { it.link.collectionId in doomed }
            .forEach {
                fetcher.deleteThumbnail(it.link.imagePath)
                fetcher.deleteThumbnail(it.link.customImagePath)
            }
        collections.delete(target)
        tags.deleteOrphans()
    }

    private suspend fun descendantsOf(id: Long): List<Long> {
        val all = collections.allOnce()
        val result = mutableListOf<Long>()
        val queue = ArrayDeque<Long>().apply { add(id) }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            all.filter { it.parentId == current }.forEach {
                result += it.id
                queue += it.id
            }
        }
        return result
    }

    suspend fun defaultCollectionId(): Long {
        collections.allOnce().firstOrNull { it.parentId == null }?.let { return it.id }
        return createCollection("Inbox", "sunny", "bookmark")
    }

    // ---------------------------------------------------------------- link

    sealed interface SaveResult {
        data class Saved(val id: Long) : SaveResult
        /** Tautan sudah ada; [collectionName] memberi tahu di mana tersimpannya. */
        data class Duplicate(val id: Long, val collectionName: String?) : SaveResult
        data object InvalidUrl : SaveResult
    }

    /**
     * Menyimpan link langsung dengan data seadanya, lalu memperkayanya di latar
     * belakang. Dengan begitu kartu langsung muncul tanpa menunggu jaringan.
     */
    suspend fun saveLink(rawUrl: String?, collectionId: Long): SaveResult {
        val url = UrlUtil.normalize(rawUrl) ?: return SaveResult.InvalidUrl
        links.findByUrl(url)?.let { existing ->
            val owner = collections.byId(existing.collectionId)?.name
            return SaveResult.Duplicate(existing.id, owner)
        }
        val now = System.currentTimeMillis()
        val id = links.insert(
            LinkEntity(
                collectionId = collectionId,
                url = url,
                title = UrlUtil.prettyHost(url),
                siteName = UrlUtil.prettyHost(url),
                createdAt = now,
                updatedAt = now,
            ),
        )
        scope.launch { enrich(id) }
        return SaveResult.Saved(id)
    }

    /**
     * Menyimpan dari layar yang langsung ditutup setelah aksi, seperti lembar
     * berbagi. Pekerjaannya berjalan di lingkup repositori supaya tidak ikut
     * dibatalkan saat layarnya hilang.
     */
    fun saveLinkDetached(
        rawUrl: String?,
        collectionId: Long,
        onResult: (SaveResult) -> Unit,
    ) {
        scope.launch {
            val result = saveLink(rawUrl, collectionId)
            withContext(Dispatchers.Main) { onResult(result) }
        }
    }

    /** Mengambil ulang judul, deskripsi, dan thumbnail dari halaman asal. */
    suspend fun enrich(linkId: Long) {
        val current = links.byId(linkId) ?: return
        val meta = fetcher.fetch(current.url)
        val newThumb = meta.imageUrl?.let { fetcher.downloadThumbnail(it) }
        if (newThumb != null && newThumb != current.imagePath) {
            fetcher.deleteThumbnail(current.imagePath)
        }
        links.update(
            current.copy(
                title = meta.title?.takeIf { it.isNotBlank() } ?: current.title,
                description = meta.description ?: current.description,
                siteName = meta.siteName ?: current.siteName,
                authorLine = meta.authorLine ?: current.authorLine,
                imagePath = newThumb ?: current.imagePath,
                imageUrl = meta.imageUrl ?: current.imageUrl,
                isVideo = meta.isVideo || current.isVideo,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    /**
     * Mengganti pratinjau dengan gambar pilihan pengguna. Pratinjau bawaan
     * tetap disimpan supaya bisa dikembalikan kapan saja.
     */
    suspend fun setCustomPreview(linkId: Long, source: Uri): Boolean {
        val link = links.byId(linkId) ?: return false
        val stored = fetcher.importImage(source) ?: return false
        fetcher.deleteThumbnail(link.customImagePath)
        links.update(link.copy(customImagePath = stored, updatedAt = System.currentTimeMillis()))
        return true
    }

    /** Membuang gambar pilihan pengguna dan kembali ke pratinjau bawaan konten. */
    suspend fun clearCustomPreview(linkId: Long) {
        val link = links.byId(linkId) ?: return
        if (link.customImagePath == null) return
        fetcher.deleteThumbnail(link.customImagePath)
        links.update(link.copy(customImagePath = null, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteLink(id: Long) {
        val link = links.byId(id) ?: return
        fetcher.deleteThumbnail(link.imagePath)
        fetcher.deleteThumbnail(link.customImagePath)
        links.delete(link)
        tags.deleteOrphans()
    }

    suspend fun deleteLinks(ids: List<Long>) {
        if (ids.isEmpty()) return
        links.allOnce()
            .filter { it.link.id in ids }
            .forEach {
                fetcher.deleteThumbnail(it.link.imagePath)
                fetcher.deleteThumbnail(it.link.customImagePath)
            }
        links.deleteByIds(ids)
        tags.deleteOrphans()
    }

    suspend fun moveLinks(ids: List<Long>, collectionId: Long) {
        if (ids.isEmpty()) return
        links.moveToCollection(ids, collectionId, System.currentTimeMillis())
    }

    suspend fun togglePin(id: Long) {
        val link = links.byId(id) ?: return
        links.setPinned(id, !link.isPinned, System.currentTimeMillis())
    }

    suspend fun setRead(id: Long, read: Boolean) {
        links.setRead(id, read, System.currentTimeMillis())
    }

    suspend fun setNotes(id: Long, notes: String?) {
        links.setNotes(id, notes?.trim()?.ifBlank { null }, System.currentTimeMillis())
    }

    suspend fun updateLinkDetails(id: Long, title: String, description: String?) {
        val link = links.byId(id) ?: return
        links.update(
            link.copy(
                title = title.trim().ifBlank { link.title },
                description = description?.trim()?.ifBlank { null },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    // ----------------------------------------------------------------- tag

    suspend fun addTag(linkId: Long, rawName: String, colorKey: String = "mint"): Boolean {
        val name = rawName.trim().removePrefix("#").lowercase()
        if (name.isEmpty()) return false
        val existing = tags.byName(name)
        val tagId = if (existing != null) {
            existing.id
        } else {
            val inserted = tags.insert(TagEntity(name = name, colorKey = colorKey))
            if (inserted > 0) inserted else tags.byName(name)?.id ?: return false
        }
        tags.link(LinkTagCrossRef(linkId = linkId, tagId = tagId))
        return true
    }

    suspend fun removeTag(linkId: Long, tagId: Long) {
        tags.unlink(linkId, tagId)
        tags.deleteOrphans()
    }

    suspend fun renameTag(tagId: Long, newName: String) {
        val tag = tags.byId(tagId) ?: return
        val name = newName.trim().removePrefix("#").lowercase()
        if (name.isEmpty()) return
        tags.update(tag.copy(name = name))
    }

    /** Mengosongkan seluruh isi aplikasi, termasuk berkas thumbnail di penyimpanan. */
    suspend fun clearEverything() {
        links.allOnce().forEach {
            fetcher.deleteThumbnail(it.link.imagePath)
            fetcher.deleteThumbnail(it.link.customImagePath)
        }
        links.clear()
        tags.clear()
        collections.clear()
    }

    // -------------------------------------------------------------- akses

    internal fun database(): AppDatabase = db

    internal fun metadataFetcher(): LinkMetadataFetcher = fetcher

    internal fun backgroundScope(): CoroutineScope = scope
}
