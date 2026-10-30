package com.nononsenseapps.feeder.ui.compose.feedarticle

import com.nononsenseapps.feeder.model.MediaImage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForkSameImageTest {
    @Test
    fun matchesResizedAndRehostedCopies() {
        assertTrue(sameImage("https://example.com/wp-content/uploads/drone-swarm.jpg", "https://example.com/wp-content/uploads/drone-swarm-1024x576.jpg"))
        assertTrue(sameImage("https://i0.wp.com/example.com/drone-swarm.jpg?w=1200", "https://example.com/drone-swarm.jpg"))
        assertTrue(sameImage("https://cdn.example.com/drone-swarm@2x.webp", "https://example.com/drone-swarm.jpg"))
        assertTrue(sameImage("https://example.com/drone%20swarm.jpg", "https://example.com/drone swarm.jpg"))
    }

    @Test
    fun keepsDifferentPictures() {
        assertFalse(sameImage("https://example.com/drone-swarm.jpg", "https://example.com/mountain-range.jpg"))
        // Too short to tell apart
        assertFalse(sameImage("https://a.com/1.jpg", "https://b.com/1.jpg"))
    }

    @Test
    fun headerImageHiddenWhenContentHasACopy() {
        assertFalse(
            shouldShowHeaderImage(
                showHeaderImage = true,
                image = MediaImage("https://example.com/hero-image.jpg?resize=600"),
                contentImageUrls = setOf("https://example.com/hero-image-1200x800.jpg"),
            ),
        )
    }
}
