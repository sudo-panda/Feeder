package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.ColorUtils
import com.nononsenseapps.feeder.archmodel.AccentColor

// Fork: Boost-style colours. Neutral grey surfaces with one accent colour.

// Light theme: dark enough to read on white
fun AccentColor.Rgb.light(): Color = withLightness { minOf(it, 0.5f) }

// Dark themes: a lighter shade so the accent stays readable on dark surfaces
fun AccentColor.Rgb.dark(): Color = withLightness { maxOf(it, 0.68f) }

private fun AccentColor.Rgb.withLightness(lightness: (Float) -> Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(rgb or 0xFF000000.toInt(), hsl)
    hsl[2] = lightness(hsl[2])
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun onAccent(accent: Color): Color = if (accent.luminance() > 0.4f) Color(0xFF111111) else Color.White

fun forkColorScheme(
    accentColor: AccentColor.Rgb,
    dark: Boolean,
    black: Boolean,
): ColorScheme {
    if (!dark) {
        val accent = accentColor.light()
        val onAccent = onAccent(accent)
        val surface = Color.White
        val text = Color(0xFF1F1F1F)
        val tint = accent.copy(alpha = 0.14f).compositeOver(surface)
        return lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent,
            onPrimaryContainer = onAccent,
            inversePrimary = accentColor.dark(),
            secondary = accent,
            onSecondary = onAccent,
            secondaryContainer = tint,
            onSecondaryContainer = text,
            tertiary = accent,
            onTertiary = onAccent,
            tertiaryContainer = tint,
            onTertiaryContainer = text,
            background = Color(0xFFEDEDED),
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = Color(0xFFE6E6E6),
            onSurfaceVariant = Color(0xFF6B6B6B),
            surfaceTint = Color.Transparent,
            inverseSurface = Color(0xFF2B2B2B),
            inverseOnSurface = Color(0xFFF2F2F2),
            outline = Color(0xFF9E9E9E),
            outlineVariant = Color(0xFFDADADA),
            surfaceBright = surface,
            surfaceDim = Color(0xFFDDDDDD),
            surfaceContainerLowest = surface,
            surfaceContainerLow = surface,
            surfaceContainer = Color(0xFFF5F5F5),
            surfaceContainerHigh = Color(0xFFEFEFEF),
            surfaceContainerHighest = Color(0xFFE6E6E6),
        )
    }

    val accent = accentColor.dark()
    val onAccent = onAccent(accent)
    val background = if (black) Color.Black else Color(0xFF121212)
    val surface = if (black) Color.Black else Color(0xFF1E1E1E)
    val card = if (black) Color(0xFF121212) else Color(0xFF1E1E1E)
    val text = Color(0xFFE8E8E8)
    val tint = accent.copy(alpha = 0.22f).compositeOver(card)
    return darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent,
        onPrimaryContainer = onAccent,
        inversePrimary = accentColor.light(),
        secondary = accent,
        onSecondary = onAccent,
        secondaryContainer = tint,
        onSecondaryContainer = text,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = tint,
        onTertiaryContainer = text,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = if (black) Color(0xFF1A1A1A) else Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFA6A6A6),
        surfaceTint = Color.Transparent,
        inverseSurface = text,
        inverseOnSurface = Color(0xFF1E1E1E),
        outline = Color(0xFF7A7A7A),
        outlineVariant = if (black) Color(0xFF262626) else Color(0xFF3A3A3A),
        surfaceBright = Color(0xFF2E2E2E),
        surfaceDim = background,
        surfaceContainerLowest = background,
        surfaceContainerLow = card,
        surfaceContainer = if (black) Color(0xFF161616) else Color(0xFF232323),
        surfaceContainerHigh = if (black) Color(0xFF1C1C1C) else Color(0xFF2A2A2A),
        surfaceContainerHighest = if (black) Color(0xFF242424) else Color(0xFF333333),
    )
}
