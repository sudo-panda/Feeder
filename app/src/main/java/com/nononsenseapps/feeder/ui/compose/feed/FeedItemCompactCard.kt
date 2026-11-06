package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import coil3.size.pxOrElse
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.EnclosureImage
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.PreviewThemes
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import java.time.Instant

sealed interface FeedItemEvent {
    data object OpenInReader : FeedItemEvent

    data object OpenInCustomTab : FeedItemEvent

    data object OpenInBrowser : FeedItemEvent

    data object MarkAboveAsRead : FeedItemEvent

    data object MarkBelowAsRead : FeedItemEvent

    data object ShareItem : FeedItemEvent

    data object ToggleBookmarked : FeedItemEvent

    data object DismissDropdown : FeedItemEvent
}

@Immutable
data class FeedItemState(
    val item: FeedListItem,
    val showThumbnail: Boolean = true,
    val bookmarkIndicator: Boolean = true,
    val dropDownMenuExpanded: Boolean = false,
    val showReadingTime: Boolean = false,
    val maxLines: Int = 2,
)

private val iconSize = 24.dp

// Fork: the top of the image stays clear; from a third of the way down it darkens, strongly behind
// the text at the bottom, so white text is readable on any image
private val gradientStops =
    arrayOf(
        0f to Color.Transparent,
        0.3f to Color.Black.copy(alpha = 0.1f),
        0.6f to Color.Black.copy(alpha = 0.55f),
        1f to Color.Black.copy(alpha = 0.9f),
    )

// Fork: read cards are darker all over, and darkest behind the text
private val readGradientStops =
    arrayOf(
        0f to Color.Black.copy(alpha = 0.25f),
        0.3f to Color.Black.copy(alpha = 0.4f),
        0.6f to Color.Black.copy(alpha = 0.75f),
        1f to Color.Black.copy(alpha = 0.95f),
    )

@Composable
fun FeedItemCompactCard(
    state: FeedItemState,
    modifier: Modifier = Modifier,
    onEvent: (FeedItemEvent) -> Unit = { },
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .requiredHeightIn(min = minimumTouchSize)
                    // Fork: with an image, read cards show it greyed out instead of fading the
                    // white text until it can't be read
                    .alpha(if (!state.item.unread && !state.showThumbnail) 0.75f else 1.0f)
                    .fillMaxWidth(),
        ) {
            if (state.showThumbnail) {
                val sizePx =
                    with(LocalDensity.current) {
                        val width = maxWidth.roundToPx()
                        Size(width, (width * 9) / 16)
                    }
                val gradient =
                    Brush.verticalGradient(
                        colorStops = if (state.item.unread) gradientStops else readGradientStops,
                        startY = 0f,
                        endY = sizePx.height.pxOrElse { 0 }.toFloat(),
                    )
                val imageUrl = state.item.image?.url
                if (imageUrl != null) {
                    FeedItemThumbnail(
                        imageUrl = imageUrl,
                        sizePx = sizePx,
                        read = !state.item.unread,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .background(gradient),
                )
            }

            Row(
                modifier =
                    Modifier
                        // Fork: the same 8dp from the top and the side
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
            ) {
                if (state.item.bookmarked && state.bookmarkIndicator) {
                    FeedItemSavedIndicator(size = iconSize, modifier = Modifier)
                }
                // Fork: a white ticked circle in the corner marks read articles (grey when there is
                // no image to show it on); the feed icon is next to its name
                if (!state.item.unread) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(id = R.string.read_adjective),
                        tint = if (state.showThumbnail) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .width(maxWidth)
                        .padding(top = 48.dp)
                        .align(Alignment.BottomCenter),
            ) {
                FeedItemTitle(
                    state = state,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun FeedItemTitle(
    state: FeedItemState,
    onEvent: (FeedItemEvent) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .padding(vertical = 8.dp, horizontal = 8.dp),
    ) {
        val textColor =
            when {
                !state.showThumbnail -> LocalContentColor.current
                else -> Color.White
            }
        CompositionLocalProvider(LocalContentColor provides textColor) {
            FeedItemText(
                item = state.item,
                onOpenFeedItemInReader = { onEvent(FeedItemEvent.OpenInReader) },
                onOpenFeedItemInCustomTab = { onEvent(FeedItemEvent.OpenInCustomTab) },
                onOpenFeedItemInBrowser = { onEvent(FeedItemEvent.OpenInBrowser) },
                onMarkAboveAsRead = { onEvent(FeedItemEvent.MarkAboveAsRead) },
                onMarkBelowAsRead = { onEvent(FeedItemEvent.MarkBelowAsRead) },
                onShareItem = { onEvent(FeedItemEvent.ShareItem) },
                onToggleBookmark = { onEvent(FeedItemEvent.ToggleBookmarked) },
                dropDownMenuExpanded = state.dropDownMenuExpanded,
                onDismissDropdown = { onEvent(FeedItemEvent.DismissDropdown) },
                maxLines = state.maxLines,
                showOnlyTitle = true,
                showReadingTime = state.showReadingTime,
                showFeedAvatar = true,
            )
        }
    }
}

@Composable
private fun FeedItemThumbnail(
    imageUrl: String?,
    sizePx: Size,
    read: Boolean = false,
) {
    if (imageUrl != null) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageUrl)
                    .listener(
                        onError = { a, b ->
                            logDebug("FEEDER_CARD", "error ${a.data}", b.throwable)
                        },
                    ).scale(Scale.FILL)
                    .size(sizePx)
                    .precision(Precision.INEXACT)
                    .build(),
            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
            contentDescription = stringResource(id = R.string.article_image),
            // Fork: read articles get a grey, slightly darker image
            colorFilter = if (read) ReadImageFilter else null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxWidth()
                    .aspectRatio(16.0f / 9.0f),
        )
    }
}

