package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Repository
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance

// Fork: Boost-style appearance panel, opened from the reader's top bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderAppearanceSheet(
    onDismiss: () -> Unit,
    onMoreTextSettings: () -> Unit,
) {
    val repository: Repository by LocalDI.current.instance()
    val appearance by repository.readerAppearance.collectAsStateWithLifecycle()
    val textScale by repository.textScale.collectAsStateWithLifecycle()
    val justifyText by repository.justifyText.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            Text(
                stringResource(R.string.reader_appearance),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            SliderRow(
                label = stringResource(R.string.title_size),
                value = appearance.titleScale,
                range = 0.8f..1.6f,
                steps = 7,
                onChange = { repository.setReaderAppearance(appearance.copy(titleScale = it)) },
            )
            // Same range and steps as the text scale in Settings → Text
            SliderRow(
                label = stringResource(R.string.text_scale),
                value = textScale,
                range = 1f..2f,
                steps = 9,
                onChange = repository::setTextScale,
            )
            SliderRow(
                label = stringResource(R.string.line_spacing),
                value = appearance.lineSpacing,
                range = 0.9f..1.6f,
                steps = 6,
                onChange = { repository.setReaderAppearance(appearance.copy(lineSpacing = it)) },
            )
            SwitchRow(
                label = stringResource(R.string.justify_article_text),
                checked = justifyText,
                onChange = repository::setJustifyText,
            )
            SwitchRow(
                label = stringResource(R.string.edge_to_edge_images),
                checked = appearance.edgeToEdgeImages,
                onChange = { repository.setReaderAppearance(appearance.copy(edgeToEdgeImages = it)) },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onMoreTextSettings)
                        .semantics { role = Role.Button }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.font), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.text_settings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            SizeLetter(13.sp)
            Slider(
                value = value.coerceIn(range),
                onValueChange = onChange,
                valueRange = range,
                steps = steps,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
            )
            SizeLetter(22.sp)
        }
    }
}

@Composable
private fun SizeLetter(size: TextUnit) {
    Text("A", fontSize = size, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
