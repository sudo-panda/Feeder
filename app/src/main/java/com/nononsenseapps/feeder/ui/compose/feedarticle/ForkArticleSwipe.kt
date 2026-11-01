package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

/**
 * Fork: Boost-style swiping between articles. While dragging, the previous or next article's
 * [previous]/[next] preview sits right beside the page, like the next page of a book. Released past
 * half the width (or flung), the preview settles fully in and [onSwipe] opens that article, which
 * replaces the preview without another animation. Otherwise the page settles back.
 */
private enum class SwipePage { PREVIOUS, CURRENT, NEXT }

@Composable
fun ArticleSwipeBox(
    onSwipe: (next: Boolean) -> Unit,
    previous: (@Composable () -> Unit)?,
    next: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    // Anchored dragging always settles on one of the pages (a hand-rolled drag could be left
    // between two articles), the same mechanism as swiping a card in the feed list
    val state = remember { AnchoredDraggableState(SwipePage.CURRENT) }
    var width by remember { mutableIntStateOf(0) }
    val hasPrevious = previous != null
    val hasNext = next != null

    LaunchedEffect(width, hasPrevious, hasNext) {
        if (width > 0) {
            state.updateAnchors(
                DraggableAnchors {
                    SwipePage.CURRENT at 0f
                    if (hasNext) SwipePage.NEXT at -width.toFloat()
                    if (hasPrevious) SwipePage.PREVIOUS at width.toFloat()
                },
            )
        }
    }
    LaunchedEffect(state.settledValue) {
        when (state.settledValue) {
            SwipePage.NEXT -> currentOnSwipe(true)
            SwipePage.PREVIOUS -> currentOnSwipe(false)
            SwipePage.CURRENT -> Unit
        }
    }

    val offset = { if (state.offset.isNaN()) 0f else state.offset }
    Box(
        modifier =
            modifier
                .onSizeChanged { width = it.width }
                .anchoredDraggable(state = state, orientation = Orientation.Horizontal),
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset() }) { content() }
        if (offset() < 0 && next != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset() + width }) { next() }
        }
        if (offset() > 0 && previous != null) {
            Box(Modifier.fillMaxSize().graphicsLayer { translationX = offset() - width }) { previous() }
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
