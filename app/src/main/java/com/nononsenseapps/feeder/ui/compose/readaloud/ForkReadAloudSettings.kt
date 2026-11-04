package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.AppSetting
import com.nononsenseapps.feeder.model.ForcedAuto
import com.nononsenseapps.feeder.model.ForcedLocale
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.TTSStateHolder
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import java.util.Locale

// Fork: read aloud settings (speed, language, voice), from the player's settings button and from Settings
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadAloudSettingsSheet(onDismiss: () -> Unit) {
    val holder: TTSStateHolder by LocalDI.current.instance()
    val speed by holder.speed.collectAsStateWithLifecycle()
    val language by holder.language.collectAsStateWithLifecycle()
    val languages by holder.availableLanguages.collectAsStateWithLifecycle()
    val voices by holder.availableVoices.collectAsStateWithLifecycle()
    val voiceName by holder.voiceName.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { holder.loadVoices() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
            item {
                Text(
                    stringResource(R.string.read_aloud_settings),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    Text(stringResource(R.string.read_aloud_speed), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text("%.2f×".format(Locale.ROOT, speed).replace(".00", "").replace("0×", "×"), style = MaterialTheme.typography.labelLarge)
                }
                Slider(
                    value = speed,
                    onValueChange = holder::setSpeed,
                    valueRange = 0.5f..2f,
                    steps = 5,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            item { SectionTitle(stringResource(R.string.read_aloud_language)) }
            item {
                Choice(stringResource(R.string.use_app_default), language is AppSetting) { holder.setLanguage(AppSetting) }
            }
            item {
                Choice(stringResource(R.string.use_detect_language), language is ForcedAuto) { holder.setLanguage(ForcedAuto) }
            }
            items(languages, key = { it.toLanguageTag() }) { locale ->
                Choice(locale.getDisplayName(locale), (language as? ForcedLocale)?.locale == locale) {
                    holder.setLanguage(ForcedLocale(locale) as LocaleOverride)
                }
            }
            item { SectionTitle(stringResource(R.string.read_aloud_voice)) }
            item {
                Choice(stringResource(R.string.read_aloud_default_voice), voiceName == null) { holder.setVoice(null) }
            }
            items(voices, key = { it.name }) { voice ->
                val label = if (voice.online) stringResource(R.string.read_aloud_online_voice, voice.label) else voice.label
                Choice(label, voiceName == voice.name) { holder.setVoice(voice.name) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun Choice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 12.dp),
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