@Composable
@PreviewThemes
private fun Preview() {
    PreviewTheme {
        FeedItemCompactCard(
            state =
                FeedItemState(
                    item =
                        FeedListItem(
                            title = "title",
                            snippet =
                                "snippet which is quite long as you might expect from a snipper of a story." +
                                    " It keeps going and going and going and going and going and going and going" +
                                    " and going and going and going and going and going and going and going" +
                                    " and going and going and going and going and going and going and going" +
                                    " and going and going and going and going and going and going and going" +
                                    " and going and going and going and going and going and going and snowing",
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
                            wordCount = 0,
                        ),
                ),
        )
    }
}

@Composable
@PreviewThemes
private fun PreviewWithImageUnread() {
    PreviewTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCompactCard(
                state =
                    FeedItemState(
                        item =
                            FeedListItem(
                                title = "title can be one line",
                                snippet =
                                    "snippet which is quite long as you might expect from a" +
                                        " snipper of a story. It keeps going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and snowing",
                                feedTitle = "Super Feed",
                                pubDate = "Jun 9, 2021",
                                unread = true,
                                image = EnclosureImage(url = "blabal", length = 0),
                                link = null,
                                id = ID_UNSET,
                                bookmarked = false,
                                feedImageUrl = URL("https://foo/bar.png"),
                                primarySortTime = Instant.EPOCH,
                                rawPubDate = null,
                                wordCount = 0,
                            ),
                    ),
            )
        }
    }
}

@Composable
@PreviewThemes
private fun PreviewWithImageRead() {
    PreviewTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCompactCard(
                state =
                    FeedItemState(
                        item =
                            FeedListItem(
                                title = "title can be one line",
                                snippet =
                                    "snippet which is quite long as you might expect from a" +
                                        " snipper of a story. It keeps going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and going and going" +
                                        " and going and going and going and going and going and" +
                                        " going and going and going and going and snowing",
                                feedTitle = "Super Duper Feed",
                                pubDate = "Jun 9, 2021",
                                unread = false,
                                image = EnclosureImage(url = "blabal", length = 0),
                                link = null,
                                id = ID_UNSET,
                                bookmarked = true,
                                feedImageUrl = null,
                                primarySortTime = Instant.EPOCH,
                                rawPubDate = null,
                                wordCount = 0,
                            ),
                    ),
            )
        }
    }
}

private val ReadImageFilter =
    ColorFilter.colorMatrix(
        ColorMatrix().apply {
            setToSaturation(0f)
            // 75% brightness
            this[0, 0] *= 0.75f
            this[0, 1] *= 0.75f
            this[0, 2] *= 0.75f
            this[1, 0] *= 0.75f
            this[1, 1] *= 0.75f
            this[1, 2] *= 0.75f
            this[2, 0] *= 0.75f
            this[2, 1] *= 0.75f
            this[2, 2] *= 0.75f
        },
    )
