package com.nononsenseapps.feeder.archmodel

import android.os.Build
import androidx.annotation.StringRes
import com.nononsenseapps.feeder.R

// Fork: Boost-style accent colour on neutral grey surfaces, replaces the dynamic colors switch
enum class AccentColor(
    @StringRes val stringId: Int,
) {
    WALLPAPER(R.string.accent_wallpaper),
    BLUE(R.string.accent_blue),
    INDIGO(R.string.accent_indigo),
    PURPLE(R.string.accent_purple),
    PINK(R.string.accent_pink),
    RED(R.string.accent_red),
    ORANGE(R.string.accent_orange),
    GREEN(R.string.accent_green),
    TEAL(R.string.accent_teal),
}

fun accentColorFromString(value: String?): AccentColor =
    AccentColor.entries
        .firstOrNull { it.name.equals(value, ignoreCase = true) }
        ?.takeIf { it != AccentColor.WALLPAPER || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
        ?: AccentColor.GREEN
