package com.nononsenseapps.feeder.archmodel

import android.os.Build

/**
 * Fork: Boost-style accent colour on neutral grey surfaces, replaces the dynamic colors switch.
 * Stored as "wallpaper" or "#RRGGBB".
 */
sealed interface AccentColor {
    // Android 12+ dynamic colours
    data object Wallpaper : AccentColor

    data class Rgb(
        // 0xRRGGBB
        val rgb: Int,
    ) : AccentColor {
        fun toHex(): String = "#%06X".format(rgb and 0xFFFFFF)
    }

    companion object {
        val DEFAULT = Rgb(0x2F8A63)

        // Boost-like range, red through brown, as tiles in Settings
        val PRESETS =
            listOf(
                0xD32F2F,
                0xD81B60,
                0x8E24AA,
                0x5E35B1,
                0x3949AB,
                0x1976D2,
                0x0288D1,
                0x0097A7,
                0x00897B,
                0x2F8A63,
                0x689F38,
                0xAFB42B,
                0xFFA000,
                0xEF6C00,
                0xE64A19,
                0x6D4C41,
                0x546E7A,
            ).map { Rgb(it) }
    }
}

fun AccentColor.toPrefString(): String =
    when (this) {
        AccentColor.Wallpaper -> "wallpaper"
        is AccentColor.Rgb -> toHex()
    }

// The first beta stored names from a fixed list
private val oldNames =
    mapOf(
        "blue" to 0x1976D2,
        "indigo" to 0x3949AB,
        "purple" to 0x8E24AA,
        "pink" to 0xD81B60,
        "red" to 0xD32F2F,
        "orange" to 0xEF6C00,
        "green" to 0x2F8A63,
        "teal" to 0x00897B,
    )

fun accentColorFromHex(value: String): AccentColor.Rgb? =
    value
        .trim()
        .removePrefix("#")
        .takeIf { it.length == 6 }
        ?.toIntOrNull(16)
        ?.let { AccentColor.Rgb(it) }

fun accentColorFromString(value: String?): AccentColor {
    val text = value?.trim()?.lowercase() ?: return AccentColor.DEFAULT
    return when {
        text == "wallpaper" ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) AccentColor.Wallpaper else AccentColor.DEFAULT
        text in oldNames -> AccentColor.Rgb(oldNames.getValue(text))
        else -> accentColorFromHex(text) ?: AccentColor.DEFAULT
    }
}
