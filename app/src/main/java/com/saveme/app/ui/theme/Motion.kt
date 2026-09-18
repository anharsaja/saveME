package com.saveme.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Satu tempo untuk seluruh aplikasi.
 *
 * Reaksi sentuh dibuat dengan pegas berkekakuan tinggi tanpa pantulan supaya
 * terasa langsung menjawab jari, sementara perpindahan halaman memakai durasi
 * pendek dan tegas. Menaruhnya di satu tempat menjaga semuanya sejalan.
 */
object Motion {

    /** Lama satu halaman menggeser masuk atau keluar. */
    const val ScreenMillis = 200

    /** Untuk perubahan kecil: warna sakelar, pesan sesaat, pudar gambar. */
    const val QuickMillis = 140

    val PressFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    val PressDp = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )

    val ScreenSlide = tween<IntOffset>(durationMillis = ScreenMillis, easing = FastOutSlowInEasing)

    val QuickFloat = tween<Float>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickColor = tween<Color>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    val QuickOffset = tween<IntOffset>(durationMillis = QuickMillis, easing = FastOutSlowInEasing)

    /** Seberapa mengecil sebuah elemen saat ditekan. */
    const val PressScale = 0.95f
}
