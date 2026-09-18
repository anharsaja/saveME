package com.saveme.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

private const val SUMMARY_COLUMNS = """
    c.*,
    (SELECT COUNT(*) FROM links l WHERE l.collectionId = c.id) AS linkCount,
    (SELECT COUNT(*) FROM collections s WHERE s.parentId = c.id) AS childCount
"""

@Dao
interface CollectionDao {

    @Query("SELECT $SUMMARY_COLUMNS FROM collections c WHERE c.parentId IS NULL ORDER BY c.position ASC, c.createdAt ASC")
    fun observeRoots(): Flow<List<CollectionSummary>>

    @Query("SELECT $SUMMARY_COLUMNS FROM collections c WHERE c.parentId = :parentId ORDER BY c.position ASC, c.createdAt ASC")
    fun observeChildren(parentId: Long): Flow<List<CollectionSummary>>

    @Query("SELECT * FROM collections ORDER BY position ASC, createdAt ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections ORDER BY position ASC, createdAt ASC")
    suspend fun allOnce(): List<CollectionEntity>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun observeById(id: Long): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun byId(id: Long): CollectionEntity?

    @Query("SELECT $SUMMARY_COLUMNS FROM collections c WHERE c.name LIKE '%' || :q || '%' ORDER BY c.name ASC")
    fun search(q: String): Flow<List<CollectionSummary>>

    @Query("SELECT COUNT(*) FROM collections")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM collections WHERE (:parentId IS NULL AND parentId IS NULL) OR parentId = :parentId")
    suspend fun nextPosition(parentId: Long?): Int

    @Insert
    suspend fun insert(collection: CollectionEntity): Long

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("DELETE FROM collections")
    suspend fun clear()
}

@Dao
interface LinkDao {

    @Transaction
    @Query("SELECT * FROM links WHERE collectionId = :collectionId ORDER BY isPinned DESC, createdAt DESC")
    fun observeInCollection(collectionId: Long): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE isRead = 0 ORDER BY createdAt ASC")
    fun observeUnread(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE id = :id")
    fun observeById(id: Long): Flow<LinkWithTags?>

    @Transaction
    @Query(
        """
        SELECT * FROM links
        WHERE title LIKE '%' || :q || '%'
           OR url LIKE '%' || :q || '%'
           OR COALESCE(description, '') LIKE '%' || :q || '%'
           OR COALESCE(notes, '') LIKE '%' || :q || '%'
           OR COALESCE(siteName, '') LIKE '%' || :q || '%'
        ORDER BY isPinned DESC, createdAt DESC
        """,
    )
    fun search(q: String): Flow<List<LinkWithTags>>

    @Transaction
    @Query(
        """
        SELECT l.* FROM links l
        JOIN link_tags lt ON lt.linkId = l.id
        WHERE lt.tagId = :tagId
        ORDER BY l.isPinned DESC, l.createdAt DESC
        """,
    )
    fun observeByTag(tagId: Long): Flow<List<LinkWithTags>>

    @Query("SELECT * FROM links WHERE id = :id")
    suspend fun byId(id: Long): LinkEntity?

    @Transaction
    @Query("SELECT * FROM links ORDER BY createdAt ASC")
    suspend fun allOnce(): List<LinkWithTags>

    @Query("SELECT COUNT(*) FROM links")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM links WHERE isPinned = 1")
    fun observePinnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM links WHERE isRead = 0")
    suspend fun unreadCount(): Int

    @Query("SELECT * FROM links WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): LinkEntity?

    @Insert
    suspend fun insert(link: LinkEntity): Long

    @Update
    suspend fun update(link: LinkEntity)

    @Delete
    suspend fun delete(link: LinkEntity)

    @Query("DELETE FROM links WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("UPDATE links SET collectionId = :collectionId, updatedAt = :now WHERE id IN (:ids)")
    suspend fun moveToCollection(ids: List<Long>, collectionId: Long, now: Long)

    @Query("UPDATE links SET isPinned = :pinned, updatedAt = :now WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, now: Long)

    @Query("UPDATE links SET isRead = :read, updatedAt = :now WHERE id = :id")
    suspend fun setRead(id: Long, read: Boolean, now: Long)

    @Query("UPDATE links SET notes = :notes, updatedAt = :now WHERE id = :id")
    suspend fun setNotes(id: Long, notes: String?, now: Long)

    @Query("DELETE FROM links")
    suspend fun clear()
}

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun allOnce(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun byName(name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun byId(id: Long): TagEntity?

    @Query("SELECT COUNT(*) FROM tags")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun link(ref: LinkTagCrossRef)

    @Query("DELETE FROM link_tags WHERE linkId = :linkId AND tagId = :tagId")
    suspend fun unlink(linkId: Long, tagId: Long)

    @Query("SELECT * FROM link_tags")
    suspend fun allRefsOnce(): List<LinkTagCrossRef>

    /** Tag yang tidak lagi dipakai link mana pun ikut dibersihkan. */
    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tagId FROM link_tags)")
    suspend fun deleteOrphans()

    @Query("DELETE FROM tags")
    suspend fun clear()
}
