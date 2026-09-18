package com.saveme.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CollectionEntity::class,
        LinkEntity::class,
        TagEntity::class,
        LinkTagCrossRef::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao
    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "saveme.db")
                .addCallback(SeedCallback)
                .addMigrations(MIGRATION_1_2)
                .build()

        /** Menambah kolom pratinjau pilihan pengguna tanpa menghapus isi lama. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE links ADD COLUMN customImagePath TEXT")
            }
        }
    }
}

/**
 * Isi awal saat database pertama kali dibuat, supaya aplikasi tidak terbuka kosong.
 * Koleksi ini bebas dihapus atau diganti namanya.
 */
private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val starters = listOf(
            Triple("coding", "mint", "code"),
            Triple("content", "coral", "leaf"),
            Triple("destination", "slate", "pin"),
        )
        starters.forEachIndexed { index, (name, color, icon) ->
            db.execSQL(
                "INSERT INTO collections (name, colorKey, iconKey, parentId, position, createdAt) VALUES (?, ?, ?, NULL, ?, ?)",
                arrayOf<Any>(name, color, icon, index, now + index),
            )
        }
    }
}
