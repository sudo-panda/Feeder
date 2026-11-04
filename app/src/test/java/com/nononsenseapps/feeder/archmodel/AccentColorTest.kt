package com.nononsenseapps.feeder.archmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccentColorTest {
    @Test
    fun defaultsToGreen() {
        assertEquals(AccentColor.Rgb(0x2F8A63), accentColorFromString(null))
        assertEquals(AccentColor.DEFAULT, accentColorFromString("nonsense"))
    }

    @Test
    fun roundTripsHex() {
        val color = AccentColor.Rgb(0x7B5CD6)
        assertEquals("#7B5CD6", color.toPrefString())
        assertEquals(color, accentColorFromString("#7B5CD6"))
        assertEquals(color, accentColorFromString("#7b5cd6"))
    }

    @Test
    fun readsNamesFromFirstBeta() {
        assertEquals(AccentColor.Rgb(0x1976D2), accentColorFromString("blue"))
    }

    @Test
    fun rejectsBadHex() {
        assertNull(accentColorFromHex("#12345"))
        assertNull(accentColorFromHex("#GGGGGG"))
    }

    @Test
    fun wallpaperNeedsAndroid12() {
        // JVM tests report SDK_INT 0
        assertEquals(AccentColor.DEFAULT, accentColorFromString("wallpaper"))
        assertEquals("wallpaper", AccentColor.Wallpaper.toPrefString())
    }
}
