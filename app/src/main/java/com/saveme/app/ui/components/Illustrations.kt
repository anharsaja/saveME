package com.saveme.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saveme.app.ui.theme.CardWhite
import com.saveme.app.ui.theme.Coral
import com.saveme.app.ui.theme.Ink
import com.saveme.app.ui.theme.PaperDim
import com.saveme.app.ui.theme.Sky
import com.saveme.app.ui.theme.Sunny

/**
 * Maskot aplikasi: sebuah penanda kertas kuning bermata dua dengan penjepit
 * kertas merah. Digambar dengan Canvas, bukan aset gambar, supaya tajam di
 * segala ukuran layar.
 */
@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    cheering: Boolean = true,
) {
    // Warna dibaca di sini karena isi Canvas bukan lagi ruang komposisi.
    val ink = Ink
    val body = Sunny
    val sheet = Sky
    val clip = Coral
    val highlight = CardWhite

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 100f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        val lineWidth = 4.2f * s

        // Kaki
        drawLine(ink, p(40f, 76f), p(35f, 91f), lineWidth, StrokeCap.Round)
        drawLine(ink, p(60f, 76f), p(65f, 91f), lineWidth, StrokeCap.Round)
        drawLine(ink, p(29f, 92f), p(39f, 92f), lineWidth, StrokeCap.Round)
        drawLine(ink, p(61f, 92f), p(71f, 92f), lineWidth, StrokeCap.Round)

        // Lengan, terangkat saat sedang bersorak
        val leftHand = if (cheering) p(9f, 34f) else p(11f, 56f)
        val rightHand = if (cheering) p(91f, 34f) else p(89f, 56f)
        drawLine(ink, p(24f, 52f), leftHand, lineWidth, StrokeCap.Round)
        drawLine(ink, p(76f, 52f), rightHand, lineWidth, StrokeCap.Round)
        drawCircle(ink, 5.2f * s, leftHand)
        drawCircle(ink, 5.2f * s, rightHand)

        // Lembar biru yang mengintip di belakang badan
        drawRoundRect(
            color = sheet,
            topLeft = p(31f, 26f),
            size = Size(48f * s, 54f * s),
            cornerRadius = CornerRadius(11f * s, 11f * s),
            style = Fill,
        )
        drawRoundRect(
            color = ink,
            topLeft = p(31f, 26f),
            size = Size(48f * s, 54f * s),
            cornerRadius = CornerRadius(11f * s, 11f * s),
            style = Stroke(width = 4f * s),
        )

        // Badan kuning
        drawRoundRect(
            color = body,
            topLeft = p(24f, 20f),
            size = Size(50f * s, 56f * s),
            cornerRadius = CornerRadius(11f * s, 11f * s),
            style = Fill,
        )
        drawRoundRect(
            color = ink,
            topLeft = p(24f, 20f),
            size = Size(50f * s, 56f * s),
            cornerRadius = CornerRadius(11f * s, 11f * s),
            style = Stroke(width = 4f * s),
        )

        // Wajah
        drawCircle(ink, 3.6f * s, p(39f, 42f))
        drawCircle(ink, 3.6f * s, p(59f, 42f))
        val smile = Path().apply {
            moveTo(40f * s, 54f * s)
            quadraticTo(49f * s, 64f * s, 58f * s, 54f * s)
        }
        drawPath(smile, ink, style = Stroke(width = 3.6f * s, cap = StrokeCap.Round))

        // Penjepit kertas di pojok kiri atas
        drawCircle(ink, 10f * s, p(26f, 15f), style = Stroke(width = 7.5f * s))
        drawCircle(clip, 10f * s, p(26f, 15f), style = Stroke(width = 4.2f * s))
        drawCircle(ink, 4.6f * s, p(26f, 15f), style = Stroke(width = 3f * s))
        drawCircle(highlight, 3.2f * s, p(26f, 15f))
    }
}

/** Ilustrasi map kosong untuk layar koleksi yang belum berisi link. */
@Composable
fun EmptyFolderArt(modifier: Modifier = Modifier, size: Dp = 120.dp) {
    val fill = PaperDim
    val ink = Ink

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension / 100f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)

        // Bayangan lembut di bawah map
        drawOval(
            color = ink.copy(alpha = 0.10f),
            topLeft = p(20f, 78f),
            size = Size(60f * s, 10f * s),
        )

        // Lidah map di belakang
        drawRoundRect(
            color = fill,
            topLeft = p(20f, 20f),
            size = Size(34f * s, 16f * s),
            cornerRadius = CornerRadius(5f * s, 5f * s),
            style = Fill,
        )
        drawRoundRect(
            color = ink.copy(alpha = 0.32f),
            topLeft = p(20f, 20f),
            size = Size(34f * s, 16f * s),
            cornerRadius = CornerRadius(5f * s, 5f * s),
            style = Stroke(width = 2.6f * s),
        )

        // Badan map
        drawRoundRect(
            color = fill,
            topLeft = p(16f, 28f),
            size = Size(68f * s, 52f * s),
            cornerRadius = CornerRadius(8f * s, 8f * s),
            style = Fill,
        )
        drawRoundRect(
            color = ink.copy(alpha = 0.32f),
            topLeft = p(16f, 28f),
            size = Size(68f * s, 52f * s),
            cornerRadius = CornerRadius(8f * s, 8f * s),
            style = Stroke(width = 2.6f * s),
        )
    }
}
