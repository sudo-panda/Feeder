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
                        if ((far || flung) && currentOnSwipe(next)) {
                            offset.animateTo(if (next) -width.toFloat() else width.toFloat())
                        } else {
                            offset.animateTo(0f)
                        }
                    },
                ).graphicsLayer {
                    translationX = offset.value
                    alpha = 1f - (abs(offset.value) / width).coerceIn(0f, 1f) * 0.5f
                },
    ) {
        content()
    }
}
