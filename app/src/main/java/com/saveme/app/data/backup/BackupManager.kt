package com.saveme.app.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.File
import com.saveme.app.data.db.CollectionEntity
import com.saveme.app.data.db.LinkEntity
import com.saveme.app.data.db.LinkTagCrossRef
import com.saveme.app.data.db.TagEntity
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cadangan berupa satu berkas JSON yang ditulis lewat Storage Access Framework,
 * jadi pengguna sendiri yang memilih lokasinya dan aplikasi tidak perlu izin
 * penyimpanan apa pun.
 */
class BackupManager(
    private val context: Context,
    private val repository: LibraryRepository,
) {

    private val db = repository.database()
    private val fetcher = repository.metadataFetcher()

    suspend fun export(target: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val collections = db.collectionDao().allOnce()
            val links = db.linkDao().allOnce()
            val tags = db.tagDao().allOnce()

            val root = JSONObject().apply {
                put("app", APP_TAG)
                put("formatVersion", FORMAT_VERSION)
                put("exportedAt", System.currentTimeMillis())
                put("collections", JSONArray().apply { collections.forEach { put(it.toJson()) } })
                put("tags", JSONArray().apply { tags.forEach { put(it.toJson()) } })
                put(
                    "links",
                    JSONArray().apply {
                        links.forEach { withTags ->
                            put(
                                withTags.link.toJson().apply {
                                    put(
                                        "tags",
                                        JSONArray().apply { withTags.tags.forEach { put(it.name) } },
                                    )
                                    // Pratinjau bawaan bisa diunduh ulang dari alamatnya,
                                    // tapi gambar pilihan pengguna hanya ada di perangkat
                                    // ini — jadi isinya ikut dibawa di dalam cadangan.
                                    encodeCustomPreview(withTags.link.customImagePath)?.let {
                                        put("customPreview", it)
                                    }
                                },
                            )
                        }
                    },
                )
            }

            context.contentResolver.openOutputStream(target, "wt")?.use { stream ->
                stream.write(root.toString(2).toByteArray())
            } ?: error("Tidak bisa membuka berkas tujuan")

            links.size
        }
    }

    /**
     * Memulihkan cadangan. Isi lama dihapus lebih dulu agar hasilnya sama persis
     * dengan berkas cadangan, bukan gabungan yang bisa menghasilkan duplikat.
     */
    suspend fun import(source: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(source)?.use {
                it.readBytes().decodeToString()
            } ?: error("Tidak bisa membaca berkas cadangan")

            val root = JSONObject(text)
            require(root.optString("app") == APP_TAG) { "Berkas ini bukan cadangan saveME" }

            val collections = root.optJSONArray("collections").orEmptyObjects().map { it.toCollection() }
            val tags = root.optJSONArray("tags").orEmptyObjects().map { it.toTag() }
            val linkObjects = root.optJSONArray("links").orEmptyObjects()

            // Bersihkan gambar lama sebelum tabelnya dikosongkan.
            db.linkDao().allOnce().forEach {
                fetcher.deleteThumbnail(it.link.imagePath)
                fetcher.deleteThumbnail(it.link.customImagePath)
            }
            db.linkDao().clear()
            db.tagDao().clear()
            db.collectionDao().clear()

            // Induk harus masuk lebih dulu agar foreign key parentId valid.
            val inserted = mutableSetOf<Long>()
            var pending = collections.toMutableList()
            while (pending.isNotEmpty()) {
                val ready = pending.filter { it.parentId == null || it.parentId in inserted }
                if (ready.isEmpty()) {
                    // Induk tidak ditemukan: jadikan koleksi tingkat atas agar tidak hilang.
                    pending.forEach {
                        db.collectionDao().insert(it.copy(parentId = null))
                        inserted += it.id
                    }
                    break
                }
                ready.forEach {
                    db.collectionDao().insert(it)
                    inserted += it.id
                }
                pending = pending.filterNot { it in ready }.toMutableList()
            }

            val tagIdByName = mutableMapOf<String, Long>()
            tags.forEach { tag ->
                db.tagDao().insert(tag)
                tagIdByName[tag.name] = tag.id
            }

            var restored = 0
            linkObjects.forEach { obj ->
                val restoredPreview = obj.optStringOrNull("customPreview")
                    ?.let { runCatching { Base64.decode(it, Base64.NO_WRAP) }.getOrNull() }
                    ?.let { fetcher.storeImageBytes(it) }
                val link = obj.toLink().copy(customImagePath = restoredPreview)
                if (link.collectionId !in inserted) return@forEach
                db.linkDao().insert(link)
                restored++
                obj.optJSONArray("tags")?.let { array ->
                    for (i in 0 until array.length()) {
                        val name = array.optString(i).trim().lowercase()
                        if (name.isEmpty()) continue
                        val tagId = tagIdByName.getOrPut(name) {
                            db.tagDao().insert(TagEntity(name = name))
                                .takeIf { it > 0 }
                                ?: db.tagDao().byName(name)?.id
                                ?: return@forEach
                        }
                        db.tagDao().link(LinkTagCrossRef(linkId = link.id, tagId = tagId))
                    }
                }
            }

            // Thumbnail diunduh ulang di latar belakang dari alamat yang tersimpan.
            repository.backgroundScope().launch {
                db.linkDao().allOnce().forEach { withTags ->
                    val url = withTags.link.imageUrl ?: return@forEach
                    val path = fetcher.downloadThumbnail(url) ?: return@forEach
                    db.linkDao().update(withTags.link.copy(imagePath = path))
                }
            }

            restored
        }
    }

    fun suggestedFileName(): String {
        val stamp = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis())
        return "saveme-backup-$stamp.json"
    }

    // ------------------------------------------------------------ pemetaan

    private fun CollectionEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("colorKey", colorKey)
        put("iconKey", iconKey)
        if (parentId != null) put("parentId", parentId) else put("parentId", JSONObject.NULL)
        put("position", position)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toCollection() = CollectionEntity(
        id = optLong("id"),
        name = optString("name").ifBlank { "Tanpa nama" },
        colorKey = optString("colorKey").ifBlank { "mint" },
        iconKey = optString("iconKey").ifBlank { "folder" },
        parentId = if (isNull("parentId")) null else optLong("parentId"),
        position = optInt("position"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
    )

    private fun TagEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("colorKey", colorKey)
    }

    private fun JSONObject.toTag() = TagEntity(
        id = optLong("id"),
        name = optString("name").lowercase(),
        colorKey = optString("colorKey").ifBlank { "mint" },
    )

    private fun LinkEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("collectionId", collectionId)
        put("url", url)
        put("title", title)
        put("description", description ?: JSONObject.NULL)
        put("siteName", siteName ?: JSONObject.NULL)
        put("authorLine", authorLine ?: JSONObject.NULL)
        put("imageUrl", imageUrl ?: JSONObject.NULL)
        put("isVideo", isVideo)
        put("notes", notes ?: JSONObject.NULL)
        put("isPinned", isPinned)
        put("isRead", isRead)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toLink() = LinkEntity(
        id = optLong("id"),
        collectionId = optLong("collectionId"),
        url = optString("url"),
        title = optString("title").ifBlank { optString("url") },
        description = optStringOrNull("description"),
        siteName = optStringOrNull("siteName"),
        authorLine = optStringOrNull("authorLine"),
        imagePath = null,
        imageUrl = optStringOrNull("imageUrl"),
        isVideo = optBoolean("isVideo"),
        notes = optStringOrNull("notes"),
        isPinned = optBoolean("isPinned"),
        isRead = optBoolean("isRead"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        updatedAt = optLong("updatedAt", System.currentTimeMillis()),
    )

    /** Isi berkas pratinjau pilihan pengguna, dikemas sebagai teks Base64. */
    private fun encodeCustomPreview(path: String?): String? {
        val file = path?.let(::File)?.takeIf { it.exists() } ?: return null
        return runCatching { Base64.encodeToString(file.readBytes(), Base64.NO_WRAP) }.getOrNull()
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).ifBlank { null }

    private fun JSONArray?.orEmptyObjects(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optJSONObject(it) }
    }

    private companion object {
        const val APP_TAG = "saveME"
        const val FORMAT_VERSION = 1
    }
}
