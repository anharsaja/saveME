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
 * Nada neo-brutalism: tinta hitam pekat tanpa abu-abu kebiruan, kertas putih
 * gading yang terang, dan warna blok yang tajam tanpa gradasi. Warna aksen
 * (mint, coral, dan kawan-kawan) tidak ikut berganti karena sudah cukup pekat
 * untuk keduanya; yang bertukar hanyalah kertas, tinta, dan kartu.
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
    // Kertas gading hangat, bukan abu-abu: kuning logo jadi lebih menyala di atasnya.
    paper = Color(0xFFFFFBEF),
    paperDim = Color(0xFFF0E7D2),
    card = Color(0xFFFFFFFF),
    // Hitam murni. Seluruh garis tepi dan bayangan pejal memakai warna ini.
    ink = Color(0xFF000000),
    inkSoft = Color(0xFF3A3A3A),
    inkFaint = Color(0xFF6B6B6B),
    sunny = Color(0xFFFFD93D),
    sunnyDeep = Color(0xFFF0C000),
    sunnySoft = Color(0xFFFFF0B8),
    danger = Color(0xFFFF4757),
    isDark = false,
)

val DarkPalette = SaveMePalette(
    paper = Color(0xFF121212),
    paperDim = Color(0xFF262626),
    card = Color(0xFF1C1C1C),
    // Di tema gelap giliran putih yang jadi garis tepi dan bayangan.
    ink = Color(0xFFFFFFFF),
    inkSoft = Color(0xFFCBCBCB),
    inkFaint = Color(0xFF8F8F8F),
    sunny = Color(0xFFFFD93D),
    sunnyDeep = Color(0xFFF0C000),
    sunnySoft = Color(0xFF3B3113),
    danger = Color(0xFFFF6B6B),
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
val OnAccent = Color(0xFF000000)

private val LightInk = Color(0xFFFFFFFF)

/**
 * Warna teks atau ikon yang terbaca di atas [background].
 *
 * Ambangnya lebih rendah dari setengah karena aksen kita — mint, coral, slate —
 * berada di tengah-tengah dan justru lebih jelas bila diberi tinta gelap.
 */
fun readableOn(background: Color): Color =
    if (background.luminance() > 0.28f) OnAccent else LightInk

// Aksen tetap sama di tema terang maupun gelap. Semuanya warna blok yang pekat:
// tidak ada pastel yang pudar, sesuai nada neo-brutalism.
val Mint = Color(0xFF00D9A3)
val Coral = Color(0xFFFF5C5C)
val Slate = Color(0xFF7E93B0)
val Rose = Color(0xFFFF7EB6)
/** Biru langit yang sama dengan mata rantai di logo. */
val Sky = Color(0xFF7FD4FF)
val Grape = Color(0xFF9B6DFF)
val Leafy = Color(0xFF7BE495)
val Sand = Color(0xFFE8C07D)
val Teal = Color(0xFF2FD4D4)

/** Warna yang bisa dipilih untuk sampul koleksi. Disimpan sebagai key, bukan nilai mentah. */
val AccentPalette: List<Pair<String, Color>> = listOf(
    "mint" to Mint,
    "coral" to Coral,
    "slate" to Slate,
    "rose" to Rose,
    "sky" to Sky,
    "grape" to Grape,
    "leafy" to Leafy,
    "sunny" to Color(0xFFFFD93D),
    "sand" to Sand,
    "teal" to Teal,
)

private val accentByKey: Map<String, Color> by lazy { AccentPalette.toMap() }

fun accentColor(key: String?): Color = accentByKey[key] ?: Mint

/** Versi gelap tipis dari warna aksen, dipakai untuk "lidah" folder di bagian atas kartu. */
fun accentTab(key: String?): Color {
    val c = accentColor(key)
    return Color(
        red = (c.red * 0.85f).coerceIn(0f, 1f),
        green = (c.green * 0.85f).coerceIn(0f, 1f),
        blue = (c.blue * 0.85f).coerceIn(0f, 1f),
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
fun softTint(color: Color, base: Color = CardWhite, strength: Float = 0.16f): Color =
    lerp(base, color, strength)
