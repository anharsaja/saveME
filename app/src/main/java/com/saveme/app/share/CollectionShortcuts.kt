package com.saveme.app.share

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.saveme.app.R
import com.saveme.app.ShareTargetActivity
import com.saveme.app.data.db.CollectionSummary
import com.saveme.app.data.repo.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Menerbitkan tiap koleksi sebagai tujuan berbagi Android.
 *
 * Hasilnya, saat menekan *Share* di Instagram atau TikTok, lembar berbagi sudah
 * memuat "saveME · coding", "saveME · content", dan seterusnya — satu ketukan
 * langsung menyimpan tanpa perlu memilih koleksi lagi.
 *
 * Baris tujuan langsung ini disediakan sistem sejak Android 10. Di versi yang
 * lebih lama saveME tetap muncul seperti biasa di daftar aplikasi.
 */
object CollectionShortcuts {

    const val CATEGORY = "com.saveme.app.category.SAVE_LINK"

    private const val ID_PREFIX = "collection-"
    private const val MAX_SHORTCUTS = 4

    /** Mengikuti perubahan daftar koleksi dan menyegarkan pintasannya. */
    fun keepInSync(context: Context, repository: LibraryRepository, scope: CoroutineScope) {
        scope.launch {
            repository.rootCollections().collectLatest { publish(context, it) }
        }
    }

    fun collectionIdFrom(shortcutId: String?): Long? =
        shortcutId?.takeIf { it.startsWith(ID_PREFIX) }?.removePrefix(ID_PREFIX)?.toLongOrNull()

    fun reportUsed(context: Context, shortcutId: String?) {
        if (shortcutId.isNullOrBlank()) return
        runCatching { ShortcutManagerCompat.reportShortcutUsed(context, shortcutId) }
    }

    private fun publish(context: Context, collections: List<CollectionSummary>) {
        val shortcuts = collections.take(MAX_SHORTCUTS).map { summary ->
            val collection = summary.collection
            ShortcutInfoCompat.Builder(context, ID_PREFIX + collection.id)
                .setShortLabel(collection.name)
                .setLongLabel("Save to ${collection.name}")
                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                .setCategories(setOf(CATEGORY))
                // Wajib agar pintasan tetap bisa dipakai lembar berbagi walau
                // sudah tidak lagi berada di daftar dinamis.
                .setLongLived(true)
                .setIntent(
                    Intent(context, ShareTargetActivity::class.java).setAction(Intent.ACTION_VIEW),
                )
                .build()
        }
        runCatching { ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts) }
    }
}
