package com.saveme.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saveme.app.data.backup.BackupManager
import com.saveme.app.data.prefs.SettingsStore
import com.saveme.app.data.repo.LibraryRepository
import com.saveme.app.share.CollectionShortcuts
import com.saveme.app.work.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Wadah sederhana untuk komponen berumur panjang. Aplikasi ini kecil dan tanpa
 * jaringan berlapis, jadi tidak perlu kerangka injeksi terpisah.
 */
class SaveMeApp : Application(), SingletonImageLoader.Factory {

    val repository: LibraryRepository by lazy { LibraryRepository(this) }
    val settings: SettingsStore by lazy { SettingsStore(this) }
    val backup: BackupManager by lazy { BackupManager(this, repository) }

    /** Untuk kerja ringan yang hidup selama aplikasi berjalan. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.ensureChannel(this)
        CollectionShortcuts.keepInSync(this, repository, appScope)
    }

    /** Pemuat gambar untuk thumbnail yang tersimpan di folder internal. */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(com.saveme.app.ui.theme.Motion.QuickMillis)
            .build()
}

/** Pintasan membuat ViewModel tanpa menulis Factory satu per satu. */
inline fun <reified VM : ViewModel> appViewModelFactory(crossinline create: (SaveMeApp) -> VM) = viewModelFactory {
    initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SaveMeApp
        create(app)
    }
}

