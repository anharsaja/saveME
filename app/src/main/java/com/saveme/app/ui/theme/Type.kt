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
 * Archivo dipakai sebagai satu-satunya family: grotesk bersudut tegak dengan
 * bobot sampai 900 — huruf khas neo-brutalism, jauh dari kesan bulat dan lembut.
 * File-nya variable font, jadi tiap bobot diminta lewat sumbu `wght` alih-alih
 * menyertakan satu file per bobot.
 */
private fun archivo(weight: Int) = Font(
    resId = R.font.archivo,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Archivo = FontFamily(
    archivo(400),
    archivo(500),
    archivo(600),
    archivo(700),
    archivo(800),
    archivo(900),
)

/**
 * Judul besar dua baris di layar Home. Bobot paling tebal dengan jarak huruf
 * dirapatkan sampai nyaris bersentuhan — blok teks yang pekat, bukan kalimat.
 */
val DisplayStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W900,
    fontSize = 34.sp,
    lineHeight = 37.sp,
    letterSpacing = (-1.4).sp,
)

/** Judul di top bar layar dalam. */
val ScreenTitleStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W800,
    fontSize = 21.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.6).sp,
)

/** "My Collections" dan judul seksi besar lainnya. */
val SectionTitleStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W900,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.5).sp,
)

/** Label kapital kecil: ALL LINKS (4), PREFERENCES, NOTES, ... */
val OverlineStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W900,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.4.sp,
)

/** Judul kartu link / nama koleksi. */
val CardTitleStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W700,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.2).sp,
)

val BodyStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    letterSpacing = (-0.1).sp,
)

val CaptionStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W600,
    fontSize = 13.sp,
    lineHeight = 17.sp,
)

/** Teks di dalam tombol utama — selalu tampil kapital, lihat NeoButton. */
val ButtonStyle = TextStyle(
    fontFamily = Archivo,
    fontWeight = FontWeight.W800,
    fontSize = 15.sp,
    lineHeight = 19.sp,
    textAlign = TextAlign.Center,
    letterSpacing = 0.6.sp,
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
