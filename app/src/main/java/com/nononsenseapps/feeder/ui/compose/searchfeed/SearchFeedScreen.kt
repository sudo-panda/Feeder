package com.nononsenseapps.feeder.ui.compose.searchfeed

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.os.Parcelable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeedParserError
import com.nononsenseapps.feeder.model.FetchError
import com.nononsenseapps.feeder.model.FullTextDecodingFailure
import com.nononsenseapps.feeder.model.FullTextTooLarge
import com.nononsenseapps.feeder.model.HttpError
import com.nononsenseapps.feeder.model.JsonFeedParseError
import com.nononsenseapps.feeder.model.MetaDataParseError
import com.nononsenseapps.feeder.model.NoAlternateFeeds
import com.nononsenseapps.feeder.model.NoBody
import com.nononsenseapps.feeder.model.NoUrl
import com.nononsenseapps.feeder.model.NotHTML
import com.nononsenseapps.feeder.model.NotInitializedYet
import com.nononsenseapps.feeder.model.RSSParseError
import com.nononsenseapps.feeder.model.UnsupportedContentType
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.modifiers.interceptKey
import com.nononsenseapps.feeder.ui.compose.theme.Dimensions
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSizeMetrics
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.ui.compose.utils.StableHolder
import com.nononsenseapps.feeder.ui.compose.utils.getScreenType
import com.nononsenseapps.feeder.ui.compose.utils.rememberApiPermissionState
import com.nononsenseapps.feeder.ui.compose.utils.stableListHolderOf
import com.nononsenseapps.feeder.util.resolvesToLanLocalAddress
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale

private const val LOG_TAG = "FEEDER_SEARCH"

