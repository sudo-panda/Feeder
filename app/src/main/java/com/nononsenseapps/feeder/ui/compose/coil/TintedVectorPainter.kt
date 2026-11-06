package com.nononsenseapps.feeder.ui.compose.coil

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun rememberTintedVectorPainter(
    image: ImageVector,
    // Fork: soft grey like the other secondary icons, instead of full black or white
    tintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
) = rememberVectorPainter(
    defaultWidth = image.defaultWidth,
    defaultHeight = image.defaultHeight,
    viewportWidth = image.viewportWidth,
    viewportHeight = image.viewportHeight,
    name = image.name,
    tintColor = tintColor,
    tintBlendMode = image.tintBlendMode,
    autoMirror = image.autoMirror,
    content = { _, _ -> RenderVectorGroup(group = image.root) },
)
