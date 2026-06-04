package com.notifiq.feature.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.notifiq.core.designsystem.component.EmptyState
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.SearchBar
import com.notifiq.core.designsystem.component.SwipeableNotificationCard
import com.notifiq.core.designsystem.theme.EyebrowStyle
import com.notifiq.core.model.ClassificationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagedNotifications = viewModel.pagedNotifications.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastArchivedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Editorial header
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                Text(
                    text = "Inbox",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = inboxSubtitle(pagedNotifications.itemCount).uppercase(),
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Search notifications..."
                )
            }

            Spacer(Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                item {
                    val totalCount = uiState.filterCounts.values.sum()
                    FilterTab(
                        selected = uiState.selectedFilter == null && !uiState.showUnreadOnly,
                        label = "All",
                        count = totalCount,
                        onClick = { viewModel.onFilterSelected(null) }
                    )
                }
                items(ClassificationLabel.entries.take(4).size) { index ->
                    val label = ClassificationLabel.entries.take(4)[index]
                    FilterTab(
                        selected = uiState.selectedFilter == label,
                        label = label.displayName(),
                        count = uiState.filterCounts[label] ?: 0,
                        onClick = { viewModel.onFilterSelected(label) }
                    )
                }
                item {
                    FilterTab(
                        selected = uiState.showUnreadOnly,
                        label = "Unread",
                        count = 0,
                        onClick = viewModel::onToggleUnread
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                pagedNotifications.loadState.refresh is LoadState.Loading -> {
                    LoadingState()
                }
                pagedNotifications.loadState.refresh is LoadState.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.Email,
                        title = "Error loading",
                        message = "Something went wrong. Tap to retry."
                    )
                }
                pagedNotifications.itemCount == 0 &&
                        pagedNotifications.loadState.refresh is LoadState.NotLoading -> {
                    EmptyState(
                        icon = Icons.Outlined.Email,
                        title = "Your inbox is clear",
                        message = when {
                            uiState.searchQuery.isNotBlank() ->
                                "No notifications match your search."
                            uiState.selectedFilter != null ->
                                "No ${uiState.selectedFilter?.displayName()?.lowercase()} notifications."
                            else -> "New priority alerts will appear here."
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            count = pagedNotifications.itemCount,
                            key = { index -> pagedNotifications[index]?.id ?: index }
                        ) { index ->
                            val notification = pagedNotifications[index] ?: return@items
                            var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

                            LaunchedEffect(pendingAction) {
                                pendingAction?.invoke()
                                pendingAction = null
                            }

                            SwipeableNotificationCard(
                                appName = notification.appName,
                                title = notification.title,
                                text = notification.text,
                                timestamp = notification.postTime,
                                label = notification.classificationLabel,
                                confidence = notification.classificationScore,
                                isSuppressed = notification.isSuppressed,
                                isRead = notification.isRead,
                                onClick = { onNavigateToDetail(notification.id) },
                                onSwipeLeft = {
                                    lastArchivedId = notification.id
                                    viewModel.onArchive(notification.id)
                                    pendingAction = {
                                        viewModel.onUndoArchive(notification.id)
                                    }
                                },
                                onSwipeRight = {
                                    viewModel.onMarkImportant(notification.id)
                                }
                            )
                        }

                        if (pagedNotifications.loadState.append is LoadState.Loading) {
                            item {
                                LoadingState(modifier = Modifier.height(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(lastArchivedId) {
        lastArchivedId?.let { id ->
            val result = snackbarHostState.showSnackbar(
                message = "Notification archived",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoArchive(id)
            }
            lastArchivedId = null
        }
    }
}

@Composable
private fun FilterTab(
    selected: Boolean,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "filterLabel"
    )
    val underline by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "filterUnderline"
    )

    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = labelColor
            )
            if (count > 0) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(underline)
        )
    }
}

private fun inboxSubtitle(itemCount: Int): String {
    return if (itemCount > 0) {
        "$itemCount notifications captured"
    } else {
        "Smart notification triage"
    }
}

private fun ClassificationLabel.displayName(): String {
    return name.lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
