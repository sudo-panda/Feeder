package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.LocalReaderAppearance
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText

// Fork: Boost-style reader: feed and meta line, bold title, edge-to-edge images and an action bar

/**
 * How far reader images and the action bar extend past the reader's side margins, so they reach
 * the screen edges. Zero when the reader doesn't fill the screen width (tablets, dual pane).
 */
val LocalReaderBleed = compositionLocalOf { 0.dp }

fun Modifier.bleed(amount: Dp): Modifier =
    if (amount == 0.dp) {
        this
    } else {
        layout { measurable, constraints ->
            val extra = (amount * 2).roundToPx()
            val placeable =
                measurable.measure(
                    constraints.copy(
                        minWidth = constraints.maxWidth + extra,
                        maxWidth = constraints.maxWidth + extra,
                    ),
                )
            layout(constraints.maxWidth, placeable.height) {
                placeable.place(-amount.roundToPx(), 0)
            }
        }
    }

@Composable
fun ForkReaderHeader(
    feedTitle: String,
    articleTitle: String,
    authorDate: String?,
    readTimeSecs: Int,
    onFeedTitleClick: () -> Unit,
    modifier: Modifier = Modifier,
    feedImageUrl: String? = null,
) {
    val goToFeedLabel = stringResource(R.string.go_to_feed, feedTitle)
    val readTime =
        if (readTimeSecs > 0) {
            // Rounded up, so short articles show "1 min read"
            val minutes = (readTimeSecs + 59) / 60
            pluralStringResource(R.plurals.fork_min_read, minutes, minutes)
        } else {
            null
        }
    val meta = listOfNotNull(authorDate, readTime).joinToString(" · ")

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier =
                Modifier
                    .clickable(onClick = onFeedTitleClick)
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        contentDescription = goToFeedLabel
                    },
        ) {
            FeedAvatar(feedTitle = feedTitle, feedImageUrl = feedImageUrl)
            Column {
                WithBidiDeterminedLayoutDirection(paragraph = feedTitle) {
                    Text(
                        text = feedTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (articleTitle.isNotBlank()) {
            val titleStyle = MaterialTheme.typography.titleLarge
            val scale = LocalReaderAppearance.current.titleScale
            ProvideScaledText(
                style =
                    titleStyle.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = titleStyle.fontSize * scale,
                        lineHeight = titleStyle.lineHeight * scale,
                    ),
            ) {
                WithBidiDeterminedLayoutDirection(paragraph = articleTitle) {
                    Text(text = articleTitle, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun ForkReaderActionBar(
    saved: Boolean,
    onSaveClick: () -> Unit,
    onShare: () -> Unit,
    onOpenInBrowser: () -> Unit,
    isUnread: Boolean,
    onToggleRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
        ) {
            ReaderAction(
                icon = if (saved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                label = stringResource(if (saved) R.string.saved_adjective else R.string.save_article),
                highlighted = saved,
                onClick = onSaveClick,
            )
            ReaderAction(Icons.Outlined.Share, stringResource(R.string.share), onClick = onShare)
            ReaderAction(
                Icons.Outlined.OpenInBrowser,
                stringResource(R.string.open_article_in_default_browser),
                onClick = onOpenInBrowser,
            )
            // Shows what a tap does, like the icon behind a swiped card
            ReaderAction(
                icon = if (isUnread) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                label = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
                onClick = onToggleRead,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ReaderAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
) {
    val color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    // Long press shows the label
    PlainTooltipBox(tooltip = { Text(label) }) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
    }
}

// The feed's icon, as in the feed list and the drawer, or its first letter while loading or without one
@Composable
private fun FeedAvatar(
    feedTitle: String,
    feedImageUrl: String?,
) {
    var loaded by remember(feedImageUrl) { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (loaded) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primary),
    ) {
        if (!loaded) {
            Text(
                text = feedTitle.trim().take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        if (feedImageUrl != null) {
            val pixels = with(LocalDensity.current) { 36.dp.roundToPx() }
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(feedImageUrl)
                        .scale(Scale.FILL)
                        .size(pixels)
                        .precision(Precision.INEXACT)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
