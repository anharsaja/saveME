package com.saveme.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Satu tempo untuk seluruh aplikasi.
 *
 * Reaksi sentuh dibagi dua arah yang sengaja tidak sama. Turunnya nyaris
 * seketika supaya tombol sudah benar-benar tertekan sebelum jari sempat
 * terangkat; naiknya sedikit memantul supaya terasa seperti per yang dilepas.
 * Perpindahan halaman memakai durasi pendek dan tegas, tapi diberi jeda kecil
 * di depan supaya tidak menimpa pantulan tombol yang memanggilnya. Menaruh
 * semuanya di satu tempat menjaga tempo aplikasi tetap sejalan.
 */
object Motion {

    /** Lama satu halaman menggeser masuk atau keluar. */
    const val ScreenMillis = 200

    /**
     * Jeda sebelum halaman mulai bergeser.
     *
     * Yang ditahan hanya gerakannya — perpindahannya sendiri tetap terjadi
     * seketika saat diketuk. Selama jeda ini layar lama diam di tempat, jadi
     * tombol yang baru diketuk sempat terlihat memantul naik lebih dulu, baru
     * setelah itu halaman menggeser. Urutannya terbaca sebagai sebab lalu
     * akibat, bukan sebagai jeda kosong; dan halaman tujuan kebagian waktu
     * menyusun diri sebelum terlihat.
     *
     * Tidak dipakai untuk kembali lewat tombol atau usapan sistem: di sana
     * tidak ada tombol yang memantul, sehingga diamnya layar hanya akan
     * terasa lambat.
     */
    const val ScreenDelayMillis = 90

    /** Untuk perubahan kecil: warna sakelar, pesan sesaat, pudar gambar. */
    const val QuickMillis = 140

    /**
     * Lama tekanan turun sampai penuh.
     *
     * Sengaja jauh lebih pendek dari satu ketukan (sekitar 80 md) supaya
     * bentuk tertekan sudah tercapai utuh, bukan tertangkap setengah jalan.
     */
    const val PressInMillis = 45

    /**
     * Tekanan ditahan selama ini meski jari sudah terangkat.
     *
     * Ketukan yang tergesa bisa terlepas sebelum [PressInMillis] habis;
     * penahan ini memastikan bentuk tertekannya sempat utuh dan diam sejenak
     * sebelum melenting balik. Angkanya dipasangkan dengan
     * [ScreenDelayMillis]: tekanan terlepas sekitar sepuluh milidetik setelah
     * jari terangkat, dan halaman baru bergeser puluhan milidetik sesudahnya —
     * cukup untuk melihat pantulannya sampai tuntas.
     */
    const val PressHoldMillis = 90L

    val PressInFloat = tween<Float>(durationMillis = PressInMillis, easing = LinearOutSlowInEasing)
    val PressInDp = tween<Dp>(durationMillis = PressInMillis, easing = LinearOutSlowInEasing)

    val PressOutFloat = spring<Float>(dampingRatio = 0.8f, stiffness = 900f)
    val PressOutDp = spring<Dp>(dampingRatio = 0.8f, stiffness = 900f)

    /** Geseran halaman yang dipicu tombol di dalam aplikasi — pakai jeda. */
    val ScreenSlide = tween<IntOffset>(
        durationMillis = ScreenMillis,
        delayMillis = ScreenDelayMillis,
        easing = FastOutSlowInEasing,
    )

    /**
     * Geseran yang sama tanpa jeda, untuk kembali lewat tombol atau usapan
     * sistem. Gerakan itu harus menempel pada jari, bukan menunggu.
     */
    val ScreenSlideGesture = tween<IntOffset>(
        durationMillis = ScreenMillis,
        easing = FastOutSlowInEasing,
    )

    val QuickFloat = tween<Float>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickColor = tween<Color>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickOffset = tween<IntOffset>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickDp = spring<Dp>(dampingRatio = 0.8f, stiffness = 900f)

    /** Seberapa mengecil sebuah elemen saat ditekan. */
    const val PressScale = 0.94f
}
