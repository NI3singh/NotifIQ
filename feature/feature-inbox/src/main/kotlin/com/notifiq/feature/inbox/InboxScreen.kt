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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.notifiq.core.model.ClassificationLabel

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Inbox",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = inboxSubtitle(pagedNotifications.itemCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = "Search notifications..."
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val totalCount = uiState.filterCounts.values.sum()
                    InboxFilterChip(
                        selected = uiState.selectedFilter == null,
                        onClick = { viewModel.onFilterSelected(null) },
                        label = "All",
                        count = totalCount
                    )
                }

                items(ClassificationLabel.entries.take(4).size) { index ->
                    val label = ClassificationLabel.entries.take(4)[index]
                    InboxFilterChip(
                        selected = uiState.selectedFilter == label,
                        onClick = { viewModel.onFilterSelected(label) },
                        label = label.displayName(),
                        count = uiState.filterCounts[label] ?: 0
                    )
                }

                item {
                    InboxFilterChip(
                        selected = uiState.showUnreadOnly,
                        onClick = viewModel::onToggleUnread,
                        label = "Unread only"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                                "No ${uiState.selectedFilter?.displayName()?.lowercase()} notifications."
                            else -> "Your inbox is clear. New priority alerts will appear here."
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
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

@Composable
private fun InboxFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    count: Int = 0
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (count > 0) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            enabled = true,
            selected = selected
        )
    )
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
