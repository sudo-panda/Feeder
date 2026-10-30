package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.components.FeedAvatar
import com.nononsenseapps.feeder.ui.compose.components.ForkActionButton
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
            // Non-breaking spaces keep "5 min read" on one line
            pluralStringResource(R.plurals.fork_min_read, minutes, minutes).replace(' ', '\u00A0')
        } else {
            null
        }
    val meta = listOfNotNull(authorDate, readTime).joinToString("\u00A0· ")

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
            FeedAvatar(feedTitle = feedTitle, imageUrl = feedImageUrl, size = 36.dp)
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
    onReadAloud: () -> Unit,
    isUnread: Boolean,
    onToggleRead: () -> Unit,
    modifier: Modifier = Modifier,
    // Lines the first icon up with the text: the 22dp icon sits 13dp inside its 48dp button
    startPadding: Dp = 0.dp,
) {
    // Same buttons, size and alignment as the feed cards' action bar
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = startPadding),
        ) {
            ForkActionButton(
                icon = if (saved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                label = stringResource(if (saved) R.string.saved_adjective else R.string.save_article),
                highlighted = saved,
                onClick = onSaveClick,
            )
            ForkActionButton(Icons.Outlined.Share, stringResource(R.string.share), onClick = onShare)
            ForkActionButton(
                Icons.Outlined.OpenInBrowser,
                stringResource(R.string.open_in_web_view),
                onClick = onOpenInBrowser,
            )
            ForkActionButton(Icons.Outlined.RecordVoiceOver, stringResource(R.string.read_article), onClick = onReadAloud)
            // Shows what a tap does, like the icon behind a swiped card
            ForkActionButton(
                icon = if (isUnread) Icons.Outlined.MarkEmailRead else Icons.Outlined.MarkEmailUnread,
                label = stringResource(if (isUnread) R.string.mark_as_read else R.string.mark_as_unread),
                onClick = onToggleRead,
            )
        }
    }
}
