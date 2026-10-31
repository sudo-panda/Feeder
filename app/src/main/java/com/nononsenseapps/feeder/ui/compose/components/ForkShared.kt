package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox

// Fork: pieces shared by the feed cards, the drawer and the reader, so they look the same everywhere

/** Icon buttons in the card and reader action bars: 22dp icons in 48dp touch targets. */
@Composable
fun ForkActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    // Long press shows the label
    PlainTooltipBox(tooltip = { Text(label) }, modifier = modifier) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

/**
 * A feed's icon in a circle, or its first letter on a colour picked from its name while the icon
 * loads or when it has none, so feeds without icons still look different from each other.
 */
@Composable
fun FeedAvatar(
    feedTitle: String,
    imageUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    var loaded by remember(imageUrl) { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(if (loaded) MaterialTheme.colorScheme.surfaceContainerHighest else avatarColor(feedTitle)),
    ) {
        if (!loaded) {
            Text(
                text = feedTitle.trim().take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize * (size / 36.dp),
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        if (imageUrl != null) {
            val pixels = with(LocalDensity.current) { size.roundToPx() }
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .scale(Scale.FILL)
                        .size(pixels)
                        .precision(Precision.INEXACT)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                modifier = Modifier.size(size),
            )
        }
    }
}

// Mid-tone colours that all keep white letters readable
private val avatarColors =
    listOf(
        Color(0xFF3F51B5),
        Color(0xFF00796B),
        Color(0xFFC2185B),
        Color(0xFF7B1FA2),
        Color(0xFF1565C0),
        Color(0xFF5D4037),
        Color(0xFFD84315),
        Color(0xFF2E7D32),
    )

private fun avatarColor(feedTitle: String): Color = avatarColors[Math.floorMod(feedTitle.trim().lowercase().hashCode(), avatarColors.size)]
