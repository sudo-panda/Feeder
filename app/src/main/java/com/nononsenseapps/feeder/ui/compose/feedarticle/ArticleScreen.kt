package com.nononsenseapps.feeder.ui.compose.feedarticle

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.Enclosure
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.TextToDisplay
import com.nononsenseapps.feeder.archmodel.formatForFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.localtranslation.BergamotModelDownloadProgress
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.ReadAloudProgress
import com.nononsenseapps.feeder.ui.MainActivityViewModel
import com.nononsenseapps.feeder.ui.ScrollDirection
import com.nononsenseapps.feeder.ui.compose.components.TranslationProgressContent
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.html.ColumnArticleContent
import com.nononsenseapps.feeder.ui.compose.readaloud.HideableTTSPlayer
import com.nononsenseapps.feeder.ui.compose.readaloud.SpeechFollowViewport
import com.nononsenseapps.feeder.ui.compose.readaloud.SpeechHighlight
import com.nononsenseapps.feeder.ui.compose.readaloud.stopSpeechFollowingOnAccessibilityScroll
import com.nononsenseapps.feeder.ui.compose.readaloud.stopSpeechFollowingOnUserScroll
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.ProvideScaledText
import com.nononsenseapps.feeder.ui.compose.utils.ScreenType
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.util.ActivityLauncher
import com.nononsenseapps.feeder.util.stripTrackingParameters
import com.nononsenseapps.feeder.util.unicodeWrap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date
import java.util.TimeZone
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ArticleScreen(
    onNavigateUp: () -> Unit,
    onNavigateToFeed: (Long) -> Unit,
    viewModel: ArticleViewModel,
    mainActivityViewModel: MainActivityViewModel,
    modifier: Modifier = Modifier,
    onNavigateToArticle: (Long) -> Unit = {},
    onNavigateToTextSettings: () -> Unit = {},
    onSwipeToArticle: (id: Long, next: Boolean) -> Unit = { id, _ -> onNavigateToArticle(id) },
) {
    BackHandler(onBack = onNavigateUp)
    val di = LocalDI.current
    val activityLauncher: ActivityLauncher by di.instance()

    val mavm = remember { mainActivityViewModel }
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val readAloudProgress = viewModel.readAloudProgress
    val speechHighlight by viewModel.speechHighlight.collectAsStateWithLifecycle(initialValue = null)
    val isPagingMode by mavm.isPagingMode.collectAsStateWithLifecycle()
    val isAnimatedPaging by mavm.isAnimatedPaging.collectAsStateWithLifecycle()

    val articleScrollState = rememberScrollState(initial = viewModel.scrollPosition)

    // Fork: once this article has slid in after a swipe, other navigation fades again
    LaunchedEffect(Unit) {
        delay(1000)
        ArticleSwipeDirection.current = 0
    }

    LaunchedEffect(articleScrollState) {
        snapshotFlow { articleScrollState.value }
            .debounce(500.milliseconds)
            .collect { viewModel.saveScrollPosition(it) }
    }

    LaunchedEffect(Unit) {
        mavm.scrollCommand.collect { direction ->
            viewModel.ttsStopFollowing()
            val scrollAmount = (articleScrollState.viewportSize * 0.9f).toInt()
            when (direction) {
                ScrollDirection.UP -> {
                    val target = (articleScrollState.value - scrollAmount).coerceAtLeast(0)
                    if (isAnimatedPaging) {
                        articleScrollState.animateScrollTo(target)
                    } else {
                        articleScrollState.scrollTo(target)
                    }
                }

                ScrollDirection.DOWN -> {
                    val target = (articleScrollState.value + scrollAmount).coerceAtMost(articleScrollState.maxValue)
                    if (isAnimatedPaging) {
                        articleScrollState.animateScrollTo(target)
                    } else {
                        articleScrollState.scrollTo(target)
                    }
                }
            }
        }
    }

    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    ArticleScreen(
        viewState = viewState,
        onTextSettings = onNavigateToTextSettings,
        neighbours = viewModel.neighbours.collectAsStateWithLifecycle().value,
        onSwipeArticle = { next ->
            val neighbours = viewModel.neighbours.value
            (if (next) neighbours.next else neighbours.previous)?.let { onSwipeToArticle(it.id, next) }
        },
        onToggleFullText = viewModel::toggleFullText,
        isFullText = viewModel.showingFullText.collectAsStateWithLifecycle().value,
        // Fork: the reader's read/unread button toggles
        onMarkAsUnread = { if (viewState.isUnread) viewModel.markAsRead() else viewModel.markAsUnread() },
        onShare = {
            if (viewState.articleId > ID_UNSET) {
                val intent =
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            val articleLink = viewState.articleLink
                            if (articleLink != null) {
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    stripTrackingParameters(articleLink),
                                )
                            }
                            putExtra(Intent.EXTRA_TITLE, viewState.articleTitle)
                            type = "text/plain"
                        },
                        null,
                    )
                activityLauncher.startActivity(
                    openAdjacentIfSuitable = false,
                    intent = intent,
                )
            }
        },
        onOpenInCustomTab = {
            viewState.articleLink?.let { link ->
                activityLauncher.openLinkInCustomTab(link, toolbarColor)
            }
        },
        onFeedTitleClick = {
            onNavigateToFeed(viewState.articleFeedId)
        },
        onOpenAudioPlayer = viewModel::openPodcastPlayer,
        onShowToolbarMenu = viewModel::setToolbarMenuVisible,
        ttsOnPlay = viewModel::ttsPlay,
        ttsOnPause = viewModel::ttsPause,
        ttsOnStop = viewModel::ttsStop,
        ttsOnSkipNext = viewModel::ttsSkipNext,
        ttsOnSelectLanguage = viewModel::ttsOnSelectLanguage,
        readAloudProgress = readAloudProgress,
        speechHighlight = speechHighlight,
        ttsOnSeekTo = viewModel::ttsSeekTo,
        ttsOnUserScroll = viewModel::ttsStopFollowing,
        ttsOnGoToSpokenText = {
            viewModel.ttsFocusSpokenText()?.let { articleId ->
                if (articleId != viewState.articleId) onNavigateToArticle(articleId)
            }
        },
        podcastOnPlay = viewModel::podcastPlay,
        podcastOnPause = viewModel::podcastPause,
        podcastOnStop = viewModel::stopPodcastPlayback,
        podcastOnSeekBack = { viewModel.podcastSeekBy(-10_000) },
        podcastOnSeekForward = { viewModel.podcastSeekBy(10_000) },
        podcastOnSeekTo = viewModel::podcastSeekTo,
        onToggleBookmark = {
            viewModel.setBookmarked(!viewState.isBookmarked)
        },
        articleScrollState = articleScrollState,
        onNavigateUp = onNavigateUp,
        onSummarize = {
            viewModel.summarize()
        },
        onTranslate = {
            viewModel.translate()
        },
        onOpenSystemTranslationSettings = {
            activityLauncher.startActivity(
                openAdjacentIfSuitable = false,
                intent = Intent(Settings.ACTION_SETTINGS),
            )
            viewModel.dismissSystemTranslationSettingsPrompt()
        },
        onDismissSystemTranslationSettings = viewModel::dismissSystemTranslationSettingsPrompt,
        onDownloadBergamotRuntime = viewModel::downloadBergamotRuntimeAndTranslate,
        onDismissBergamotRuntimeDownload = viewModel::dismissBergamotRuntimeDownloadPrompt,
        modifier = modifier,
        isPagingMode = isPagingMode,
        isAnimatedPaging = isAnimatedPaging,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun ArticleScreen(
    viewState: ArticleScreenViewState,
    onToggleFullText: () -> Unit,
    onMarkAsUnread: () -> Unit,
    onShare: () -> Unit,
    onOpenInCustomTab: () -> Unit,
    onFeedTitleClick: () -> Unit,
    onOpenAudioPlayer: (url: String) -> Unit,
    onShowToolbarMenu: (Boolean) -> Unit,
    ttsOnPlay: () -> Unit,
    ttsOnPause: () -> Unit,
    ttsOnStop: () -> Unit,
    ttsOnSkipNext: () -> Unit,
    ttsOnSelectLanguage: (LocaleOverride) -> Unit,
    podcastOnPlay: () -> Unit,
    podcastOnPause: () -> Unit,
    podcastOnStop: () -> Unit,
    podcastOnSeekBack: () -> Unit,
    podcastOnSeekForward: () -> Unit,
    podcastOnSeekTo: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    articleScrollState: ScrollState,
    onNavigateUp: () -> Unit,
    onSummarize: () -> Unit,
    onTranslate: () -> Unit,
    onOpenSystemTranslationSettings: () -> Unit,
    onDismissSystemTranslationSettings: () -> Unit,
    onDownloadBergamotRuntime: () -> Unit,
    onDismissBergamotRuntimeDownload: () -> Unit,
    modifier: Modifier = Modifier,
    readAloudProgress: StateFlow<ReadAloudProgress>? = null,
    speechHighlight: SpeechHighlight? = null,
    ttsOnSeekTo: (Long) -> Unit = {},
    ttsOnGoToSpokenText: () -> Unit = {},
    ttsOnUserScroll: () -> Unit = {},
    isPagingMode: Boolean = false,
    isAnimatedPaging: Boolean = false,
    onTextSettings: () -> Unit = {},
    onSwipeArticle: (next: Boolean) -> Unit = {},
    neighbours: ArticleNeighbours = ArticleNeighbours(),
    isFullText: Boolean = false,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Fork: Boost-style find in article
    var findOpen by rememberSaveable { mutableStateOf(false) }
    var findQuery by rememberSaveable { mutableStateOf("") }
    var findCurrent by rememberSaveable { mutableIntStateOf(0) }
    var findRequest by remember { mutableIntStateOf(0) }
    val findMatchList = remember(viewState.articleContent, findQuery) { findMatches(viewState.articleContent, findQuery) }
    val findHighlightMap = remember(findMatchList, findCurrent, findOpen) { if (findOpen) findHighlights(findMatchList, findCurrent) else emptyMap() }
    BackHandler(enabled = findOpen) { findOpen = false }

    // Fork: Boost-style reader appearance panel
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    if (showAppearance) {
        ReaderAppearanceSheet(
            onDismiss = { showAppearance = false },
            onMoreTextSettings = {
                showAppearance = false
                onTextSettings()
            },
        )
    }

    val bottomBarVisibleState = remember { MutableTransitionState(viewState.isBottomBarVisible) }
    LaunchedEffect(viewState.isBottomBarVisible) {
        bottomBarVisibleState.targetState = viewState.isBottomBarVisible
    }

    val focusArticle = remember { FocusRequester() }
    val focusTopBar = remember { FocusRequester() }

    val closeMenuText = stringResource(id = R.string.close_menu)

    // Fork: show the article's title in the top bar once its header has scrolled away
    val headerHeightPx = with(LocalDensity.current) { 180.dp.toPx() }
    val showTitleInBar by remember { derivedStateOf { articleScrollState.value > headerHeightPx } }

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        // Fork: the reader is a white page (dark grey, or black, at night), like a Boost post
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (findOpen) {
                ForkFindBar(
                    query = findQuery,
                    onQueryChange = {
                        findQuery = it
                        findCurrent = 0
                        findRequest++
                    },
                    current = findCurrent,
                    total = findMatchList.size,
                    onPrevious = {
                        if (findMatchList.isNotEmpty()) {
                            findCurrent = (findCurrent - 1 + findMatchList.size) % findMatchList.size
                            findRequest++
                        }
                    },
                    onNext = {
                        if (findMatchList.isNotEmpty()) {
                            findCurrent = (findCurrent + 1) % findMatchList.size
                            findRequest++
                        }
                    },
                    onClose = { findOpen = false },
                )
            } else {
                // Fork: keeps the status bar covered once the top bar has scrolled away
                Box {
                    SensibleTopAppBar(
                        modifier =
                            Modifier
                                .focusGroup()
                                .focusRequester(focusTopBar)
                                .focusProperties {
                                    down = focusArticle
                                },
                        scrollBehavior = scrollBehavior,
                        // Fork: the header shows the feed's name, the bar shows the title once scrolled
                        title = if (showTitleInBar) viewState.articleTitle else "",
                        titleStyle = MaterialTheme.typography.titleMedium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.go_back),
                                )
                            }
                        },
                        actions = {
                            // Fork: fetch full article and reader appearance live in the menu, and the
                            // action bar under the title opens the article in the browser
                            PlainTooltipBox(tooltip = { Text(stringResource(id = R.string.find_in_article)) }) {
                                IconButton(onClick = { findOpen = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(id = R.string.find_in_article),
                                    )
                                }
                            }

                            PlainTooltipBox(tooltip = { Text(stringResource(id = R.string.open_menu)) }) {
                                Box {
                                    IconButton(
                                        onClick = { onShowToolbarMenu(true) },
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(id = R.string.open_menu),
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = viewState.showToolbarMenu,
                                        onDismissRequest = { onShowToolbarMenu(false) },
                                        modifier =
                                            Modifier
                                                .onKeyEventLikeEscape {
                                                    onShowToolbarMenu(false)
                                                },
                                    ) {
                                        // Fork: Share, Save, Mark as unread, Open in browser and Read aloud are
                                        // in the reader's action bar
                                        DropdownMenuItem(
                                            onClick = {
                                                onShowToolbarMenu(false)
                                                showAppearance = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.FormatSize,
                                                    contentDescription = null,
                                                )
                                            },
                                            text = {
                                                Text(stringResource(id = R.string.reader_appearance))
                                            },
                                        )
                                        if (viewState.showSummarize) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    onShowToolbarMenu(false)
                                                    onSummarize()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.AutoFixHigh,
                                                        contentDescription = null,
                                                    )
                                                },
                                                text = {
                                                    Text(stringResource(id = R.string.summarize))
                                                },
                                            )
                                        }

                                        if (viewState.showTranslate) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    onShowToolbarMenu(false)
                                                    onTranslate()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Translate,
                                                        contentDescription = null,
                                                    )
                                                },
                                                text = {
                                                    Text(
                                                        stringResource(
                                                            if (viewState.isShowingTranslated) {
                                                                R.string.show_original_language
                                                            } else {
                                                                R.string.translate_article
                                                            },
                                                            viewState.translationSourceLanguage.ifBlank {
                                                                stringResource(R.string.original_article)
                                                            },
                                                        ),
                                                    )
                                                },
                                            )
                                        }

                                        // Hidden button for TalkBack
                                        DropdownMenuItem(
                                            onClick = {
                                                onShowToolbarMenu(false)
                                            },
                                            text = {},
                                            modifier =
                                                Modifier
                                                    .height(0.dp)
                                                    .safeSemantics {
                                                        contentDescription = closeMenuText
                                                        role = Role.Button
                                                    },
                                        )
                                    }
                                }
                            }
                        },
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .background(
                                    if (scrollBehavior.state.collapsedFraction > 0f || scrollBehavior.state.overlappedFraction > 0.01f) {
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                ),
                    )
                }
            }
        },
        bottomBar = {
            if (viewState.podcastPlayerState.isVisible) {
                HideablePodcastPlayer(
                    visibleState = bottomBarVisibleState,
                    viewState = viewState.podcastPlayerState,
                    onPlay = podcastOnPlay,
                    onPause = podcastOnPause,
                    onStop = podcastOnStop,
                    onSeekBack = podcastOnSeekBack,
                    onSeekForward = podcastOnSeekForward,
                    onSeekTo = podcastOnSeekTo,
                )
            } else {
                HideableTTSPlayer(
                    visibleState = bottomBarVisibleState,
                    currentlyPlaying = viewState.isTTSPlaying,
                    onPlay = ttsOnPlay,
                    onPause = ttsOnPause,
                    onStop = ttsOnStop,
                    onSkipNext = ttsOnSkipNext,
                    languages = ImmutableHolder(viewState.ttsLanguages),
                    onSelectLanguage = ttsOnSelectLanguage,
                    progress = readAloudProgress,
                    onSeekTo = ttsOnSeekTo,
                    onGoToSpokenText = ttsOnGoToSpokenText,
                    displayedArticleId = viewState.articleId,
                )
            }
        },
    ) { padding ->
        // Box handles the dynamic padding so ArticleContent don't have to recompose on scroll
        SpeechFollowViewport(
            centerSpeech = speechHighlight?.followSpokenText == true,
            modifier =
                Modifier
                    .padding(padding)
                    .stopSpeechFollowingOnUserScroll(ttsOnUserScroll),
        ) {
            val coroutineScope = rememberCoroutineScope()
            // Fork: swipe left or right for the next or previous article
            CompositionLocalProvider(LocalFindHighlights provides findHighlightMap) {
                ArticleSwipeBox(
                    onSwipe = onSwipeArticle,
                    previous = neighbours.previous?.let { peek -> { ArticlePeekPage(peek) } },
                    next = neighbours.next?.let { peek -> { ArticlePeekPage(peek) } },
                ) {
                    ArticleContent(
                        viewState = viewState,
                        screenType = ScreenType.SINGLE,
                        speechHighlight = speechHighlight,
                        articleScrollState = articleScrollState,
                        onUserScroll = ttsOnUserScroll,
                        onFeedTitleClick = onFeedTitleClick,
                        onOpenAudioPlayer = onOpenAudioPlayer,
                        onOpenInCustomTab = onOpenInCustomTab,
                        onBookmark = onToggleBookmark,
                        onMarkAsUnread = onMarkAsUnread,
                        onShare = onShare,
                        onReadAloud = ttsOnPlay,
                        isFullText = isFullText,
                        onToggleFullText = onToggleFullText,
                        findFocusElement = findMatchList.getOrNull(findCurrent)?.elementIndex?.takeIf { findOpen },
                        findFocusRequest = findRequest,
                        modifier =
                            Modifier
                                // Fork: no tap zones for page turns, they got in the way of the buttons
                                .focusGroup()
                                .focusRequester(focusArticle)
                                .focusProperties {
                                    up = focusTopBar
                                },
                    )
                }
            }
        }
    }

    if (viewState.systemTranslationSettingsMessage.isNotBlank()) {
        SystemTranslationSettingsDialog(
            message = viewState.systemTranslationSettingsMessage,
            onOpenSettings = onOpenSystemTranslationSettings,
            onDismiss = onDismissSystemTranslationSettings,
        )
    }
    if (viewState.showBergamotRuntimeDownloadPrompt) {
        BergamotRuntimeDownloadDialog(
            onDownload = onDownloadBergamotRuntime,
            onDismiss = onDismissBergamotRuntimeDownload,
        )
    }
}

