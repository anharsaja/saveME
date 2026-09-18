package com.saveme.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Warna dasar aplikasi dalam dua rupa: terang dan gelap.
 *
 * Warna aksen (mint, coral, dan kawan-kawan) tidak ikut berganti karena sudah
 * cukup pekat untuk keduanya; yang bertukar hanyalah kertas, tinta, dan kartu.
 */
@Immutable
data class SaveMePalette(
    val paper: Color,
    val paperDim: Color,
    val card: Color,
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    val sunny: Color,
    val sunnyDeep: Color,
    val sunnySoft: Color,
    val danger: Color,
    val isDark: Boolean,
)

val LightPalette = SaveMePalette(
    paper = Color(0xFFF4F4F2),
    paperDim = Color(0xFFE9E9E6),
    card = Color(0xFFFFFFFF),
    ink = Color(0xFF111111),
    // Abu-abu sengaja dibuat lebih gelap dari kelabu biasa supaya teks kecil
    // tetap terbaca oleh mata yang kurang tajam.
    inkSoft = Color(0xFF474747),
    inkFaint = Color(0xFF6E6E6C),
    sunny = Color(0xFFF8C744),
    sunnyDeep = Color(0xFFEFB933),
    sunnySoft = Color(0xFFFDF2D9),
    danger = Color(0xFFFF2D6F),
    isDark = false,
)

val DarkPalette = SaveMePalette(
    paper = Color(0xFF15151A),
    paperDim = Color(0xFF2B2B33),
    card = Color(0xFF1E1E25),
    ink = Color(0xFFF3F3ED),
    inkSoft = Color(0xFFC2C2BC),
    inkFaint = Color(0xFF90908B),
    sunny = Color(0xFFF8C744),
    sunnyDeep = Color(0xFFEFB933),
    sunnySoft = Color(0xFF3C3324),
    danger = Color(0xFFFF4D82),
    isDark = true,
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

// Nama-nama di bawah ini dibaca saat komposisi, jadi seluruh layar ikut berganti
// begitu palet diganti tanpa perlu menyebut LocalPalette di tiap berkas.
val Paper: Color @Composable get() = LocalPalette.current.paper
val PaperDim: Color @Composable get() = LocalPalette.current.paperDim
val CardWhite: Color @Composable get() = LocalPalette.current.card
val Ink: Color @Composable get() = LocalPalette.current.ink
val InkSoft: Color @Composable get() = LocalPalette.current.inkSoft
val InkFaint: Color @Composable get() = LocalPalette.current.inkFaint
val Sunny: Color @Composable get() = LocalPalette.current.sunny
val SunnyDeep: Color @Composable get() = LocalPalette.current.sunnyDeep
val SunnySoft: Color @Composable get() = LocalPalette.current.sunnySoft
val Danger: Color @Composable get() = LocalPalette.current.danger

/** Tinta gelap yang dipakai di atas bidang berwarna, sama di kedua tema. */
val OnAccent = Color(0xFF111111)

private val LightInk = Color(0xFFF5F5F0)

/**
 * Warna teks atau ikon yang terbaca di atas [background].
 *
 * Ambangnya lebih rendah dari setengah karena aksen kita — mint, coral, slate —
 * berada di tengah-tengah dan justru lebih jelas bila diberi tinta gelap.
 */
fun readableOn(background: Color): Color =
    if (background.luminance() > 0.28f) OnAccent else LightInk

// Aksen tetap sama di tema terang maupun gelap.
val Mint = Color(0xFF22C79A)
val Coral = Color(0xFFFF6B6B)
val Slate = Color(0xFF8698AF)
val Rose = Color(0xFFFF8DA1)
val Sky = Color(0xFF6AA6FF)
val Grape = Color(0xFFB794F6)
val Leafy = Color(0xFF86E3A8)
val Sand = Color(0xFFE4C99B)
val Teal = Color(0xFF4DC9CE)

/** Warna yang bisa dipilih untuk sampul koleksi. Disimpan sebagai key, bukan nilai mentah. */
val AccentPalette: List<Pair<String, Color>> = listOf(
    "mint" to Mint,
    "coral" to Coral,
    "slate" to Slate,
    "rose" to Rose,
    "sky" to Sky,
    "grape" to Grape,
    "leafy" to Leafy,
    "sunny" to Color(0xFFF8C744),
    "sand" to Sand,
    "teal" to Teal,
)

private val accentByKey: Map<String, Color> by lazy { AccentPalette.toMap() }

fun accentColor(key: String?): Color = accentByKey[key] ?: Mint

/** Versi gelap tipis dari warna aksen, dipakai untuk "lidah" folder di bagian atas kartu. */
fun accentTab(key: String?): Color {
    val c = accentColor(key)
    return Color(
        red = (c.red * 0.88f).coerceIn(0f, 1f),
        green = (c.green * 0.88f).coerceIn(0f, 1f),
        blue = (c.blue * 0.88f).coerceIn(0f, 1f),
        alpha = 1f,
    )
}

/**
 * Versi lembut sebuah warna yang tetap pekat.
 *
 * Permukaan di aplikasi ini punya bayangan pejal di belakangnya, jadi latar
 * ber-alpha rendah akan ditembus bayangan itu. Warna lembut dibuat dengan
 * mencampur ke warna dasar, bukan menurunkan alpha.
 */
@Composable
fun softTint(color: Color, base: Color = CardWhite, strength: Float = 0.12f): Color =
    lerp(base, color, strength)
