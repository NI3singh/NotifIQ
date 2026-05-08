package com.notifiq.feature.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.notifiq.core.designsystem.component.EmptyState
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.SearchBar
import com.notifiq.core.designsystem.component.SwipeableNotificationCard
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    // FIX 5C: notification IDs are String UUIDs — changed from (Long) to (String).
    onNavigateToDetail: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagedNotifications = viewModel.pagedNotifications.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastArchivedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inbox")
                        val itemCount = pagedNotifications.itemCount
                        if (itemCount > 0) {
                            Text(
                                text = "$itemCount notifications",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = "Search notifications..."
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = {
                        Row {
                            Text("All")
                            val totalCount = uiState.filterCounts.values.sum()
                            if (totalCount > 0) {
                                Text(
                                    text = " ($totalCount)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                )

                ClassificationLabel.entries.take(4).forEach { label ->
                    val count = uiState.filterCounts[label] ?: 0
                    FilterChip(
                        selected = uiState.selectedFilter == label,
                        onClick = { viewModel.onFilterSelected(label) },
                        label = {
                            Row {
                                Text(label.name.lowercase().replace("_", " "))
                                if (count > 0) {
                                    Text(
                                        text = " ($count)",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = viewModel::onToggleUnread) {
                    Text(
                        text = if (uiState.showUnreadOnly) "Show all" else "Unread only",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.showUnreadOnly)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        title = "No notifications",
                        message = when {
                            uiState.searchQuery.isNotBlank() ->
                                "No notifications match your search."
                            uiState.selectedFilter != null ->
                                "No ${uiState.selectedFilter?.name?.lowercase()
                                    ?.replace("_", " ")} notifications."
                            else -> "Your inbox is empty."
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                // notification.postTime is the correct field on NotificationRecord
                                timestamp = notification.postTime,
                                label = notification.classificationLabel,
                                confidence = notification.classificationScore,
                                isSuppressed = notification.isSuppressed,
                                isRead = notification.isRead,
                                // FIX 5C: notification.id is a UUID String.
                                // The old code did id.toLongOrNull() ?: 0L which always
                                // returned 0 for UUIDs, making every detail load fail.
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