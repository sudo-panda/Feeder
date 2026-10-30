package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Fork: Boost-style swiping between articles. The article follows the finger sideways; past a
 * quarter of the width (or with a fling) [onSwipe] is asked to open the next (swipe left) or
 * previous (swipe right) article. When it returns false, for example at the end of the list, the
 * article springs back.
 */
@Composable
fun ArticleSwipeBox(
    onSwipe: suspend (next: Boolean) -> Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var width by remember { mutableIntStateOf(1) }

    Box(
        modifier =
            modifier
                .onSizeChanged { width = it.width.coerceAtLeast(1) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state =
                        rememberDraggableState { delta ->
                            scope.launch { offset.snapTo(offset.value + delta) }
                        },
                    onDragStopped = { velocity ->
                        val next = offset.value < 0
                        val far = abs(offset.value) > width * 0.25f
                        val flung = abs(velocity) > 1500f && (velocity < 0) == next && abs(offset.value) > width * 0.05f
                        // On success the screen change slides this page the rest of the way out while the
                        // next one slides in beside it, so it stays where the finger left it
                        if (!((far || flung) && currentOnSwipe(next))) {
                            offset.animateTo(0f)
                        }
                    },
                ).graphicsLayer {
                    translationX = offset.value
                },
    ) {
        content()
    }
}

/**
 * Which way the last article swipe went (1 = next, -1 = previous), so the screen change can slide
 * sideways. Reset once the new article is showing, so other navigation keeps its fade.
 */
object ArticleSwipeDirection {
    var current: Int = 0
}
