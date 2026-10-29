package com.nononsenseapps.feeder.model

import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Fork: tests for the favicon.ico fallback
class FaviconUrlTest {
    @Test
    fun usesSiteRoot() {
        assertEquals(URL("https://example.com/favicon.ico"), faviconUrl("https://example.com/blog/feed.xml?x=1"))
    }

    @Test
    fun keepsPort() {
        assertEquals(URL("http://example.com:8080/favicon.ico"), faviconUrl("http://example.com:8080/rss"))
    }

    @Test
    fun nullAndInvalidGiveNull() {
        assertNull(faviconUrl(null))
        assertNull(faviconUrl("not a url"))
    }
}
