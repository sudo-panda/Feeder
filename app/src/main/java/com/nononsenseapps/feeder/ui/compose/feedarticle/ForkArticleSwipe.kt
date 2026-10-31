package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
 * Fork: Boost-style swiping between articles. While dragging, the previous or next article's
 * [previous]/[next] preview sits right beside the page, like the next page of a book. Released
 * past half the width (or flung), the preview slides fully in and [onSwipe] opens that article,
 * which replaces the preview without another animation. Without a neighbour the page springs back.
 */
@Composable
fun ArticleSwipeBox(
    onSwipe: (next: Boolean) -> Unit,
    previous: (@Composable () -> Unit)?,
    next: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val hasPrevious by rememberUpdatedState(previous != null)
    val hasNext by rememberUpdatedState(next != null)
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
                            scope.launch {
                                val target = offset.value + delta
                                // Only a little give towards a side without an article
                                val allowed = (target < 0 && hasNext) || (target > 0 && hasPrevious)
                                offset.snapTo(if (allowed) target else target.coerceIn(-width * 0.08f, width * 0.08f))
                            }
                        },
                    onDragStopped = { velocity ->
                        val goNext = offset.value < 0
                        val available = if (goNext) hasNext else hasPrevious
                        val far = abs(offset.value) > width * 0.5f
                        val flung = abs(velocity) > 1500f && (velocity < 0) == goNext && abs(offset.value) > width * 0.1f
                        if (available && (far || flung)) {
                            offset.animateTo(if (goNext) -width.toFloat() else width.toFloat())
                            currentOnSwipe(goNext)
                        } else {
                            offset.animateTo(0f)
                        }
                    },
                ),
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset.value }) { content() }
        if (offset.value < 0 && next != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset.value + width }) { next() }
        }
        if (offset.value > 0 && previous != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset.value - width }) { previous() }
        }
    }
}

/**
 * Which way the last article swipe went (1 = next, -1 = previous). The screen change then skips its
 * animation, since the preview has already slid in. Reset once the new article shows.
 */
object ArticleSwipeDirection {
    var current: Int = 0
}