@Composable
private fun BergamotRuntimeDownloadDialog(
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bergamot_runtime_download_title)) },
        text = { Text(stringResource(R.string.bergamot_runtime_download_warning)) },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(stringResource(R.string.download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun SystemTranslationSettingsDialog(
    message: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
fun ArticleContent(
    viewState: ArticleScreenViewState,
    screenType: ScreenType,
    onFeedTitleClick: () -> Unit,
    onOpenAudioPlayer: (url: String) -> Unit,
    onOpenInCustomTab: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsUnread: () -> Unit,
    articleScrollState: ScrollState,
    modifier: Modifier = Modifier,
    speechHighlight: SpeechHighlight? = null,
    onUserScroll: (() -> Unit)? = null,
    onShare: () -> Unit = {},
    onReadAloud: () -> Unit = {},
    isFullText: Boolean = false,
    onToggleFullText: () -> Unit = {},
    findFocusElement: Int? = null,
    findFocusRequest: Int = 0,
) {
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()

    val context = LocalContext.current
    val activityLauncher: ActivityLauncher by LocalDI.current.instance()
    val coroutineScope = rememberCoroutineScope()

    // Track Y positions of article elements by index for anchor link scrolling
    val elementPositions = remember { mutableMapOf<Int, Float>() }

    // Fork: scroll the current find-in-article match into view, a bit below the top
    val findPositions = remember { mutableMapOf<Int, Float>() }
    var viewportTop by remember { mutableFloatStateOf(0f) }
    val findMargin = with(LocalDensity.current) { 96.dp.toPx() }
    LaunchedEffect(findFocusElement, findFocusRequest) {
        val y = findFocusElement?.let { findPositions[it] } ?: return@LaunchedEffect
        articleScrollState.animateScrollBy(y - viewportTop - findMargin)
    }
    val contentImageUrls = remember(viewState.articleContent) { viewState.articleContent.imageUrls }

    // Fork: the feed's icon for the reader header, the same one the feed list and drawer show
    val repository: Repository by LocalDI.current.instance()
    val feedImageUrl by produceState<String?>(null, viewState.articleFeedId) {
        value = repository.getFeed(viewState.articleFeedId)?.imageUrl?.toString()
    }

    ReaderView(
        screenType = screenType,
        wordCount = viewState.wordCount,
        onEnclosureClick = {
            if (viewState.enclosure.present) {
                if (viewState.useInAppAudioPlayer && shouldOpenInPodcastPlayer(viewState.enclosure.link, viewState.enclosure)) {
                    onOpenAudioPlayer(viewState.enclosure.link)
                } else {
                    activityLauncher.openLinkInBrowser(link = viewState.enclosure.link)
                }
            }
        },
        onFeedTitleClick = onFeedTitleClick,
        enclosure = viewState.enclosure,
        articleTitle = viewState.articleTitle,
        feedTitle = viewState.feedDisplayTitle,
        saved = viewState.isBookmarked,
        onSaveClick = onBookmark,
        isUnread = viewState.isUnread,
        onMarkAsUnread = onMarkAsUnread,
        // Fork: short "author · date" like the feed cards, instead of "by …, Friday, 30 October …"
        authorDate =
            listOfNotNull(
                viewState.author?.takeIf { it.isNotBlank() }?.let { context.unicodeWrap(it) },
                viewState.pubDate.formatForFeed(context).takeIf { it.isNotBlank() },
            ).joinToString(" · ").takeIf { it.isNotEmpty() },
        image = viewState.image,
        showHeaderImage = viewState.textToDisplay == TextToDisplay.CONTENT,
        contentImageUrls = contentImageUrls,
        modifier =
            (if (onUserScroll != null) modifier.stopSpeechFollowingOnAccessibilityScroll(articleScrollState, onUserScroll) else modifier)
                .onGloballyPositioned { viewportTop = it.positionInRoot().y },
        articleScrollState = articleScrollState,
        onShare = onShare,
        // Fork: the only open-in-browser button now, it does what the top bar's button did
        onOpenInBrowser = onOpenInCustomTab,
        onReadAloud = onReadAloud,
        isFullText = isFullText,
        onToggleFullText = onToggleFullText,
        feedImageUrl = feedImageUrl,
    ) { indexOffset ->
        var offsetCounter = indexOffset

        if (viewState.isTranslationLoading) {
            offsetCounter++
            viewState.translationModelDownloadProgress?.let { progress ->
                TranslationModelDownloadProgress(progress)
            } ?: LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (viewState.openAiSummary !is OpenAISummaryState.Empty) {
            offsetCounter++
            SummarySection(viewState.openAiSummary)
        }
        // Can take a composition or two before viewstate is set to its actual values
        if (viewState.articleId > ID_UNSET) {
            when (viewState.textToDisplay) {
                TextToDisplay.CONTENT -> {
                    ColumnArticleContent(
                        articleContent = viewState.articleContent,
                        speechHighlight = speechHighlight,
                        onLinkClick = { link, index ->
                            if (index != null && elementPositions.containsKey(index)) {
                                onUserScroll?.invoke()
                                // Anchor link - scroll to the element position
                                val yPosition = elementPositions[index]
                                if (yPosition != null) {
                                    coroutineScope.launch {
                                        articleScrollState.animateScrollTo(yPosition.toInt())
                                    }
                                }
                            } else {
                                if (viewState.useInAppAudioPlayer && shouldOpenInPodcastPlayer(link, viewState.enclosure)) {
                                    onOpenAudioPlayer(link)
                                } else {
                                    // External link - open in browser/custom tab
                                    activityLauncher.openLink(
                                        link = link,
                                        toolbarColor = toolbarColor,
                                    )
                                }
                            }
                        },
                        onElementPosition = { index, yPosition ->
                            findPositions[index] = yPosition
                            elementPositions[offsetCounter + index] = yPosition
                        },
                    )
                }

                TextToDisplay.LOADING_FULLTEXT -> {
                    Text(text = stringResource(id = R.string.fetching_full_article))
                }

                TextToDisplay.FAILED_TO_LOAD_FULLTEXT -> {
                    Text(text = stringResource(id = R.string.failed_to_fetch_full_article))
                }

                TextToDisplay.FAILED_MISSING_BODY -> {
                    Text(text = stringResource(id = R.string.failed_to_fetch_full_article_missing_body))
                }

                TextToDisplay.FAILED_MISSING_LINK -> {
                    Text(text = stringResource(id = R.string.failed_to_fetch_full_article_missing_link))
                }

                TextToDisplay.FAILED_NOT_HTML -> {
                    Text(text = stringResource(id = R.string.failed_to_fetch_full_article_not_html))
                }

                TextToDisplay.FAILED_FULLTEXT_TOO_LARGE -> {
                    Text(text = stringResource(id = R.string.failed_to_fetch_full_article_too_large))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenInCustomTab) {
                        Text(text = stringResource(id = R.string.open_in_web_view))
                    }
                }
            }
        }
    }
}

internal fun formatArticleDate(
    context: Context,
    publicationDate: ZonedDateTime?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String =
    publicationDate?.let {
        val skeleton = if (DateFormat.is24HourFormat(context)) "yMMMMEEEEdHm" else "yMMMMEEEEdhm"
        val locale = context.resources.configuration.locales[0]
        SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
            .apply { timeZone = TimeZone.getTimeZone(zoneId) }
            .format(Date.from(it.toInstant()))
    } ?: ""

@Composable
private fun TranslationModelDownloadProgress(progress: BergamotModelDownloadProgress) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TranslationProgressContent(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SummarySection(summary: OpenAISummaryState) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (summary) {
            OpenAISummaryState.Empty -> {}
            OpenAISummaryState.Loading ->
                LinearProgressIndicator(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                )
            is OpenAISummaryState.Result -> {
                Column(modifier = Modifier.padding(8.dp)) {
                    summary.annotatedStrings.forEachIndexed { index, annotatedString ->
                        Text(
                            text = annotatedString,
                            modifier =
                                Modifier.padding(
                                    bottom = if (index < summary.annotatedStrings.size - 1) 8.dp else 0.dp,
                                ),
                        )
                    }
                }
            }
        }
    }
}

// Fork: the neighbouring article as the reader will show it, built only from stored data
@Composable
private fun ArticlePeekPage(peek: ArticlePeek) {
    val context = LocalContext.current
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        ReaderView(
            screenType = ScreenType.SINGLE,
            wordCount = peek.wordCount,
            onEnclosureClick = {},
            onFeedTitleClick = {},
            enclosure = Enclosure(),
            articleTitle = peek.title,
            feedTitle = peek.feedTitle,
            saved = peek.bookmarked,
            onSaveClick = {},
            isUnread = peek.unread,
            onMarkAsUnread = {},
            authorDate =
                listOfNotNull(
                    peek.author?.takeIf { it.isNotBlank() },
                    peek.pubDate.formatForFeed(context).takeIf { it.isNotBlank() },
                ).joinToString(" · ").takeIf { it.isNotEmpty() },
            image = peek.image,
            showHeaderImage = true,
            contentImageUrls = emptySet(),
            feedImageUrl = peek.feedImageUrl,
        ) { _ ->
            ProvideScaledText(style = MaterialTheme.typography.bodyLarge) {
                Text(peek.snippet, modifier = Modifier.width(LocalDimens.current.maxReaderWidth))
            }
        }
    }
}
