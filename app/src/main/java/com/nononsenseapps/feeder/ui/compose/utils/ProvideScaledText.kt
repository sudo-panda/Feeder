package com.nononsenseapps.feeder.ui.compose.utils

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalReaderAppearance
import com.nononsenseapps.feeder.ui.compose.theme.LocalTypographySettings

@Composable
fun ProvideScaledText(
    style: TextStyle = LocalTextStyle.current,
    content: @Composable () -> Unit,
) {
    val typographySettings = LocalTypographySettings.current

    ProvideTextStyle(
        style.merge(
            TextStyle(
                fontSize = style.fontSize * typographySettings.fontScale,
                // Fork: line spacing from the reader appearance panel
                lineHeight = style.lineHeight * typographySettings.fontScale * LocalReaderAppearance.current.lineSpacing,
            ),
        ),
    ) {
        content()
    }
}
