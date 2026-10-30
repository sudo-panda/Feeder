package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Fork: tap the left or right fifth of the reader to page up or down, but only when nothing under
 * the finger (a button, a link, a scroll) used the tap. Upstream put invisible boxes on top of the
 * article for this, which swallowed taps on the reader's buttons.
 */
fun Modifier.pagingTaps(
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
): Modifier =
    if (!enabled) {
        this
    } else {
        pointerInput(onPrevious, onNext) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                // Null when the tap was used by something else or turned into a scroll
                val up = waitForUpOrCancellation(pass = PointerEventPass.Final) ?: return@awaitEachGesture
                when {
                    up.position.x < size.width * 0.2f -> onPrevious()
                    up.position.x > size.width * 0.8f -> onNext()
                }
            }
        }
    }
