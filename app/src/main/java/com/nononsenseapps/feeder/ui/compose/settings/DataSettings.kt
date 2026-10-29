package com.nononsenseapps.feeder.ui.compose.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.export.exportSavedArticles
import com.nononsenseapps.feeder.model.export.importSavedArticles
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.util.ToastMaker
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.compose.instance
import org.kodein.di.instance
import java.time.LocalDate
import java.time.LocalTime

// Fork: import/export of feeds and saved articles, moved here from the feed screen's menu
@Composable
fun DataSettingsGroup(modifier: Modifier = Modifier) {
    val di = LocalDI.current
    val toastMaker: ToastMaker by instance()
    val coroutineScope = rememberCoroutineScope()

    val opmlImporter =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
                applicationCoroutineScope.launch { importOpml(di, uri) }
            }
        }
    val opmlExporter =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-opml")) { uri ->
            if (uri != null) {
                val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
                applicationCoroutineScope.launch { exportOpml(di, uri) }
            }
        }
    val savedArticleImporter =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
                applicationCoroutineScope.launch { importSavedArticles(di, uri) }
            }
        }
    val savedArticleExporter =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val applicationCoroutineScope: ApplicationCoroutineScope by di.instance()
                applicationCoroutineScope.launch { exportSavedArticles(di, uri) }
            }
        }

    // Launching fails with ActivityNotFoundException if no file picker is installed
    fun launchOrToast(
        @StringRes failureMessage: Int,
        launch: () -> Unit,
    ) {
        try {
            launch()
        } catch (_: Exception) {
            coroutineScope.launch { toastMaker.makeToast(failureMessage) }
        }
    }

    SettingsGroup(title = R.string.data, modifier = modifier) {
        ActionSetting(
            title = stringResource(R.string.import_feeds_from_opml),
            icon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) },
            onClick = {
                launchOrToast(R.string.failed_to_import_OPML) {
                    opmlImporter.launch(
                        arrayOf(
                            // All valid for OPML files
                            "text/xml",
                            "text/x-opml",
                            "application/xml",
                            // This is the mimetype the file actually gets when exported
                            "application/json",
                            "application/octet-stream",
                            // But just in case a file isn't named right etc, accept all
                            "*/*",
                        ),
                    )
                }
            },
        )
        ActionSetting(
            title = stringResource(R.string.export_feeds_to_opml),
            icon = { Icon(Icons.Outlined.SaveAs, contentDescription = null) },
            onClick = {
                launchOrToast(R.string.failed_to_export_OPML) {
                    opmlExporter.launch("feeder-export-${LocalDate.now()}-${LocalTime.now().toSecondOfDay()}.opml")
                }
            },
        )
        ActionSetting(
            title = stringResource(R.string.import_saved_articles),
            icon = { Icon(Icons.Outlined.FileOpen, contentDescription = null) },
            onClick = {
                launchOrToast(R.string.failed_to_import_saved_articles) {
                    savedArticleImporter.launch(
                        arrayOf(
                            "text/plain",
                            // This is the mimetype the file may get from older exports
                            "application/octet-stream",
                            // But just in case a file isn't named right etc, accept all
                            "*/*",
                        ),
                    )
                }
            },
        )
        ActionSetting(
            title = stringResource(R.string.export_saved_articles),
            icon = { Icon(Icons.Outlined.SaveAs, contentDescription = null) },
            onClick = {
                launchOrToast(R.string.failed_to_export_saved_articles) {
                    savedArticleExporter.launch(
                        "feeder-saved-articles-${LocalDate.now()}-${LocalTime.now().toSecondOfDay()}.json",
                    )
                }
            },
        )
    }
}
