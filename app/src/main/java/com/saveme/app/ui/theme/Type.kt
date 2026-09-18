package com.saveme.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.saveme.app.R

/**
 * Nunito dipakai sebagai satu-satunya family. File-nya variable font, jadi tiap bobot
 * diminta lewat sumbu `wght` alih-alih menyertakan satu file per bobot.
 */
private fun nunito(weight: Int) = Font(
    resId = R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Nunito = FontFamily(
    nunito(400),
    nunito(500),
    nunito(600),
    nunito(700),
    nunito(800),
    nunito(900),
)

/** Judul besar dua baris di layar Home. */
val DisplayStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W800,
    fontSize = 34.sp,
    lineHeight = 39.sp,
    letterSpacing = (-0.8).sp,
)

/** Judul di top bar layar dalam. */
val ScreenTitleStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W800,
    fontSize = 21.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.3).sp,
)

/** "My Collections" dan judul seksi besar lainnya. */
val SectionTitleStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W800,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.3).sp,
)

/** Label kapital kecil: ALL LINKS (4), PREFERENCES, NOTES, ... */
val OverlineStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W900,
    fontSize = 13.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.9.sp,
)

/** Judul kartu link / nama koleksi. */
val CardTitleStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W800,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.1).sp,
)

val BodyStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W700,
    fontSize = 15.sp,
    lineHeight = 22.sp,
)

val CaptionStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W700,
    fontSize = 13.sp,
    lineHeight = 18.sp,
)

/** Teks di dalam tombol utama. */
val ButtonStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.W800,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    textAlign = TextAlign.Center,
    letterSpacing = (-0.2).sp,
)

val SaveMeTypography = Typography(
    displayLarge = DisplayStyle,
    headlineMedium = ScreenTitleStyle,
    titleLarge = SectionTitleStyle,
    titleMedium = CardTitleStyle,
    labelLarge = ButtonStyle,
    labelMedium = OverlineStyle,
    bodyMedium = BodyStyle,
    bodySmall = CaptionStyle,
)
