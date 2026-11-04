package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearBlockQuote
import com.nononsenseapps.feeder.model.html.LinearElement
import com.nononsenseapps.feeder.model.html.LinearImage
import com.nononsenseapps.feeder.model.html.LinearListItem
import com.nononsenseapps.feeder.model.html.LinearTable
import com.nononsenseapps.feeder.model.html.LinearText

// Fork: Boost-style "find in article" for the reader

/** One match of the search text, inside [text], in the article's top-level element [elementIndex]. */
data class FindMatch(
    val text: LinearText,
    val range: IntRange,
    val elementIndex: Int,
)

/** Matches in reading order, ignoring case. */
fun findMatches(
    article: LinearArticle,
    query: String,
): List<FindMatch> {
    if (query.isBlank()) return emptyList()
    val matches = mutableListOf<FindMatch>()
    article.elements.forEachIndexed { index, element ->
        textsIn(element).forEach { text ->
            var start = text.text.indexOf(query, ignoreCase = true)
            while (start >= 0) {
                matches += FindMatch(text, start until start + query.length, index)
                start = text.text.indexOf(query, startIndex = start + query.length, ignoreCase = true)
            }
        }
    }
    return matches
}

private fun textsIn(element: LinearElement): List<LinearText> =
    when (element) {
        is LinearText -> listOf(element)
        is LinearListItem -> element.content.flatMap { textsIn(it) }
        is LinearBlockQuote -> element.content.flatMap { textsIn(it) }
        is LinearTable -> element.cells.values.flatMap { cell -> cell.content.flatMap { textsIn(it) } }
        is LinearImage -> listOfNotNull(element.caption)
        else -> emptyList()
    }

/** Highlights per text: each match's range, and whether it is the current one. */
fun findHighlights(
    matches: List<FindMatch>,
    current: Int,
): Map<LinearText, List<Pair<IntRange, Boolean>>> =
    matches
        .mapIndexed { index, match -> match.text to (match.range to (index == current)) }
        .groupBy({ it.first }, { it.second })

internal val LocalFindHighlights = compositionLocalOf<Map<LinearText, List<Pair<IntRange, Boolean>>>> { emptyMap() }

@Composable
fun ForkFindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    current: Int,
    total: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onNext() }),
                decorationBox = { field ->
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.find_in_article),
                            style = LocalTextStyle.current.merge(MaterialTheme.typography.titleMedium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    field()
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .focusRequester(focusRequester),
            )
            if (query.isNotBlank()) {
                Text(
                    text = if (total == 0) "0/0" else "${current + 1}/$total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onPrevious, enabled = total > 0) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.find_previous))
            }
            IconButton(onClick = onNext, enabled = total > 0) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.find_next))
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_find))
            }
        }
    }
}