@Suppress("ktlint:compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFeedScreen(
    onNavigateUp: () -> Unit,
    searchFeedViewModel: SearchFeedViewModel,
    onClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    initialFeedUrl: String? = null,
) {
    val windowSize = LocalWindowSizeMetrics.current
    val screenType by remember(windowSize) {
        derivedStateOf {
            getScreenType(windowSize)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = stringResource(id = R.string.add_feed),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        SearchFeedView(
            screenType = screenType,
            searchFeedViewModel = searchFeedViewModel,
            onClick = onClick,
            modifier = Modifier.padding(padding),
            initialFeedUrl = initialFeedUrl ?: "",
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchFeedView(
    screenType: ScreenType,
    searchFeedViewModel: SearchFeedViewModel,
    onClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    initialFeedUrl: String = "",
) {
    val coroutineScope = rememberCoroutineScope()
    var permissionRequestInFlight by remember { mutableStateOf(false) }
    // Saved so a search queued behind the dialog survives activity recreation
    var permissionResults by rememberSaveable { mutableIntStateOf(0) }
    var handledPermissionResults by rememberSaveable { mutableIntStateOf(0) }
    var pendingSearchUrl by rememberSaveable { mutableStateOf("") }
    val localNetworkPermission =
        rememberApiPermissionState(
            permission = "android.permission.ACCESS_LOCAL_NETWORK",
            minimumApiLevel = 37,
            onPermissionResult = {
                permissionRequestInFlight = false
                permissionResults++
            },
        )
    var requestedLanHost by rememberSaveable { mutableStateOf("") }

    // Returns true while a permission dialog is showing, so a search can wait for its result.
    // Hostnames are resolved, so a name that points at a LAN address is caught too.
    suspend fun maybeRequestLocalNetworkPermission(
        url: URL,
        force: Boolean = false,
    ): Boolean {
        if (localNetworkPermission.status is PermissionStatus.Granted) {
            return false
        }
        val host = url.host?.lowercase(Locale.ROOT).orEmpty()
        if (host.isEmpty() || (!permissionRequestInFlight && !force && host == requestedLanHost)) {
            return false
        }
        if (!url.resolvesToLanLocalAddress()) {
            return false
        }
        // The lookup can suspend, so check again before showing the dialog
        if (localNetworkPermission.status is PermissionStatus.Granted) {
            return false
        }
        if (permissionRequestInFlight) {
            return true
        }
        requestedLanHost = host
        permissionRequestInFlight = true
        localNetworkPermission.launchPermissionRequest()
        return true
    }

    var feedUrl by rememberSaveable {
        mutableStateOf(initialFeedUrl)
    }

    var currentlySearching by rememberSaveable {
        mutableStateOf(false)
    }

    var results by rememberSaveable {
        mutableStateOf(listOf<SearchResult>())
    }
    var errors by rememberSaveable {
        mutableStateOf(listOf<FeedParserError>())
    }
    LaunchedEffect(Unit) {
        searchFeedViewModel.ensureSuggestionsLoaded()
    }

    var suggestions by remember {
        mutableStateOf<List<SearchResult>>(emptyList())
    }

    @OptIn(FlowPreview::class)
    LaunchedEffect(Unit) {
        searchFeedViewModel.ensureSuggestionsLoaded()
        snapshotFlow { feedUrl }
            .debounce(150)
            .collectLatest { query ->
                suggestions =
                    if (query.length < 2) {
                        emptyList()
                    } else {
                        searchFeedViewModel.suggestionsFor(query)
                    }
            }
    }
    var currentSearchJob by remember { mutableStateOf<Job?>(null) }
    var permissionCheckJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(feedUrl) {
        delay(300)
        maybeRequestLocalNetworkPermission(sloppyLinkToStrictURLNoThrows(feedUrl))
    }

    fun startSearch(url: URL) {
        currentSearchJob?.cancel()
        val job =
            coroutineScope.launch {
                searchFeedViewModel
                    .searchForFeeds(url)
                    .collect { result ->
                        result
                            .onLeft { e ->
                                errors = errors + e
                            }.onRight { r ->
                                results = results + r
                            }
                    }
            }
        job.invokeOnCompletion {
            if (currentSearchJob == job) {
                currentlySearching = false
            }
        }
        currentSearchJob = job
    }

    LaunchedEffect(permissionResults) {
        if (permissionResults == handledPermissionResults) {
            return@LaunchedEffect
        }
        handledPermissionResults = permissionResults
        val url = pendingSearchUrl
        if (url.isNotEmpty()) {
            pendingSearchUrl = ""
            startSearch(sloppyLinkToStrictURLNoThrows(url))
        }
    }

    SearchFeedView(
        screenType = screenType,
        onUrlChange = { newUrl ->
            feedUrl = newUrl
            pendingSearchUrl = ""
            permissionCheckJob?.cancel()
            permissionCheckJob = null
            currentSearchJob?.cancel()
            currentSearchJob = null
            currentlySearching = false
            results = emptyList()
            errors = emptyList()
        },
        onSearch = { url ->
            results = emptyList()
            errors = emptyList()
            currentlySearching = true
            currentSearchJob?.cancel()
            currentSearchJob = null
            permissionCheckJob?.cancel()
            permissionCheckJob =
                coroutineScope.launch {
                    if (maybeRequestLocalNetworkPermission(url, force = true)) {
                        // Search once the dialog is answered, so the first attempt does not race it
                        pendingSearchUrl = url.toString()
                    } else {
                        pendingSearchUrl = ""
                        startSearch(url)
                    }
                }
        },
        results = StableHolder(results),
        errors = if (currentlySearching) StableHolder(emptyList()) else StableHolder(errors),
        currentlySearching = currentlySearching,
        onClick = onClick,
        modifier = modifier,
        feedUrl = feedUrl,
        suggestions = StableHolder(suggestions),
        onSuggestionClick = { suggestion ->
            onClick(suggestion)
        },
        initialFeedUrl = initialFeedUrl,
    )
}

@Composable
fun SearchFeedView(
    screenType: ScreenType,
    onUrlChange: (String) -> Unit,
    onSearch: (URL) -> Unit,
    results: StableHolder<List<SearchResult>>,
    errors: StableHolder<List<FeedParserError>>,
    currentlySearching: Boolean,
    onClick: (SearchResult) -> Unit,
    suggestions: StableHolder<List<SearchResult>>,
    onSuggestionClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    feedUrl: String = "",
    initialFeedUrl: String = "",
) {
    val focusManager = LocalFocusManager.current
    val dimens = LocalDimens.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val onSearchCallback by rememberUpdatedState(newValue = onSearch)

    // If screen is opened from intent with pre-filled URL, trigger search directly
    LaunchedEffect(initialFeedUrl) {
        if (initialFeedUrl.isNotBlank() &&
            feedUrl == initialFeedUrl &&
            results.item.isEmpty() &&
            errors.item.isEmpty() &&
            isValidUrl(initialFeedUrl)
        ) {
            onSearchCallback(
                sloppyLinkToStrictURLNoThrows(initialFeedUrl),
            )
        }
    }

    // Fork: search by itself once typing pauses on something that looks like an address
    val searchedAlready by rememberUpdatedState(currentlySearching || results.item.isNotEmpty() || errors.item.isNotEmpty())
    LaunchedEffect(feedUrl) {
        if (feedUrl != initialFeedUrl && looksLikeAddress(feedUrl)) {
            delay(1_000)
            // Not if Search was pressed meanwhile
            if (!searchedAlready) {
                onSearchCallback(sloppyLinkToStrictURLNoThrows(feedUrl))
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
    ) {
        if (screenType == ScreenType.DUAL) {
            Row(
                modifier = Modifier.width(dimens.maxContentWidth),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .weight(1f, fill = true)
                            .padding(horizontal = dimens.margin, vertical = 8.dp),
                ) {
                    leftContent(
                        feedUrl = feedUrl,
                        clearFocus = {
                            focusManager.clearFocus()
                        },
                        dimens = dimens,
                        keyboardController = keyboardController,
                        onUrlChange = onUrlChange,
                        onSearch = onSearch,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .weight(1f, fill = true)
                            .padding(horizontal = dimens.margin, vertical = 8.dp),
                ) {
                    rightContent(
                        results = results,
                        errors = errors,
                        currentlySearching = currentlySearching,
                        onClick = onClick,
                        suggestions = suggestions,
                        onSuggestionClick = onSuggestionClick,
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .padding(horizontal = dimens.margin, vertical = 8.dp)
                        .width(dimens.maxContentWidth),
            ) {
                leftContent(
                    feedUrl = feedUrl,
                    onUrlChange = onUrlChange,
                    onSearch = onSearch,
                    clearFocus = {
                        focusManager.clearFocus()
                    },
                    dimens = dimens,
                    keyboardController = keyboardController,
                )
                rightContent(
                    results = results,
                    errors = errors,
                    currentlySearching = currentlySearching,
                    onClick = onClick,
                    suggestions = suggestions,
                    onSuggestionClick = onSuggestionClick,
                )
            }
        }
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.leftContent(
    feedUrl: String,
    onUrlChange: (String) -> Unit,
    onSearch: (URL) -> Unit,
    clearFocus: () -> Unit,
    dimens: Dimensions,
    keyboardController: SoftwareKeyboardController?,
    modifier: Modifier = Modifier,
) {
    val isNotValidUrl by remember(feedUrl) {
        derivedStateOf {
            feedUrl.isNotEmpty() && isNotValidUrl(feedUrl)
        }
    }
    val isValidUrl by remember(feedUrl) {
        derivedStateOf {
            isValidUrl(feedUrl)
        }
    }

    // Fork: rounded search field and a filled Search button, see ForkSearchFeed.kt
    ForkFeedSearchField(
        value = feedUrl,
        onValueChange = onUrlChange,
        isError = isNotValidUrl,
        canSearch = isValidUrl,
        onSearch = {
            if (isValidUrl) {
                onSearch(
                    sloppyLinkToStrictURLNoThrows(feedUrl),
                )
                keyboardController?.hide()
            }
        },
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .interceptKey(Key.Enter) {
                    if (isValidUrl(feedUrl)) {
                        onSearch(
                            sloppyLinkToStrictURLNoThrows(feedUrl),
                        )
                        keyboardController?.hide()
                    }
                }.interceptKey(Key.Escape, clearFocus)
                .safeSemantics {
                    testTag = "urlField"
                },
    )
}

@Composable
fun ColumnScope.rightContent(
    suggestions: StableHolder<List<SearchResult>>,
    results: StableHolder<List<SearchResult>>,
    errors: StableHolder<List<FeedParserError>>,
    currentlySearching: Boolean,
    onClick: (SearchResult) -> Unit,
    onSuggestionClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        // Fork: a short how-to before anything is typed
        if (suggestions.item.isEmpty() && results.item.isEmpty() && errors.item.isEmpty() && !currentlySearching) {
            ForkSearchHint()
        }
        if (suggestions.item.isNotEmpty()) {
            ForkSectionLabel(text = stringResource(id = R.string.feed_search_suggestions_header))
            suggestions.item.forEach { suggestion ->
                SearchResultView(
                    title = suggestion.title,
                    url = suggestion.url,
                    description = suggestion.description,
                    feedImage = suggestion.feedImage,
                    onClick = {
                        onSuggestionClick(suggestion)
                    },
                )
            }
        }

        if (results.item.isEmpty()) {
            for (error in errors.item) {
                val title =
                    when (error) {
                        is FetchError -> stringResource(R.string.failed_to_download)
                        is MetaDataParseError -> stringResource(R.string.failed_to_parse_the_page)
                        is NoAlternateFeeds -> stringResource(R.string.no_feeds_in_the_page)
                        is NotHTML -> stringResource(R.string.content_is_not_html)
                        is NotInitializedYet -> "Not initialized yet" // Should never happen
                        is RSSParseError -> stringResource(R.string.failed_to_parse_rss_feed)
                        is HttpError -> stringResource(R.string.http_error)
                        is JsonFeedParseError -> stringResource(R.string.failed_to_parse_json_feed)
                        is NoBody -> stringResource(R.string.no_body_in_response)
                        is UnsupportedContentType -> stringResource(R.string.unsupported_content_type)
                        is FullTextDecodingFailure -> stringResource(R.string.failed_to_parse_full_article)
                        is FullTextTooLarge -> stringResource(R.string.failed_to_fetch_full_article_too_large)
                        is NoUrl -> stringResource(R.string.no_url)
                    }

                ErrorResultView(
                    title = title,
                    description = error.description,
                    url = error.url,
                    {
                        onClick(
                            SearchResult(
                                title = error.url,
                                url = error.url,
                                description = "",
                                feedImage = "",
                            ),
                        )
                    },
                )
            }
        }
        if (results.item.isNotEmpty()) {
            ForkSectionLabel(text = stringResource(id = R.string.fork_add_feed_found))
        }
        for (result in results.item) {
            SearchResultView(
                title = result.title,
                url = result.url,
                description = result.description,
                feedImage = result.feedImage,
                onClick = {
                    onClick(result)
                },
            )
        }
        AnimatedVisibility(visible = currentlySearching) {
            SearchingIndicator()
        }
    }
}

@Composable
fun SearchingIndicator(modifier: Modifier = Modifier) {
    // Fork: thin bar instead of a spinner
    ForkSearchingIndicator(
        modifier =
            modifier
                .safeSemantics {
                    testTag = "searchingIndicator"
                },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultView(
    title: String,
    url: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    feedImage: String = "",
) {
    val dimens = LocalDimens.current
    // Fork: card with the feed's avatar and an add icon, see ForkSearchFeed.kt
    ForkSearchResultCard(
        title = title,
        url = url,
        description = description,
        imageUrl = feedImage,
        onClick = onClick,
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .safeSemantics {
                    testTag = "searchResult"
                },
    )
}

@Composable
fun ErrorResultView(
    title: String,
    description: String,
    url: String,
    onAddAnyway: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current
    // Fork: see ForkSearchFeed.kt
    ForkSearchErrorCard(
        title = title,
        description = description,
        url = url,
        onAddAnyway = onAddAnyway,
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .safeSemantics {
                    testTag = "errorResult"
                },
    )
}

@Preview(
    name = "Search with results Phone",
    showSystemUi = true,
    device = Devices.NEXUS_5,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
private fun SearchPreview() {
    PreviewTheme {
        Surface {
            SearchFeedView(
                screenType = ScreenType.SINGLE,
                onUrlChange = {},
                onSearch = {},
                results =
                    stableListHolderOf(
                        SearchResult(
                            title = "Atom feed",
                            url = "https://cowboyprogrammer.org/atom",
                            description = "An atom feed",
                            feedImage = "",
                        ),
                        SearchResult(
                            title = "RSS feed",
                            url = "https://cowboyprogrammer.org/rss",
                            description = "An RSS feed",
                            feedImage = "",
                        ),
                    ),
                errors = stableListHolderOf(),
                currentlySearching = false,
                onClick = {},
                suggestions =
                    stableListHolderOf(
                        SearchResult(
                            title = "Suggested feed",
                            url = "https://example.com/rss",
                            description = "",
                            feedImage = "",
                        ),
                    ),
                onSuggestionClick = {},
                modifier = Modifier,
                feedUrl = "https://cowboyprogrammer.org",
                initialFeedUrl = "",
            )
        }
    }
}

@Preview(
    name = "Search with error Phone",
    showSystemUi = true,
    device = Devices.NEXUS_5,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
private fun ErrorPreview() {
    PreviewTheme {
        Surface {
            SearchFeedView(
                screenType = ScreenType.SINGLE,
                onUrlChange = {},
                onSearch = {},
                results = stableListHolderOf(),
                errors =
                    stableListHolderOf(
                        RSSParseError(
                            url = "https://example.com/bad",
                            throwable = NullPointerException("Missing header or something"),
                        ),
                    ),
                currentlySearching = false,
                onClick = {},
                suggestions = stableListHolderOf(),
                onSuggestionClick = {},
                modifier = Modifier,
                feedUrl = "https://cowboyprogrammer.org",
                initialFeedUrl = "",
            )
        }
    }
}

@Preview(
    name = "Search with results Foldable",
    showSystemUi = true,
    device = Devices.PIXEL_FOLD,
    uiMode = UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Search with results Tablet",
    showSystemUi = true,
    device = Devices.PIXEL_C,
    uiMode = UI_MODE_NIGHT_NO,
)
@Composable
private fun SearchPreviewLarge() {
    PreviewTheme {
        Surface {
            SearchFeedView(
                screenType = ScreenType.DUAL,
                onUrlChange = {},
                onSearch = {},
                results =
                    stableListHolderOf(
                        SearchResult(
                            title = "Atom feed",
                            url = "https://cowboyprogrammer.org/atom",
                            description = "An atom feed",
                            feedImage = "",
                        ),
                        SearchResult(
                            title = "RSS feed",
                            url = "https://cowboyprogrammer.org/rss",
                            description = "An RSS feed",
                            feedImage = "",
                        ),
                    ),
                errors = stableListHolderOf(),
                currentlySearching = false,
                onClick = {},
                suggestions =
                    stableListHolderOf(
                        SearchResult(
                            title = "Suggested feed",
                            url = "https://example.com/rss",
                            description = "",
                            feedImage = "",
                        ),
                    ),
                onSuggestionClick = {},
                modifier = Modifier,
                feedUrl = "https://cowboyprogrammer.org",
                initialFeedUrl = "",
            )
        }
    }
}

private fun isValidUrl(url: String): Boolean {
    if (url.isBlank()) {
        return false
    }
    return try {
        try {
            URL(url)
            true
        } catch (_: MalformedURLException) {
            URL("http://$url")
            true
        }
    } catch (e: Exception) {
        false
    }
}

private fun isNotValidUrl(url: String) = !isValidUrl(url)

// Fork: a host with a dot and something after it, like "example.com", before searching on its own
private fun looksLikeAddress(url: String): Boolean {
    if (!isValidUrl(url)) return false
    val host = sloppyLinkToStrictURLNoThrows(url).host.orEmpty()
    return host.contains('.') && host.substringAfterLast('.').length >= 2
}

@Immutable
@Parcelize
data class SearchResult(
    val title: String,
    val url: String,
    val description: String,
    // Empty string instead of null because query params
    val feedImage: String,
) : Parcelable
