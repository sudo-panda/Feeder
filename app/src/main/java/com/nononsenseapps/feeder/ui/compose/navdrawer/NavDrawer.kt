package com.nononsenseapps.feeder.ui.compose.navdrawer

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_SAVED_ARTICLES
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.ui.compose.components.FeedAvatar
import com.nononsenseapps.feeder.ui.compose.components.textIconSize
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeletableFeed
import com.nononsenseapps.feeder.ui.compose.deletefeed.DeleteFeedDialog
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleDrawerSheet
import com.nononsenseapps.feeder.ui.compose.material3.DismissibleNavigationDrawer
import com.nononsenseapps.feeder.ui.compose.material3.DrawerState
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
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
    onAddFeed: (() -> Unit)? = null,
    onEditFeed: (Long) -> Unit = {},
    onDeleteFeeds: (Iterable<Long>) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    // Fork: Boost-style edit mode for the feed list
    var editMode by rememberSaveable { mutableStateOf(false) }
    var feedToDelete by remember { mutableStateOf<DeletableFeed?>(null) }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) {
            editMode = false
        }
    }

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
            // Fork: the header and the edit bar handle the system bars themselves
            DismissibleDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
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
                    editMode = editMode,
                    onToggleEditMode = onAddFeed?.let { { editMode = !editMode } },
                    // Fork: the sidebar stays open under the new page, so Back returns to it
                    onAddFeed = { onAddFeed?.invoke() },
                    onEditFeed = onEditFeed,
                    onDeleteFeed = { feedId, title -> feedToDelete = DeletableFeed(feedId, title) },
                )
            }
        },
        content = content,
    )

    feedToDelete?.let { feed ->
        DeleteFeedDialog(
            feeds = ImmutableHolder(listOf(feed)),
            onDismiss = { feedToDelete = null },
            onDelete = onDeleteFeeds,
        )
    }
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
    editMode: Boolean = false,
    onToggleEditMode: (() -> Unit)? = null,
    onAddFeed: () -> Unit = {},
    onEditFeed: (Long) -> Unit = {},
    onDeleteFeed: (Long, String) -> Unit = { _, _ -> },
) {
    fun FeedUnreadCount.isSelected(): Boolean =
        when (contentType) {
            ContentType.Tag -> selectedId == ID_UNSET && selectedTag == tag
            else -> selectedId == id
        }

    // Fork: logo header on top and the Edit bar at the bottom, both fixed, with the list between
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
    ) {
        DrawerHeader()
        LazyColumn(
            state = state,
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics {
                        testTag = "feedsAndTags"
                        collectionInfo = CollectionInfo(feedsAndTags.itemCount, 1)
                    },
        ) {
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
                        // Fork: Add feed and Edit/Done for the feeds, just under the line
                        if (onToggleEditMode != null) {
                            DrawerFeedButtons(editMode = editMode, onAddFeed = onAddFeed, onToggleEditMode = onToggleEditMode)
                        }
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
                                leading = { Icon(Icons.Outlined.DynamicFeed, contentDescription = null, modifier = Modifier.size(rowIconSize())) },
                            )
                        }

                        ContentType.SavedArticles -> {
                            DrawerRow(
                                title = stringResource(id = R.string.saved_articles),
                                unreadCount = item.unreadCount,
                                selected = item.isSelected(),
                                onClick = { onItemClick(item) },
                                leading = { Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(rowIconSize())) },
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
                                selected = item.isSelected() && !editMode,
                                onClick = { if (editMode) onEditFeed(item.id) else onItemClick(item) },
                                leading = { FeedAvatar(feedTitle = item.displayTitle, imageUrl = item.imageUrl?.toString(), size = rowIconSize()) },
                                trailing =
                                    if (editMode) {
                                        {
                                            FeedEditButtons(
                                                onEdit = { onEditFeed(item.id) },
                                                onDelete = { onDeleteFeed(item.id, item.displayTitle) },
                                            )
                                        }
                                    } else {
                                        null
                                    },
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
    trailing: (@Composable () -> Unit)? = null,
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
                .padding(start = 4.dp, end = if (trailing != null) 0.dp else 16.dp),
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
        if (trailing != null) {
            trailing()
        } else if (unreadCount > 0) {
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
                modifier = Modifier.size(rowIconSize()).rotate(angle),
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
                leading = { Icon(Icons.Outlined.DynamicFeed, contentDescription = null, modifier = Modifier.size(rowIconSize())) },
            )
            TagRow(title = "News", unreadCount = 12, expanded = true, selected = false, onToggleExpansion = {}, onClick = {})
            DrawerRow(
                title = "A feed",
                unreadCount = 0,
                selected = false,
                onClick = {},
                leading = { FeedAvatar(feedTitle = "A feed", imageUrl = null, size = 24.dp) },
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

// Fork: the app's logo in the accent colour and its name in bold, white at night
@Composable
private fun DrawerHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // The bare glyph sits in the middle of its square, so it needs no gap
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 12.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fork_launcher_monochrome),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(id = R.string.fork_drawer_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.semantics { heading() },
        )
    }
}

// Fork: Add feed on the left and Edit/Done on the right, under the line
@Composable
private fun DrawerFeedButtons(
    editMode: Boolean,
    onAddFeed: () -> Unit,
    onToggleEditMode: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) {
        // Shows "Add", TalkBack says "Add feed"
        val addFeedLabel = stringResource(R.string.add_feed)
        TextButton(onClick = onAddFeed) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(buttonIconSize()))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                stringResource(R.string.fork_add_short),
                modifier = Modifier.semantics { contentDescription = addFeedLabel },
            )
        }
        TextButton(onClick = onToggleEditMode) {
            Icon(
                if (editMode) Icons.Outlined.Check else Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(buttonIconSize()),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(if (editMode) R.string.fork_edit_feeds_done else R.string.fork_edit_feeds))
        }
    }
}

@Composable
private fun FeedEditButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row {
        IconButton(onClick = onEdit) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.edit_feed),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete_feed),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// Fork: icons next to text follow the text size: 24dp next to the rows' 14sp labels
@Composable
private fun rowIconSize() = textIconSize(MaterialTheme.typography.labelLarge, 24f / 14f)

// 18dp next to the Add and Edit buttons' 14sp labels
@Composable
private fun buttonIconSize() = textIconSize(MaterialTheme.typography.labelLarge, 18f / 14f)
