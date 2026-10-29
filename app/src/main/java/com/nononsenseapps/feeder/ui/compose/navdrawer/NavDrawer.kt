package com.nononsenseapps.feeder.ui.compose.navdrawer

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_SAVED_ARTICLES
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleDrawerSheet
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleNavigationDrawer
import com.nononsenseapps.feeder.ui.compose.material3.DrawerState
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ScreenWithNavDrawer(
    feedsAndTags: LazyPagingItems<FeedUnreadCount>,
    onToggleTagExpansion: (String) -> Unit,
    onDrawerItemSelect: (Long, String) -> Unit,
    drawerState: DrawerState,
    focusRequester: FocusRequester,
    navDrawerListState: LazyListState,
    modifier: Modifier = Modifier,
    selectedId: Long? = null,
    selectedTag: String = "",
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            focusRequester.requestFocus()
        }
    }

    DismissibleNavigationDrawer(
        modifier =
            modifier
                .onKeyEventLikeEscape {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                ListOfFeedsAndTags(
                    feedsAndTags = feedsAndTags,
                    onToggleTagExpansion = onToggleTagExpansion,
                    state = navDrawerListState,
                    { item ->
                        coroutineScope.launch {
                            onDrawerItemSelect(item.id, item.tag)
                            drawerState.close()
                        }
                    },
                    modifier =
                        Modifier
                            .focusRequester(focusRequester),
                    selectedId = selectedId,
                    selectedTag = selectedTag,
                )
            }
        },
        content = content,
    )
}

// Fork: Material 3 style drawer: app name header, pill-shaped rows with the current feed or
// tag highlighted, one icon column so titles and unread counts line up, dimmed read feeds.
@ExperimentalAnimationApi
@Composable
fun ListOfFeedsAndTags(
    feedsAndTags: LazyPagingItems<FeedUnreadCount>,
    onToggleTagExpansion: (String) -> Unit,
    state: LazyListState,
    onItemClick: (FeedIdTag) -> Unit,
    modifier: Modifier = Modifier,
    selectedId: Long? = null,
    selectedTag: String = "",
) {
    fun FeedUnreadCount.isSelected(): Boolean =
        when (contentType) {
            ContentType.Tag -> selectedId == ID_UNSET && selectedTag == tag
            else -> selectedId == id
        }

    LazyColumn(
        state = state,
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        modifier =
            modifier
                .fillMaxSize()
                .semantics {
                    testTag = "feedsAndTags"
                    collectionInfo = CollectionInfo(feedsAndTags.itemCount, 1)
                },
    ) {
        item(key = "header", contentType = "header") {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .padding(start = 28.dp, end = 16.dp, top = 20.dp, bottom = 12.dp)
                        .semantics { heading() },
            )
        }
        items(
            count = feedsAndTags.itemCount,
            key =
                feedsAndTags.itemKey {
                    when (it.id) {
                        ID_ALL_FEEDS -> ID_ALL_FEEDS
                        ID_SAVED_ARTICLES -> ID_SAVED_ARTICLES
                        ID_UNSET -> it.tag
                        else -> it.id
                    }
                },
            contentType =
                feedsAndTags.itemContentType {
                    it.contentType
                },
        ) { itemIndex ->
            val item = feedsAndTags[itemIndex]

            Column {
                if (itemIndex == 2 && item?.contentType?.isFeedListItem == true) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                }

                when (item?.contentType) {
                    null -> {
                        Placeholder()
                    }

                    ContentType.AllFeeds -> {
                        DrawerRow(
                            title = stringResource(id = R.string.all_feeds),
                            unreadCount = item.unreadCount,
                            selected = item.isSelected(),
                            onClick = { onItemClick(item) },
                            leading = { Icon(Icons.Outlined.DynamicFeed, contentDescription = null) },
                        )
                    }

                    ContentType.SavedArticles -> {
                        DrawerRow(
                            title = stringResource(id = R.string.saved_articles),
                            unreadCount = item.unreadCount,
                            selected = item.isSelected(),
                            onClick = { onItemClick(item) },
                            leading = { Icon(Icons.Outlined.StarOutline, contentDescription = null) },
                        )
                    }

                    ContentType.Tag -> {
                        TagRow(
                            title = item.tag,
                            unreadCount = item.unreadCount,
                            expanded = item.expanded,
                            selected = item.isSelected(),
                            onToggleExpansion = { onToggleTagExpansion(item.tag) },
                            onClick = { onItemClick(item) },
                        )
                    }

                    ContentType.ChildFeed, ContentType.TopLevelFeed -> {
                        DrawerRow(
                            title = item.displayTitle,
                            unreadCount = item.unreadCount,
                            selected = item.isSelected(),
                            onClick = { onItemClick(item) },
                            leading = { FeedIcon(imageUrl = item.imageUrl?.toString()) },
                            modifier =
                                if (item.contentType == ContentType.ChildFeed) {
                                    Modifier.padding(start = 16.dp)
                                } else {
                                    Modifier
                                },
                        )
                    }
                }
            }
        }
    }
}

