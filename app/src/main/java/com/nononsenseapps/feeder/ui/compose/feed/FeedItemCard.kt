package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedCropScaling
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feedarticle.wordsToReadTimeSecs
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemSnippetTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.theme.titleFontWeight
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import java.time.Instant

@Composable
fun FeedItemCard(
    item: FeedListItem,
    showThumbnail: Boolean,
    onOpenFeedItemInReader: () -> Unit,
    onOpenFeedItemInCustomTab: () -> Unit,
    onOpenFeedItemInBrowser: () -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmark: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    bookmarkIndicator: Boolean,
    maxLines: Int,
    showOnlyTitle: Boolean,
    showReadingTime: Boolean,
    modifier: Modifier = Modifier,
    onShowDropdown: () -> Unit = {},
) {
    // Fork: Boost-style card: bold title, one meta line, snippet, full-bleed image, quick actions
    ElevatedCard(
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .requiredHeightIn(min = minimumTouchSize)
                    .alpha(if (!item.unread) 0.75f else 1.0f),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            ) {
                if (item.title.isNotBlank()) {
                    WithBidiDeterminedLayoutDirection(paragraph = item.title) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (item.unread) FontWeight.Bold else FontWeight.Normal,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                FeedItemCardMetaLine(item = item, showReadingTime = showReadingTime)
                // Feeds without titles always show the snippet
                if ((!showOnlyTitle || item.title.isBlank()) && item.snippet.isNotBlank()) {
                    WithBidiDeterminedLayoutDirection(paragraph = item.snippet) {
                        Text(
                            text = item.snippet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (item.title.isBlank()) maxLines else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            if (showThumbnail) {
                item.image?.let { image ->
                    // Don't render known super small images. Null is OK
                    if ((image.width ?: 1000) >= 10 && (image.height ?: 1000) >= 10) {
                        val pixelDensity = LocalDensity.current.density
                        val contentScale =
                            remember(pixelDensity) {
                                RestrainedCropScaling(pixelDensity)
                            }
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val pixels =
                                with(LocalDensity.current) {
                                    val pxWidth = maxWidth.roundToPx()
                                    Size(pxWidth, (pxWidth * 9) / 16)
                                }
                            AsyncImage(
                                model =
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(image.url)
                                        .listener(
                                            onError = { a, b ->
                                                logDebug("FEEDER_CARD", "error ${a.data}", b.throwable)
                                            },
                                        ).scale(Scale.FIT)
                                        .size(pixels)
                                        .precision(Precision.INEXACT)
                                        .build(),
                                placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                                error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                                contentDescription = stringResource(id = R.string.article_image),
                                contentScale = contentScale,
                                alignment = Alignment.Center,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16.0f / 9.0f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .safeSemantics {
                                            testTag = "card_image"
                                        },
                            )
                        }
                    }
                }
            }
            FeedItemCardActions(
                item = item,
                onToggleBookmark = onToggleBookmark,
                onShareItem = onShareItem,
                onOpenFeedItemInBrowser = onOpenFeedItemInBrowser,
                onShowDropdown = onShowDropdown,
                dropDownMenuExpanded = dropDownMenuExpanded,
                onDismissDropdown = onDismissDropdown,
                onOpenFeedItemInReader = onOpenFeedItemInReader,
                onOpenFeedItemInCustomTab = onOpenFeedItemInCustomTab,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
            )
        }
    }
}

// Fork: "[feed icon] Feed · 2 h · 4 min" under the title
@Composable
private fun FeedItemCardMetaLine(
    item: FeedListItem,
    showReadingTime: Boolean,
) {
    val readTimeSecs = remember(item.wordCount) { wordsToReadTimeSecs(item.wordCount) }
    val readTimeText =
        if (showReadingTime && readTimeSecs > 0) {
            pluralStringResource(id = R.plurals.n_minutes, count = readTimeSecs / 60)
                .format("${readTimeSecs / 60}:${"%02d".format(readTimeSecs % 60)}")
        } else {
            null
        }
    val meta = listOfNotNull(item.feedTitle, item.pubDate, readTimeText).filter { it.isNotBlank() }.joinToString(" · ")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item.feedImageUrl?.let { feedImageUrl ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(feedImageUrl.toString())
                        .scale(Scale.FIT)
                        .precision(Precision.INEXACT)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp)),
            )
        }
        WithBidiDeterminedLayoutDirection(paragraph = meta) {
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Fork: Boost-style quick actions at the bottom of each card
@Composable
private fun FeedItemCardActions(
    item: FeedListItem,
    onToggleBookmark: () -> Unit,
    onShareItem: () -> Unit,
    onOpenFeedItemInBrowser: () -> Unit,
    onShowDropdown: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    onOpenFeedItemInReader: () -> Unit,
    onOpenFeedItemInCustomTab: () -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
    ) {
        CardActionButton(
            icon = if (item.bookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(if (item.bookmarked) R.string.unsave_article else R.string.save_article),
            tint = if (item.bookmarked) MaterialTheme.colorScheme.primary else tint,
            onClick = onToggleBookmark,
        )
        CardActionButton(
            icon = Icons.Outlined.Share,
            contentDescription = stringResource(R.string.share),
            tint = tint,
            onClick = onShareItem,
        )
        CardActionButton(
            icon = Icons.Outlined.OpenInBrowser,
            contentDescription = stringResource(R.string.open_article_in_default_browser),
            tint = tint,
            onClick = onOpenFeedItemInBrowser,
        )
        Spacer(modifier = Modifier.weight(1f))
        // The menu is anchored to this button, also when opened by a long press on the card
        Box {
            CardActionButton(
                icon = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.open_menu),
                tint = tint,
                onClick = onShowDropdown,
            )
            FeedItemDropdownMenu(
                item = item,
                expanded = dropDownMenuExpanded,
                onDismissDropdown = onDismissDropdown,
                onOpenFeedItemInReader = onOpenFeedItemInReader,
                onOpenFeedItemInCustomTab = onOpenFeedItemInCustomTab,
                onOpenFeedItemInBrowser = onOpenFeedItemInBrowser,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onShareItem = onShareItem,
                onToggleBookmark = onToggleBookmark,
            )
        }
    }
}

