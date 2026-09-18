package com.saveme.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.Sky
import com.saveme.app.ui.theme.Sunny

/**
 * Lambang aplikasi: dua mata rantai saling mengait, satu biru langit dan satu
 * kuning, bergaris hitam tebal dengan kilau kecil di dua sudut — sama seperti
 * berkas di folder `logo/`. Digambar dengan Canvas, bukan aset gambar, supaya
 * tajam di segala ukuran layar dan ikut berganti bersama tema.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    // Warna dibaca di sini karena isi Canvas bukan lagi ruang komposisi.
    val ink = Ink
    val upper = Sky
    val lower = Sunny

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 100f

        // Mata rantai bawah digambar lebih dulu supaya yang atas menimpanya
        // di titik silang, persis seperti rantai sungguhan.
        chainLink(center = Offset(40f * s, 60f * s), unit = s, tint = lower, ink = ink)
        chainLink(center = Offset(60f * s, 40f * s), unit = s, tint = upper, ink = ink)

        // Kilau: tiga goresan di kiri atas, tiga lagi memantul di kanan bawah.
        val sparkle = 4.2f * s
        fun spark(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = ink,
                start = Offset(x1 * s, y1 * s),
                end = Offset(x2 * s, y2 * s),
                strokeWidth = sparkle,
                cap = StrokeCap.Round,
            )
        }
        spark(34f, 6f, 34f, 19f)
        spark(16f, 16f, 25f, 25f)
        spark(7f, 34f, 20f, 34f)
        spark(66f, 81f, 66f, 94f)
        spark(75f, 75f, 84f, 84f)
        spark(80f, 66f, 93f, 66f)
    }
}

/**
 * Satu mata rantai: kapsul berlubang miring 45 derajat, digambar sebagai garis
 * tebal berwarna yang diapit dua garis hitam. Cara ini menyisakan lubang di
 * tengah tanpa perlu tahu warna apa yang ada di belakangnya.
 */
private fun DrawScope.chainLink(center: Offset, unit: Float, tint: Color, ink: Color) {
    val length = 44f * unit
    val thickness = 26f * unit
    val band = 8f * unit
    val outline = 2.8f * unit

    rotate(degrees = -45f, pivot = center) {
        val topLeft = Offset(center.x - length / 2f, center.y - thickness / 2f)
        val boxSize = Size(length, thickness)
        val radius = CornerRadius(thickness / 2f, thickness / 2f)

        drawRoundRect(
            color = ink,
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = radius,
            style = Stroke(width = band + outline * 2f),
        )
        drawRoundRect(
            color = tint,
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = radius,
            style = Stroke(width = band),
        )
    }
}

/**
 * Ilustrasi map kosong untuk layar koleksi yang belum berisi link. Bergaris
 * tebal dengan bayangan pejal, satu bahasa dengan kartu di sekitarnya.
 */
@Composable
fun EmptyFolderArt(modifier: Modifier = Modifier, size: Dp = 120.dp) {
    val fill = PaperDim
    val ink = Ink

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 100f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        val corner = CornerRadius(4f * s, 4f * s)
        val line = 3.4f * s

        // Bayangan pejal, digeser ke kanan bawah seperti permukaan lain.
        drawRoundRect(
            color = ink,
            topLeft = p(22f, 33f),
            size = Size(68f * s, 52f * s),
            cornerRadius = corner,
            style = Fill,
        )

        // Lidah map di belakang
        drawRoundRect(
            color = fill,
            topLeft = p(16f, 19f),
            size = Size(34f * s, 16f * s),
            cornerRadius = corner,
            style = Fill,
        )
        drawRoundRect(
            color = ink,
            topLeft = p(16f, 19f),
            size = Size(34f * s, 16f * s),
            cornerRadius = corner,
            style = Stroke(width = line),
        )

        // Badan map
        drawRoundRect(
            color = fill,
            topLeft = p(16f, 28f),
            size = Size(68f * s, 52f * s),
            cornerRadius = corner,
            style = Fill,
        )
        drawRoundRect(
            color = ink,
            topLeft = p(16f, 28f),
            size = Size(68f * s, 52f * s),
            cornerRadius = corner,
            style = Stroke(width = line),
        )
    }
}
