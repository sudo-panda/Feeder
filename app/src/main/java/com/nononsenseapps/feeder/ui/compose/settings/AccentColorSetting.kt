package com.nononsenseapps.feeder.ui.compose.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.AccentColor
import com.nononsenseapps.feeder.archmodel.accentColorFromHex
import com.nononsenseapps.feeder.ui.compose.theme.dark
import com.nononsenseapps.feeder.ui.compose.theme.light

// Fork: Boost-style accent colour picker: preset tiles plus a custom colour

@Composable
fun AccentColorSetting(
    accentColor: AccentColor,
    onAccentColorChange: (AccentColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTiles by rememberSaveable { mutableStateOf(false) }
    var showCustom by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clickable { showTiles = true }
                .semantics { role = Role.Button },
    ) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
        }
        TitleAndSubtitle(
            title = { Text(stringResource(R.string.accent_color)) },
            subtitle = { Text(accentColor.label()) },
        )
        AccentTile(accentColor = accentColor, selected = false, size = 28)
    }

    if (showTiles) {
        AccentTilesDialog(
            current = accentColor,
            onDismiss = { showTiles = false },
            onSelect = {
                showTiles = false
                onAccentColorChange(it)
            },
            onCustom = {
                showTiles = false
                showCustom = true
            },
        )
    }

    if (showCustom) {
        CustomAccentDialog(
            initial = accentColor as? AccentColor.Rgb ?: AccentColor.DEFAULT,
            onDismiss = { showCustom = false },
            onSelect = {
                showCustom = false
                onAccentColorChange(it)
            },
        )
    }
}

@Composable
private fun AccentColor.label(): String =
    when (this) {
        AccentColor.Wallpaper -> stringResource(R.string.accent_wallpaper)
        is AccentColor.Rgb -> toHex()
    }

@Composable
private fun AccentTile(
    accentColor: AccentColor?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 48,
) {
    val color = (accentColor as? AccentColor.Rgb)?.let { Color(it.rgb or 0xFF000000.toInt()) }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size.dp)
                .then(
                    if (color != null) {
                        Modifier.background(color, CircleShape)
                    } else {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    },
                ),
    ) {
        val iconModifier = Modifier.size((size / 2).dp)
        when {
            selected ->
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if ((color ?: Color.White).luminance() > 0.4f) Color.Black else Color.White,
                    modifier = iconModifier,
                )

            accentColor == null ->
                Icon(Icons.Default.Add, contentDescription = null, modifier = iconModifier)

            accentColor == AccentColor.Wallpaper ->
                Icon(Icons.Outlined.Wallpaper, contentDescription = null, modifier = iconModifier)
        }
    }
}

@Composable
private fun AccentTilesDialog(
    current: AccentColor,
    onDismiss: () -> Unit,
    onSelect: (AccentColor) -> Unit,
    onCustom: () -> Unit,
) {
    // null is the custom tile
    val tiles: List<AccentColor?> =
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(AccentColor.Wallpaper)
            addAll(AccentColor.PRESETS)
            add(null)
        }
    val customLabel = stringResource(R.string.accent_custom)
    val wallpaperLabel = stringResource(R.string.accent_wallpaper)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.accent_color)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiles.chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { tile ->
                            val isSelected = tile == current
                            AccentTile(
                                accentColor = tile,
                                selected = isSelected,
                                modifier =
                                    Modifier
                                        .clickable { if (tile == null) onCustom() else onSelect(tile) }
                                        .semantics {
                                            role = Role.Button
                                            selected = isSelected
                                            contentDescription =
                                                when (tile) {
                                                    null -> customLabel
                                                    AccentColor.Wallpaper -> wallpaperLabel
                                                    is AccentColor.Rgb -> tile.toHex()
                                                }
                                        },
                            )
                        }
                    }
                }
                // The first and last tiles have icons, not colours, so say what they do
                Text(
                    text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            stringResource(R.string.accent_tiles_hint_wallpaper)
                        } else {
                            stringResource(R.string.accent_tiles_hint)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun CustomAccentDialog(
    initial: AccentColor.Rgb,
    onDismiss: () -> Unit,
    onSelect: (AccentColor.Rgb) -> Unit,
) {
    val hsl = remember { FloatArray(3).also { ColorUtils.colorToHSL(initial.rgb or 0xFF000000.toInt(), it) } }
    var hue by remember { mutableFloatStateOf(hsl[0]) }
    // Greys make poor accents, so start the custom colour with some saturation
    var saturation by remember { mutableFloatStateOf(hsl[1].coerceAtLeast(0.5f)) }
    var lightness by remember { mutableFloatStateOf(hsl[2].coerceIn(0.2f, 0.8f)) }
    val accent = AccentColor.Rgb(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness)) and 0xFFFFFF)
    var hexText by remember { mutableStateOf(accent.toHex()) }

    fun setFromSliders(
        newHue: Float = hue,
        newLightness: Float = lightness,
    ) {
        hue = newHue
        lightness = newLightness
        hexText = AccentColor.Rgb(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness)) and 0xFFFFFF).toHex()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSelect(accentColorFromHex(hexText) ?: accent) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.accent_custom)) },
        text = {
            val shown = accentColorFromHex(hexText) ?: accent
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // As used in the light and the dark themes
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(48.dp).background(shown.light(), CircleShape))
                    Box(Modifier.size(48.dp).background(shown.dark(), CircleShape))
                }
                Text(stringResource(R.string.accent_hue), style = MaterialTheme.typography.labelLarge)
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .height(12.dp)
                            .background(
                                Brush.horizontalGradient(
                                    (0..6).map { Color.hsl(it * 60f % 360f, saturation, lightness) },
                                ),
                                RoundedCornerShape(6.dp),
                            ),
                    )
                    Slider(
                        value = hue,
                        onValueChange = { setFromSliders(newHue = it) },
                        valueRange = 0f..360f,
                        colors =
                            SliderDefaults.colors(
                                thumbColor = Color(accent.rgb or 0xFF000000.toInt()),
                                activeTrackColor = Color.Transparent,
                                inactiveTrackColor = Color.Transparent,
                            ),
                    )
                }
                Text(stringResource(R.string.accent_shade), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = lightness,
                    onValueChange = { setFromSliders(newLightness = it) },
                    valueRange = 0.2f..0.8f,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color(accent.rgb or 0xFF000000.toInt()),
                            activeTrackColor = Color(accent.rgb or 0xFF000000.toInt()),
                        ),
                )
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { text ->
                        hexText = text
                        accentColorFromHex(text)?.let {
                            val parsed = FloatArray(3)
                            ColorUtils.colorToHSL(it.rgb or 0xFF000000.toInt(), parsed)
                            hue = parsed[0]
                            saturation = parsed[1]
                            lightness = parsed[2]
                        }
                    },
                    label = { Text(stringResource(R.string.accent_hex)) },
                    isError = accentColorFromHex(hexText) == null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
