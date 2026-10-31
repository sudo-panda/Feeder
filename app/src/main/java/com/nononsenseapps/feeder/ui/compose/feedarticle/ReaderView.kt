package com.nononsenseapps.feeder.ui.compose.feedarticle

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.archmodel.isImage
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.model.ThumbnailImage
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedCropScaling
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.text.WithTooltipIfNotBlank
import com.nononsenseapps.feeder.ui.compose.text.rememberMaxImageWidth
import com.nononsenseapps.feeder.ui.compose.theme.LinkTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.LocalReaderAppearance
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import kotlin.math.roundToInt

@Composable
fun ReaderView(
    screenType: ScreenType,
    wordCount: Int,
    onEnclosureClick: () -> Unit,
    onFeedTitleClick: () -> Unit,
    enclosure: Enclosure,
    articleTitle: String,
    feedTitle: String,
    saved: Boolean,
    onSaveClick: () -> Unit,
    isUnread: Boolean,
    onMarkAsUnread: () -> Unit,
    authorDate: String?,
    image: ThumbnailImage?,
    showHeaderImage: Boolean,
    contentImageUrls: Set<String>,
    modifier: Modifier = Modifier,
    articleScrollState: ScrollState = rememberScrollState(),
    onShare: () -> Unit = {},
    onOpenInBrowser: () -> Unit = {},
    onReadAloud: () -> Unit = {},
    isFullText: Boolean = false,
    onToggleFullText: () -> Unit = {},
    feedImageUrl: String? = null,
    articleBody: @Composable (indexOffset: Int) -> Unit,
) {
    val dimens = LocalDimens.current
    // Fork: images and the action bar reach the screen edges when the reader fills the screen
    val bleed =
        if (
            screenType == ScreenType.SINGLE &&
            LocalReaderAppearance.current.edgeToEdgeImages &&
            LocalConfiguration.current.screenWidthDp.dp <= dimens.maxReaderWidth + dimens.margin * 2
        ) {
            dimens.margin
        } else {
            0.dp
        }

    val readTimeSecs =
        remember(wordCount) {
            wordsToReadTimeSecs(wordCount)
        }

    SelectionContainer {
        ColumnScrollbar(
            state = articleScrollState,
            // Fork: a slim, see-through thumb, since images now reach the screen edge under it
            settings =
                ScrollbarSettings.Default.copy(
                    thumbThickness = 3.dp,
                    scrollbarPadding = 2.dp,
                    thumbUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    thumbSelectedColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier =
                    modifier
                        .fillMaxWidth()
                        .verticalScroll(articleScrollState)
                        .padding(
                            start =
                                when (screenType) {
                                    ScreenType.DUAL -> 0.dp // List items have enough padding
                                    ScreenType.SINGLE -> dimens.margin
                                },
                            end = dimens.margin,
                        ).focusGroup()
                        .safeSemantics {
                            testTag = "readerColumn"
                        },
            ) {
                var offsetCounter = 1
                // Fork: Boost-style header replaces the title, feed, author, chips and read time
                ForkReaderHeader(
                    feedTitle = feedTitle,
                    articleTitle = articleTitle,
                    authorDate = authorDate,
                    readTimeSecs = readTimeSecs,
                    onFeedTitleClick = onFeedTitleClick,
                    feedImageUrl = feedImageUrl,
                    modifier =
                        Modifier
                            .padding(top = 8.dp)
                            .width(dimens.maxReaderWidth),
                )

                // Fork: the buttons always sit right under the title, whether the picture comes from
                // the feed or from the article itself
                offsetCounter++
                ForkReaderActionBar(
                    saved = saved,
                    onSaveClick = onSaveClick,
                    onShare = onShare,
                    onOpenInBrowser = onOpenInBrowser,
                    onReadAloud = onReadAloud,
                    isFullText = isFullText,
                    onToggleFullText = onToggleFullText,
                    isUnread = isUnread,
                    onToggleRead = onMarkAsUnread,
                    modifier =
                        Modifier
                            .width(dimens.maxReaderWidth)
                            .bleed(bleed),
                )

                if (enclosure.present) {
                    offsetCounter++
                    run {
                        // Image will be shown in block below
                        if (!enclosure.isImage) {
                            val openLabel =
                                if (enclosure.name.isBlank()) {
                                    stringResource(R.string.open_enclosed_media)
                                } else {
                                    stringResource(R.string.open_enclosed_media_file, enclosure.name)
                                }
                            ProvideScaledText(
                                style =
                                    MaterialTheme.typography.bodyLarge.merge(
                                        LinkTextStyle(),
                                    ),
                            ) {
                                Text(
                                    text = openLabel,
                                    modifier =
                                        Modifier
                                            .width(dimens.maxReaderWidth)
                                            .clickable {
                                                onEnclosureClick()
                                            }.clearAndSetSemantics {
                                                try {
                                                    customActions =
                                                        listOf(
                                                            CustomAccessibilityAction(openLabel) {
                                                                onEnclosureClick()
                                                                true
                                                            },
                                                        )
                                                } catch (e: Exception) {
                                                    // Observed nullpointer exception when setting customActions
                                                    // No clue why it could be null
                                                    Log.e(
                                                        LOG_TAG,
                                                        "Exception in semantics",
                                                        e,
                                                    )
                                                }
                                            },
                                )
                            }
                        }
                    }
                }

                if (image != null && shouldShowHeaderImage(showHeaderImage, image, contentImageUrls)) {
                    val imageWidth: Int =
                        if (image.width?.let { it < 640 } == true) {
                            // Zero or too small, do not show
                            -1
                        } else if (image.width != null) {
                            image.width ?: Int.MAX_VALUE
                        } else {
                            // Enclosures do not have a known width. This will be constrained below.
                            Int.MAX_VALUE
                        }

                    if (imageWidth > 0) {
                        offsetCounter++
                        run {
                            BoxWithConstraints(
                                contentAlignment = Alignment.Center,
                                modifier =
                                    Modifier
                                        .width(dimens.maxReaderWidth)
                                        .bleed(bleed)
                                        .clip(RectangleShape),
                            ) {
                                WithTooltipIfNotBlank(tooltip = stringResource(id = R.string.article_image)) {
                                    val maxImageWidth by rememberMaxImageWidth()
                                    val pixelDensity = LocalDensity.current.density
                                    val contentScale =
                                        remember(pixelDensity) {
                                            RestrainedCropScaling(pixelDensity)
                                        }
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalContext.current)
                                                .data(image.url)
                                                .scale(Scale.FIT)
                                                .size(imageWidth.coerceAtMost(maxImageWidth))
                                                .precision(Precision.INEXACT)
                                                .build(),
                                        contentDescription = enclosure.name,
                                        placeholder =
                                            rememberTintedVectorPainter(
                                                Icons.Outlined.Terrain,
                                            ),
                                        error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                                        contentScale = contentScale,
                                        alignment = Alignment.Center,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .run {
                                                    dimens.imageAspectRatioInReader?.let { ratio ->
                                                        aspectRatio(ratio)
                                                    } ?: this
                                                },
                                    )
                                }
                            }
                        }
                    }
                }

                CompositionLocalProvider(LocalReaderBleed provides bleed) {
                    articleBody(offsetCounter)
                }

                // Bottom spacing
                Spacer(modifier = Modifier.height(92.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ReaderPreview() {
    PreviewTheme {
        Surface {
            ReaderView(
                screenType = ScreenType.SINGLE,
                wordCount = 9700,
                onEnclosureClick = {},
                onFeedTitleClick = {},
                enclosure = Enclosure(),
                articleTitle = "Article title on top",
                feedTitle = "Feed Title is here",
                saved = true,
                onSaveClick = {},
                isUnread = false,
                onMarkAsUnread = {},
                authorDate = "2018-01-02",
                image = MediaImage("https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png"),
                showHeaderImage = true,
                contentImageUrls = emptySet(),
            ) {}
        }
    }
}

internal fun shouldShowHeaderImage(
    showHeaderImage: Boolean,
    image: ThumbnailImage?,
    contentImageUrls: Set<String>,
): Boolean =
    showHeaderImage &&
        image?.fromBody == false &&
        image.url !in contentImageUrls &&
        // Fork: also hide it when the article shows a resized or re-hosted copy of the same picture
        contentImageUrls.none { sameImage(it, image.url) }

/**
 * Fork: whether two image URLs most likely point to the same picture, ignoring the host, query,
 * extension and size suffixes such as "-1024x576" or "@2x".
 */
internal fun sameImage(
    a: String,
    b: String,
): Boolean {
    val keyA = imageKey(a)
    return keyA.length >= 6 && keyA == imageKey(b)
}

private fun imageKey(url: String): String =
    embeddedImageUrl(url)?.let { imageKey(it) } ?: url
        .substringBefore('#')
        .substringBefore('?')
        .trimEnd('/')
        .substringAfterLast('/')
        .let { runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) }
        .substringBeforeLast('.')
        .lowercase()
        .replace(Regex("""(-\d+x\d+|@\d+(\.\d+)?x|-scaled)$"""), "")

// Image CDNs such as NPR's put the original picture's address in a query parameter, e.g. "?url=https%3A%2F%2F..."
private fun embeddedImageUrl(url: String): String? =
    url
        .substringAfter('?', "")
        .substringBefore('#')
        .split('&')
        .map { runCatching { java.net.URLDecoder.decode(it.substringAfter('=', ""), "UTF-8") }.getOrDefault("") }
        .firstOrNull { it.startsWith("http://") || it.startsWith("https://") }

fun wordsToReadTimeSecs(words: Int): Int = (words * 60 / 220.0).roundToInt()

private const val LOG_TAG = "FEEDER_READER"
