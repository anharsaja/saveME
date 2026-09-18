package com.saveme.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saveme.app.data.repo.LibraryRepository
import com.saveme.app.share.CollectionShortcuts
import com.saveme.app.ui.components.CollectionPickerSheet
import com.saveme.app.ui.components.neoClickable
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.Motion
import com.saveme.app.ui.theme.SaveMeTheme
import com.saveme.app.util.UrlUtil
import kotlinx.coroutines.delay

/**
 * Tujuan lembar berbagi Android. Layar ini tembus pandang dan hanya menampilkan
 * pemilih koleksi, lalu menutup diri — sehingga setelah menyimpan, pengguna
 * kembali ke aplikasi asal alih-alih terdampar di dalam saveME.
 */
class ShareTargetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = UrlUtil.extractFirstUrl(intent?.getStringExtra(Intent.EXTRA_TEXT))
        if (url == null) {
            toast("No link found in what you shared")
            finish()
            return
        }

        val app = applicationContext as SaveMeApp

        // Dibuka lewat pintasan koleksi di lembar berbagi: tujuannya sudah
        // ditentukan, jadi simpan saja tanpa menampilkan pemilih.
        val shortcutId = intent?.getStringExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID)
        val presetCollectionId = CollectionShortcuts.collectionIdFrom(shortcutId)
        if (presetCollectionId != null) {
            CollectionShortcuts.reportUsed(this, shortcutId)
            app.repository.saveLinkDetached(url, presetCollectionId) { toast(describe(it)) }
            finish()
            return
        }

        setContent {
            SaveMeTheme (darkTheme = isSystemInDarkTheme()) {
                val collections by app.repository.allCollections()
                    .collectAsStateWithLifecycle(initialValue = emptyList())

                // Layar ini tidak punya geseran halaman yang bisa ditahan:
                // begitu ditutup ia lenyap seketika, bersama animasi tekan
                // koleksi yang baru saja diketuk. Jadi penutupannya sendiri
                // yang diberi jeda, sepanjang jeda geseran di dalam aplikasi.
                var closing by remember { mutableStateOf(false) }
                LaunchedEffect(closing) {
                    if (closing) {
                        delay(Motion.ScreenDelayMillis.toLong())
                        finish()
                    }
                }

                // Peredup digambar sendiri, bukan lewat jendela dialog, supaya
                // aplikasi asal di belakang terlihat redup dan pemilihnya
                // terasa menempel di layar ini.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Ink.copy(alpha = 0.55f))
                        // Tanpa gerakan tekan: peredam selebar layar yang ikut
                        // mengecil akan menyingkapkan tepi terang di sekelilingnya.
                        .neoClickable(scaleOnPress = false) { closing = true },
                    contentAlignment = Alignment.Center,
                ) {
                    CollectionPickerSheet(
                        title = "Save to",
                        collections = collections,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .neoClickable(scaleOnPress = false) { /* menelan ketukan */ },
                        onDismiss = { closing = true },
                        onPick = { id ->
                            // Selama jeda tadi pemilihnya masih terlihat dan
                            // masih menerima ketukan. Penjaga ini memastikan
                            // satu kali berbagi tetap menyimpan satu tautan.
                            if (!closing) {
                                val name = collections.firstOrNull { it.id == id }?.name.orEmpty()
                                app.repository.saveLinkDetached(url, id) { result ->
                                    toast(describe(result, name))
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    private fun describe(
        result: LibraryRepository.SaveResult,
        collectionName: String? = null,
    ): String = when (result) {
        is LibraryRepository.SaveResult.Saved ->
            collectionName?.takeIf { it.isNotBlank() }?.let { "Saved to $it" } ?: "Saved"

        is LibraryRepository.SaveResult.Duplicate ->
            result.collectionName?.let { "Already saved in $it" } ?: "Already in your library"

        LibraryRepository.SaveResult.InvalidUrl -> "That link could not be read"
    }

    /** Memakai konteks aplikasi karena layar ini sudah tertutup saat pesan muncul. */
    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
