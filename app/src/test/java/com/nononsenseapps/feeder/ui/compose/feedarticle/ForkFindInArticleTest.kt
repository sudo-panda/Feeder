package com.nononsenseapps.feeder.ui.compose.feedarticle

import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearBlockQuote
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearTextBlockStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForkFindInArticleTest {
    private fun text(value: String) = LinearText(emptySet(), value, LinearTextBlockStyle.TEXT)

    private val article =
        LinearArticle(
            listOf(
                text("Reddit is announcing a shift. reddit, REDDIT."),
                LinearBlockQuote(emptySet(), null, listOf(text("Quoted Reddit text"))),
                text("Nothing here"),
            ),
        )

    @Test
    fun findsAllMatchesInOrderIgnoringCase() {
        val matches = findMatches(article, "reddit")
        assertEquals(4, matches.size)
        assertEquals(listOf(0, 0, 0, 1), matches.map { it.elementIndex })
        assertEquals(0..5, matches.first().range)
        assertEquals(7..12, matches.last().range)
    }

    @Test
    fun blankQueryFindsNothing() {
        assertTrue(findMatches(article, " ").isEmpty())
    }

    @Test
    fun marksOnlyTheCurrentMatch() {
        val matches = findMatches(article, "reddit")
        val highlights = findHighlights(matches, current = 1)
        val first = highlights.getValue(article.elements[0] as LinearText)
        assertEquals(listOf(false, true, false), first.map { it.second })
    }
}
