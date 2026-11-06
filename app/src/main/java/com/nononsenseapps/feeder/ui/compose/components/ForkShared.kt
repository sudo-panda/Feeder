package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.ui.compose.theme.LocalListIconScale
import kotlinx.coroutines.delay

// Fork: pieces shared by the feed cards, the drawer and the reader, so they look the same everywhere

/**
 * Icon buttons in the card and reader action bars: 22dp icons in 48dp touch targets. A long press
 * shows the label. The label popup is only composed while shown, which keeps cards cheap to scroll.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ForkActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
) {
    val tint =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            highlighted -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    val scale = LocalListIconScale.current
    val buttonSize = 48.dp * maxOf(1f, scale)
    var showLabel by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(buttonSize)
                .combinedClickable(
                    enabled = enabled,
                    interactionSource = null,
                    indication = ripple(bounded = false, radius = buttonSize / 2),
                    role = Role.Button,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showLabel = true
                    },
                    onClick = onClick,
                ).semantics { contentDescription = label },
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp * scale))
        if (showLabel) {
            LaunchedEffect(Unit) {
                delay(1_500)
                showLabel = false
            }
            Popup(
                popupPositionProvider = AboveAnchor,
                onDismissRequest = { showLabel = false },
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

/** Centred above the anchor, or below it when there is no room above. */
private object AboveAnchor : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x =
            (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
                .coerceIn(0, maxOf(0, windowSize.width - popupContentSize.width))
        val above = anchorBounds.top - popupContentSize.height
        return IntOffset(x, if (above >= 0) above else anchorBounds.bottom)
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
    // The painter's own state, so an icon already in Coil's memory cache shows at once when a
    // card scrolls back in, without the letter flashing first
    val painter =
        if (imageUrl != null) {
            val pixels = with(LocalDensity.current) { size.roundToPx() }
            rememberAsyncImagePainter(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .scale(Scale.FILL)
                        .size(pixels)
                        .precision(Precision.INEXACT)
                        .build(),
                contentScale = ContentScale.Crop,
            )
        } else {
            null
        }
    val loaded = painter != null && painter.state.collectAsState().value is AsyncImagePainter.State.Success
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
                // Sized from the circle, not the font size setting, and centred without the
                // inherited line height, so the letter always fits
                style =
                    with(LocalDensity.current) {
                        val letter = (size * 0.42f).toSp()
                        TextStyle(
                            fontSize = letter,
                            lineHeight = letter,
                            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
                        )
                    },
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
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

/** Section headers everywhere (Settings groups, Add feed results): one look for one role. */
val ForkSectionHeaderStyle: TextStyle
    @Composable get() =
        MaterialTheme.typography.labelLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )

/** The size for an icon sitting in a line of [style] text, so it grows and shrinks with the text. */
@Composable
fun textIconSize(
    style: TextStyle,
    factor: Float,
): Dp {
    val fontSize = if (style.fontSize.isSpecified) style.fontSize else 14.sp
    return with(LocalDensity.current) { (fontSize * factor).toDp() }
}