@Composable
private fun CardActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun RowScope.FeedItemText(
    item: FeedListItem,
    onOpenFeedItemInReader: () -> Unit,
    onOpenFeedItemInCustomTab: () -> Unit,
    onOpenFeedItemInBrowser: () -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmark: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    maxLines: Int,
    showOnlyTitle: Boolean,
    showReadingTime: Boolean,
    modifier: Modifier = Modifier,
) {
    val snippetStyle = FeedListItemSnippetTextStyle()
    val joinedText =
        remember(item, showOnlyTitle) {
            buildAnnotatedString {
                if (item.title.isNotBlank()) {
                    append(item.title)
                    if (!showOnlyTitle && item.snippet.isNotBlank()) {
                        withStyle(snippetStyle.toSpanStyle()) {
                            append('\n')
                            append(item.snippet)
                        }
                    }
                } else {
                    // Heard of one feed which did not have titles. If so always include snippet
                    append(item.snippet)
                }
            }
        }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            modifier
                .weight(1f),
    ) {
        WithBidiDeterminedLayoutDirection(paragraph = joinedText.text) {
            Text(
                text = joinedText,
                style = FeedListItemTitleTextStyle(),
                fontWeight = titleFontWeight(item.unread),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
        }
        // Want the dropdown to center on the middle text row
        Box {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    WithBidiDeterminedLayoutDirection(paragraph = item.feedTitle) {
                        Text(
                            text = item.feedTitle,
                            style = FeedListItemFeedTitleStyle(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .weight(1f),
                        )
                    }
                    WithBidiDeterminedLayoutDirection(paragraph = item.pubDate) {
                        Text(
                            text = item.pubDate,
                            style = FeedListItemDateStyle(),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier,
                        )
                    }
                }
            }
            FeedItemDropdownMenu(
                item = item,
                expanded = dropDownMenuExpanded,
                onDismissDropdown = onDismissDropdown,
                onOpenFeedItemInReader = onOpenFeedItemInReader,
                onOpenFeedItemInCustomTab = onOpenFeedItemInCustomTab,
                onOpenFeedItemInBrowser = onOpenFeedItemInBrowser,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onShareItem = onShareItem,
                onToggleBookmark = onToggleBookmark,
            )
        }
        if (showReadingTime) {
            val readTimeSecs =
                remember(item.wordCount) {
                    wordsToReadTimeSecs(item.wordCount)
                }
            if (readTimeSecs > 0) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val seconds = "%02d".format(readTimeSecs % 60)
                    val readTimeText =
                        pluralStringResource(id = R.plurals.n_minutes, count = readTimeSecs / 60)
                            .format(
                                "${readTimeSecs / 60}:$seconds",
                            )
                    val wordCountText =
                        pluralStringResource(id = R.plurals.n_words, count = item.wordCount)
                            .format(item.wordCount)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                    ) {
                        WithBidiDeterminedLayoutDirection(paragraph = readTimeText) {
                            Text(
                                text = readTimeText,
                                style = FeedListItemFeedTitleStyle(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        WithBidiDeterminedLayoutDirection(paragraph = wordCountText) {
                            Text(
                                text = wordCountText,
                                style = FeedListItemFeedTitleStyle(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fork: the article menu, shared by the long-press menu and the card's more button
@Composable
fun FeedItemDropdownMenu(
    item: FeedListItem,
    expanded: Boolean,
    onDismissDropdown: () -> Unit,
    onOpenFeedItemInReader: () -> Unit,
    onOpenFeedItemInCustomTab: () -> Unit,
    onOpenFeedItemInBrowser: () -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val closeMenuText = stringResource(id = R.string.close_menu)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissDropdown,
        modifier = Modifier.onKeyEventLikeEscape(onDismissDropdown),
    ) {
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onOpenFeedItemInReader()
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(id = R.string.open_article_in_reader),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onOpenFeedItemInCustomTab()
            },
            leadingIcon = { Icon(Icons.Outlined.Tab, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(id = R.string.open_article_in_custom_tab),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onOpenFeedItemInBrowser()
            },
            leadingIcon = { Icon(Icons.Outlined.OpenInBrowser, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(id = R.string.open_article_in_default_browser),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onToggleBookmark()
            },
            leadingIcon = {
                Icon(
                    if (item.bookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text =
                        stringResource(
                            when (item.bookmarked) {
                                true -> R.string.unsave_article
                                false -> R.string.save_article
                            },
                        ),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onMarkAboveAsRead()
            },
            leadingIcon = { Icon(Icons.Outlined.VerticalAlignTop, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(id = R.string.mark_items_above_as_read),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onMarkBelowAsRead()
            },
            leadingIcon = { Icon(Icons.Outlined.VerticalAlignBottom, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(id = R.string.mark_items_below_as_read),
                )
            },
        )
        DropdownMenuItem(
            onClick = {
                onDismissDropdown()
                onShareItem()
            },
            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
            text = {
                Text(
                    text = stringResource(R.string.share),
                )
            },
        )
        // Hidden button for TalkBack
        DropdownMenuItem(
            onClick = onDismissDropdown,
            text = {},
            modifier =
                Modifier
                    .height(0.dp)
                    .safeSemantics {
                        contentDescription = closeMenuText
                        role = Role.Button
                    },
        )
    }
}

@Composable
@PreviewThemes
@Suppress("ktlint:standard:max-line-length")
private fun Preview() {
    PreviewTheme {
        FeedItemCard(
            item =
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf dsaf asd fsa dfasdf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    image = null,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = true,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 588,
                ),
            showThumbnail = true,
            onOpenFeedItemInReader = {},
            onOpenFeedItemInCustomTab = {},
            onOpenFeedItemInBrowser = {},
            onMarkAboveAsRead = {},
            onMarkBelowAsRead = {},
            onShareItem = {},
            onToggleBookmark = {},
            dropDownMenuExpanded = false,
            onDismissDropdown = {},
            bookmarkIndicator = true,
            maxLines = 2,
            showOnlyTitle = false,
            showReadingTime = true,
        )
    }
}

@Composable
@PreviewThemes
@Suppress("ktlint:standard:max-line-length")
private fun PreviewWithImageUnread() {
    PreviewTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCard(
                item =
                    FeedListItem(
                        title = "西日本や東日本",
                        snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                        feedTitle = "Super Feed",
                        pubDate = "Jun 9, 2021",
                        unread = true,
                        image = MediaImage("blabal"),
                        link = null,
                        id = ID_UNSET,
                        bookmarked = false,
                        feedImageUrl = URL("https://foo/bar.png"),
                        primarySortTime = Instant.EPOCH,
                        rawPubDate = null,
                        wordCount = 939,
                    ),
                showThumbnail = true,
                onOpenFeedItemInReader = {},
                onOpenFeedItemInCustomTab = {},
                onOpenFeedItemInBrowser = {},
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onToggleBookmark = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                bookmarkIndicator = true,
                maxLines = 2,
                showOnlyTitle = false,
                showReadingTime = true,
            )
        }
    }
}

@Composable
@PreviewThemes
@Suppress("ktlint:standard:max-line-length")
private fun PreviewWithImageRead() {
    PreviewTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCard(
                item =
                    FeedListItem(
                        title = "title can be one line",
                        snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                        feedTitle = "Super Duper Feed",
                        pubDate = "Jun 9, 2021",
                        unread = false,
                        image = MediaImage("blabal"),
                        link = null,
                        id = ID_UNSET,
                        bookmarked = true,
                        feedImageUrl = null,
                        primarySortTime = Instant.EPOCH,
                        rawPubDate = null,
                        wordCount = 910,
                    ),
                showThumbnail = true,
                onOpenFeedItemInReader = {},
                onOpenFeedItemInCustomTab = {},
                onOpenFeedItemInBrowser = {},
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onToggleBookmark = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                bookmarkIndicator = true,
                maxLines = 2,
                showOnlyTitle = false,
                showReadingTime = true,
            )
        }
    }
}
