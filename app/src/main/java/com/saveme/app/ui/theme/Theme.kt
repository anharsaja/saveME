package com.saveme.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// Sudut nyaris siku di semua ukuran — ciri paling kentara gaya neo-brutalism.
private val SaveMeShapes = Shapes(
    extraSmall = RoundedCornerShape(3.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(5.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun SaveMeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.sunny,
            onPrimary = OnAccent,
            secondary = Mint,
            onSecondary = OnAccent,
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.sunnySoft,
            onSurfaceVariant = palette.ink,
            error = palette.danger,
            onError = OnAccent,
            outline = palette.ink,
        )
    } else {
        lightColorScheme(
            primary = palette.sunny,
            onPrimary = OnAccent,
            secondary = Mint,
            onSecondary = OnAccent,
            background = palette.paper,
            onBackground = palette.ink,
            surface = palette.card,
            onSurface = palette.ink,
            surfaceVariant = palette.sunnySoft,
            onSurfaceVariant = palette.ink,
            error = palette.danger,
            onError = palette.card,
            outline = palette.ink,
        )
    }

    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SaveMeTypography,
            shapes = SaveMeShapes,
            content = content,
        )
    }
}
