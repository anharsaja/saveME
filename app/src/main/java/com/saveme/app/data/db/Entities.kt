package com.saveme.app.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.File

@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId")],
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String = "mint",
    val iconKey: String = "folder",
    /** Null berarti koleksi tingkat atas. Subkoleksi menunjuk ke induknya. */
    val parentId: Long? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "links",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("collectionId")],
)
data class LinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collectionId: Long,
    val url: String,
    val title: String,
    val description: String? = null,
    /** Nama situs asal, misalnya "Instagram". */
    val siteName: String? = null,
    /** Baris meta dari halaman asal: penulis, tanggal, jumlah suka. */
    val authorLine: String? = null,
    /** Path absolut thumbnail yang sudah diunduh ke penyimpanan internal. */
    val imagePath: String? = null,
    /** Alamat asal thumbnail, disimpan agar cadangan bisa mengunduhnya ulang. */
    val imageUrl: String? = null,
    /**
     * Gambar pratinjau pilihan pengguna sendiri. Bila terisi, gambar ini yang
     * dipakai; mengosongkannya mengembalikan pratinjau bawaan dari kontennya.
     */
    val customImagePath: String? = null,
    val isVideo: Boolean = false,
    val notes: String? = null,
    val isPinned: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String = "mint",
)

@Entity(
    tableName = "link_tags",
    primaryKeys = ["linkId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = LinkEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tagId")],
)
data class LinkTagCrossRef(
    val linkId: Long,
    val tagId: Long,
)

/**
 * Berkas gambar yang dipakai sebagai pratinjau: pilihan pengguna lebih dulu,
 * baru hasil unduhan dari halaman aslinya. Mengembalikan null bila keduanya
 * tidak ada atau berkasnya sudah hilang.
 */
fun LinkEntity.previewFile(): File? =
    sequenceOf(customImagePath, imagePath)
        .filterNotNull()
        .map(::File)
        .firstOrNull { it.exists() }

/** Benar bila pengguna sudah mengganti pratinjaunya sendiri. */
fun LinkEntity.hasCustomPreview(): Boolean =
    customImagePath?.let { File(it).exists() } == true

/** Satu link beserta tag-tagnya, dipakai di kartu dan layar detail. */
data class LinkWithTags(
    @Embedded val link: LinkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LinkTagCrossRef::class,
            parentColumn = "linkId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity> = emptyList(),
)

/** Koleksi plus jumlah isinya, supaya kartu di Home tidak perlu query terpisah. */
data class CollectionSummary(
    @Embedded val collection: CollectionEntity,
    val linkCount: Int,
    val childCount: Int,
)

/** Angka-angka untuk kartu "YOUR LIBRARY" di layar pencarian. */
data class LibraryStats(
    val collections: Int,
    val links: Int,
    val pinned: Int,
    val tags: Int,
)
