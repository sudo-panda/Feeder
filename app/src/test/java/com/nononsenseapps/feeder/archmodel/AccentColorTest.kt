package com.nononsenseapps.feeder.archmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class AccentColorTest {
    @Test
    fun defaultsToGreen() {
        assertEquals(AccentColor.GREEN, accentColorFromString(null))
        assertEquals(AccentColor.GREEN, accentColorFromString("nonsense"))
    }

    @Test
    fun readsStoredValue() {
        assertEquals(AccentColor.BLUE, accentColorFromString("blue"))
        assertEquals(AccentColor.TEAL, accentColorFromString("TEAL"))
    }

    @Test
    fun wallpaperNeedsAndroid12() {
        // JVM tests report SDK_INT 0
        assertEquals(AccentColor.GREEN, accentColorFromString("wallpaper"))
    }
}
