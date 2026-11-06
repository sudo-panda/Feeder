package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.FeedItemCursor
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.model.ThumbnailImage
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

@Composable
fun FeedItemCompact(
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
    imageWidth: Dp = 64.dp,
) {
    Surface(
        modifier =
            modifier
                .height(IntrinsicSize.Min)
                .alpha(if (!item.unread) 0.75f else 1.0f),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = LocalDimens.current.margin),
        ) {
            FeedItemText(
                item = item,
                onOpenFeedItemInReader = onOpenFeedItemInReader,
                onOpenFeedItemInCustomTab = onOpenFeedItemInCustomTab,
                onOpenFeedItemInBrowser = onOpenFeedItemInBrowser,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onShareItem = onShareItem,
                onToggleBookmark = onToggleBookmark,
                dropDownMenuExpanded = dropDownMenuExpanded,
                onDismissDropdown = onDismissDropdown,
                maxLines = maxLines,
                showOnlyTitle = showOnlyTitle,
                showReadingTime = showReadingTime,
                // Fork: the feed's round icon next to its name, like the cards
                showFeedAvatar = true,
                modifier =
                    Modifier
                        .requiredHeightIn(min = minimumTouchSize)
                        .padding(vertical = 8.dp),
            )

            // Fork: no thumbnail, it was too small to show anything; only the saved star
            if (item.bookmarked && bookmarkIndicator) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    FeedItemEitherIndicator(
                        bookmarked = true,
                        itemImage = null,
                        feedImageUrl = null,
                        size = 24.dp,
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .width(64.dp),
                    )
                }
            } else {
                // Taking Row spacing into account
                Spacer(modifier = Modifier.width(LocalDimens.current.margin - 4.dp))
            }
        }
    }
}

@Immutable
data class FeedListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val feedTitle: String,
    val unread: Boolean,
    val pubDate: String,
    val image: ThumbnailImage?,
    val link: String?,
    val bookmarked: Boolean,
    val feedImageUrl: URL?,
    val primarySortTime: Instant,
    val rawPubDate: ZonedDateTime?,
    val wordCount: Int,
) {
    val cursor: FeedItemCursor
        get() =
            object : FeedItemCursor {
                override val primarySortTime: Instant = this@FeedListItem.primarySortTime
                override val pubDate: ZonedDateTime? = this@FeedListItem.rawPubDate
                override val id: Long = this@FeedListItem.id
            }

    /**
     * Used so lazylist/grid can re-use items.
     *
     * Type will depend on having images as that will influence visible items
     */
    fun contentType(feedItemStyle: FeedItemStyle): String =
        when {
            image != null -> "$feedItemStyle/image"
            else -> "$feedItemStyle/other"
        }
}

@Composable
@PreviewLightDark
private fun PreviewRead() {
    PreviewTheme {
        FeedItemCompact(
            item =
                @Suppress("ktlint:standard:max-line-length")
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    image = null,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
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
            maxLines = 5,
            showOnlyTitle = false,
            showReadingTime = true,
            imageWidth = 64.dp,
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewUnread() {
    PreviewTheme {
        FeedItemCompact(
            item =
                @Suppress("ktlint:standard:max-line-length")
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    image = null,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = true,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
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
            maxLines = 5,
            showOnlyTitle = false,
            showReadingTime = true,
            imageWidth = 64.dp,
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewWithImage() {
    PreviewTheme {
        Box(
            modifier = Modifier.width(400.dp),
        ) {
            FeedItemCompact(
                item =
                    @Suppress("ktlint:standard:max-line-length")
                    FeedListItem(
                        title = "title",
                        snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                        feedTitle = "Super Duper Feed One two three hup di too dasf",
                        pubDate = "Jun 9, 2021",
                        unread = true,
                        image = MediaImage("blabla"),
                        link = null,
                        id = ID_UNSET,
                        bookmarked = false,
                        feedImageUrl = null,
                        primarySortTime = Instant.EPOCH,
                        rawPubDate = null,
                        wordCount = 900,
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
                maxLines = 5,
                showOnlyTitle = false,
                showReadingTime = true,
                imageWidth = 64.dp,
            )
        }
    }
}
