package com.saveme.app.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.saveme.app.ui.theme.Motion
import com.saveme.app.util.LocalHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Status tertekan sebuah tombol, dibaca langsung dari jari.
 *
 * Bacaan ini sengaja tidak memakai `InteractionSource` milik Compose. Di dalam
 * wadah yang bisa digulir — dan di aplikasi ini hampir semua tombol ada di
 * dalamnya, termasuk bilah atas Home yang ikut jadi isi `LazyVerticalGrid` —
 * `clickable` menunda status tertekannya selama `TapIndicationDelay`, yaitu
 * 100 md. Tujuannya baik: menggulir daftar jadi tidak memunculkan kedipan
 * tombol di sepanjang jalan. Tapi satu ketukan biasa cuma sekitar 80 md, jadi
 * yang terjadi di aplikasi ini adalah:
 *
 * - ketukan gesit (di bawah 100 md) — tekanannya baru dikirim *setelah* jari
 *   terangkat, sebagai sepasang tekan-lepas beruntun, sehingga tombol terasa
 *   mati saat disentuh dan animasinya menyusul belakangan;
 * - ketukan santai (di atas 100 md) — tekanannya muncul terlambat 100 md.
 *
 * Dua-duanya salah, dan mana yang kena tergantung secepat apa jari bergerak.
 * Itulah sebabnya animasinya terasa kadang ada kadang tidak.
 *
 * Di sini tekanan dimulai tepat saat jari menyentuh, tanpa jeda sama sekali.
 * Sebagai gantinya, geseran yang berubah jadi gulir dibatalkan sendiri lewat
 * [waitForUpOrCancellation], jadi kedipan yang ingin dihindari Compose tetap
 * tidak terjadi — hanya diputuskan belakangan, bukan dengan menunggu di depan.
 */
@Stable
class NeoPress internal constructor(
    private val scope: CoroutineScope,
    private val holdMillis: Long,
) {

    /** True selama tombol perlu tampak tertekan. */
    var pressed by mutableStateOf(false)
        private set

    private var pressedAt = 0L
    private var release: Job? = null

    internal fun down() {
        // Ketukan susulan membatalkan pelepasan yang masih menunggu, jadi
        // mengetuk berkali-kali tidak menimbulkan kedipan.
        release?.cancel()
        release = null
        pressedAt = SystemClock.uptimeMillis()
        pressed = true
    }

    /**
     * [completed] bernilai true bila jari benar-benar terangkat di atas tombol,
     * dan false bila gerakannya keburu direbut daftar di belakangnya untuk
     * menggulir. Yang batal dilepas saat itu juga: tidak ada ketukan yang
     * perlu sempat terlihat.
     */
    internal fun up(completed: Boolean) {
        if (!pressed) return
        release?.cancel()
        if (!completed) {
            release = null
            pressed = false
            return
        }
        val elapsed = SystemClock.uptimeMillis() - pressedAt
        release = scope.launch {
            if (elapsed < holdMillis) delay(holdMillis - elapsed)
            pressed = false
        }
    }
}

@Composable
fun rememberNeoPress(holdMillis: Long = Motion.PressHoldMillis): NeoPress {
    val scope = rememberCoroutineScope()
    return remember(scope, holdMillis) { NeoPress(scope, holdMillis) }
}

/**
 * Menyalakan [press] dari kejadian sentuh mentah.
 *
 * Dipasang paling akhir dalam rantai modifier supaya menjadi lapisan terdalam:
 * pada giliran [androidx.compose.ui.input.pointer.PointerEventPass.Main],
 * lapisan terdalam dilayani lebih dulu, jadi jari yang terangkat sudah terbaca
 * di sini sebelum `combinedClickable` mengakuinya sebagai ketukan.
 *
 * Tidak ada satu pun kejadian yang dikonsumsi di sini — pembacaan ini menumpang
 * saja, dan tidak mengubah siapa yang berhak atas gerakannya.
 *
 * Sengaja tidak dimatikan saat tombol nonaktif: kalau lapisannya ikut dicabut
 * di tengah sentuhan, tekanan yang sedang berjalan tidak akan pernah dilepas.
 * Yang menyaring tampilan adalah pemakainya, lewat `enabled` masing-masing.
 */
fun Modifier.trackNeoPress(press: NeoPress): Modifier = pointerInput(press) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        press.down()
        press.up(completed = waitForUpOrCancellation() != null)
    }
}

/**
 * Klik tanpa ripple — riak Material akan merusak tampilan blok warna datar di
 * desain ini. Sebagai gantinya elemen mengecil sedikit selama ditekan, sehingga
 * apa pun yang bisa diketuk memberi jawaban yang sama.
 *
 * [scaleOnPress] dimatikan oleh permukaan yang sudah punya gerakan tekannya
 * sendiri, misalnya [NeoSurface] yang isinya bergeser menimpa bayangan; di sana
 * [press] dititipkan dari luar supaya keduanya bergerak dari satu sumber.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.neoClickable(
    press: NeoPress? = null,
    enabled: Boolean = true,
    scaleOnPress: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    val tracker = press ?: rememberNeoPress()
    val haptics = LocalHaptics.current
    val down = tracker.pressed && enabled && scaleOnPress
    val scale by animateFloatAsState(
        targetValue = if (down) Motion.PressScale else 1f,
        animationSpec = if (down) Motion.PressInFloat else Motion.PressOutFloat,
        label = "pressScale",
    )

    return this
        .then(
            if (scaleOnPress) {
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            } else {
                Modifier
            },
        )
        .combinedClickable(
            interactionSource = null,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick?.let { action -> { haptics.longPress(); action() } },
            onClick = { haptics.tick(); onClick() },
        )
        .trackNeoPress(tracker)
}
