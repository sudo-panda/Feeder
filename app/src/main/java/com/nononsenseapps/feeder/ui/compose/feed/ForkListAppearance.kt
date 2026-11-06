package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.ui.compose.feedarticle.SliderRow
import com.nononsenseapps.feeder.ui.compose.theme.LocalListAppearance
import com.nononsenseapps.feeder.ui.compose.theme.LocalListIconScale
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance

// Fork: text and icon size of the article list, see ListAppearance

/**
 * Scales the text in [content] on top of the system font size, the article titles (titleMedium)
 * by their own amount, and sets the action icon size.
 */
@Composable
fun WithListAppearance(content: @Composable () -> Unit) {
    val appearance = LocalListAppearance.current
    val density = LocalDensity.current
    val typography = MaterialTheme.typography
    // The density below already scales every text by textScale, so titles get the difference
    val titleFactor = appearance.titleScale / appearance.textScale
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography =
            typography.copy(
                titleMedium =
                    typography.titleMedium.copy(
                        fontSize = typography.titleMedium.fontSize * titleFactor,
                        lineHeight = typography.titleMedium.lineHeight * titleFactor,
                    ),
            ),
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, density.fontScale * appearance.textScale),
            LocalListIconScale provides appearance.iconScale,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAppearanceSheet(onDismiss: () -> Unit) {
    val repository: Repository by LocalDI.current.instance()
    val appearance by repository.listAppearance.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            Text(
                stringResource(R.string.list_appearance),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            SliderRow(
                label = stringResource(R.string.title_size),
                value = appearance.titleScale,
                range = 0.8f..1.5f,
                steps = 6,
                onChange = { repository.setListAppearance(appearance.copy(titleScale = it)) },
            )
            SliderRow(
                label = stringResource(R.string.fork_text_size),
                value = appearance.textScale,
                range = 0.8f..1.5f,
                steps = 6,
                onChange = { repository.setListAppearance(appearance.copy(textScale = it)) },
            )
            SliderRow(
                label = stringResource(R.string.icon_size),
                value = appearance.iconScale,
                range = 0.8f..1.5f,
                steps = 6,
                marker = Icons.Outlined.StarBorder,
                onChange = { repository.setListAppearance(appearance.copy(iconScale = it)) },
            )
        }
    }
}
