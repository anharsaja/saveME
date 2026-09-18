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
 * Perpindahan halaman memakai durasi pendek dan tegas. Menaruh semuanya di satu
 * tempat menjaga tempo aplikasi tetap sejalan.
 */
object Motion {

    /** Lama satu halaman menggeser masuk atau keluar. */
    const val ScreenMillis = 200

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
     * Tanpa penahan, tombol yang diketuk untuk pindah halaman melepas tekanannya
     * di milidetik yang sama dengan mulainya geseran halaman — gerakannya ada,
     * tapi tidak pernah sempat terbaca mata.
     */
    const val PressHoldMillis = 110L

    val PressInFloat = tween<Float>(durationMillis = PressInMillis, easing = LinearOutSlowInEasing)
    val PressInDp = tween<Dp>(durationMillis = PressInMillis, easing = LinearOutSlowInEasing)

    val PressOutFloat = spring<Float>(dampingRatio = 0.8f, stiffness = 900f)
    val PressOutDp = spring<Dp>(dampingRatio = 0.8f, stiffness = 900f)

    val ScreenSlide = tween<IntOffset>(durationMillis = ScreenMillis, easing = FastOutSlowInEasing)

    val QuickFloat = tween<Float>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickColor = tween<Color>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickOffset = tween<IntOffset>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickDp = spring<Dp>(dampingRatio = 0.8f, stiffness = 900f)

    /** Seberapa mengecil sebuah elemen saat ditekan. */
    const val PressScale = 0.94f
}