val FeedUnreadCount.contentType: ContentType
    get() =
        when (this.id) {
            ID_UNSET -> ContentType.Tag
            ID_ALL_FEEDS -> ContentType.AllFeeds
            ID_SAVED_ARTICLES -> ContentType.SavedArticles
            else -> {
                if (this.tag.isNotEmpty()) {
                    ContentType.ChildFeed
                } else {
                    ContentType.TopLevelFeed
                }
            }
        }

enum class ContentType {
    AllFeeds,
    SavedArticles,
    Tag,
    ChildFeed,
    TopLevelFeed,
}

private val ContentType.isFeedListItem: Boolean
    get() =
        when (this) {
            ContentType.Tag,
            ContentType.ChildFeed,
            ContentType.TopLevelFeed,
            -> true

            ContentType.AllFeeds,
            ContentType.SavedArticles,
            -> false
        }

@Composable
private fun DrawerRow(
    title: String,
    unreadCount: Int,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingModifier: Modifier = Modifier,
    titleWeight: FontWeight? = null,
) {
    val colors = MaterialTheme.colorScheme
    val textColor =
        when {
            selected -> colors.onSecondaryContainer
            unreadCount > 0 || titleWeight != null -> colors.onSurface
            else -> colors.onSurfaceVariant
        }
    val unreadLabel =
        LocalContext.current.resources.getQuantityString(
            R.plurals.n_unread_articles,
            unreadCount,
            unreadCount,
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .padding(horizontal = 12.dp, vertical = 1.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(CircleShape)
                .background(if (selected) colors.secondaryContainer else Color.Transparent)
                .clickable(onClick = onClick, role = Role.Button)
                .semantics { this.selected = selected }
                .padding(start = 4.dp, end = 16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = leadingModifier.size(40.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
            ) {
                leading()
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = titleWeight,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (unreadCount > 0) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "%d".format(LocalConfiguration.current.locales[0], unreadCount),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = unreadLabel },
            )
        }
    }
}

@Composable
private fun TagRow(
    title: String,
    unreadCount: Int,
    expanded: Boolean,
    selected: Boolean,
    onToggleExpansion: () -> Unit,
    onClick: () -> Unit,
) {
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(),
        label = "angle",
    )
    val toggleExpandLabel = stringResource(id = R.string.toggle_tag_expansion)
    val expandedLabel = stringResource(id = R.string.expanded_tag)
    val contractedLabel = stringResource(id = R.string.contracted_tag)

    DrawerRow(
        title = title,
        unreadCount = unreadCount,
        selected = selected,
        onClick = onClick,
        titleWeight = FontWeight.SemiBold,
        leadingModifier =
            Modifier
                .clip(CircleShape)
                .clickable(onClick = onToggleExpansion)
                .clearAndSetSemantics { },
        leading = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.rotate(angle),
            )
        },
        modifier =
            Modifier.semantics {
                stateDescription = if (expanded) expandedLabel else contractedLabel
                customActions =
                    listOf(
                        CustomAccessibilityAction(toggleExpandLabel) {
                            onToggleExpansion()
                            true
                        },
                    )
            },
    )
}

@Composable
private fun FeedIcon(imageUrl: String?) {
    val fallback = rememberTintedVectorPainter(Icons.Outlined.RssFeed)
    if (imageUrl == null) {
        Icon(Icons.Outlined.RssFeed, contentDescription = null, modifier = Modifier.size(22.dp))
        return
    }
    val pixels = with(LocalDensity.current) { 22.dp.roundToPx() }
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(imageUrl)
                .listener(
                    onError = { a, b ->
                        logDebug("FEEDER_DRAWER", "error ${a.data}", b.throwable)
                    },
                ).scale(Scale.FIT)
                .size(pixels)
                .precision(Precision.INEXACT)
                .build(),
        placeholder = fallback,
        error = fallback,
        contentDescription = stringResource(id = R.string.feed_icon),
        contentScale = ContentScale.Crop,
        modifier =
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp)),
    )
}

@Composable
private fun Placeholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun DrawerRowsPreview() {
    PreviewTheme {
        Column {
            DrawerRow(
                title = "All feeds",
                unreadCount = 99,
                selected = true,
                onClick = {},
                leading = { Icon(Icons.Outlined.DynamicFeed, contentDescription = null) },
            )
            TagRow(title = "News", unreadCount = 12, expanded = true, selected = false, onToggleExpansion = {}, onClick = {})
            DrawerRow(
                title = "A feed",
                unreadCount = 0,
                selected = false,
                onClick = {},
                leading = { FeedIcon(imageUrl = null) },
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
