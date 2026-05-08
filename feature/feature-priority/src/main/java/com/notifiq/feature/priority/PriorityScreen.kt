package com.notifiq.feature.priority

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.AppIconResolver
import com.notifiq.core.designsystem.component.EmptyState
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.NotificationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriorityScreen(
    // FIX 5C: notification IDs are UUID strings, not Longs.
    onNavigateToDetail: (String) -> Unit,
    viewModel: PriorityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Priority")
                        if (uiState.count > 0) {
                            Text(
                                text = "${uiState.count} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Back navigation handled by NavHost */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else if (uiState.notifications.isEmpty()) {
            EmptyState(
                icon = Icons.Default.PushPin,
                title = "No priority notifications",
                message = "Notifications classified as Important or Useful will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.pinnedSources.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Pinned Sources",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.pinnedSources) { source ->
                                    PinnedSourceChip(
                                        packageName = source.packageName,
                                        appName = source.appName,
                                        count = source.count
                                    )
                                }
                            }
                        }
                    }
                }

                items(
                    items = uiState.notifications,
                    key = { it.id }
                ) { notification ->
                    NotificationCard(
                        appName = notification.appName,
                        title = notification.title,
                        text = notification.text,
                        // FIX 4A: NotificationRecord has postTime, not postedTime.
                        timestamp = notification.postTime,
                        label = notification.classificationLabel,
                        confidence = notification.classificationScore,
                        isSuppressed = notification.isSuppressed,
                        isRead = notification.isRead,
                        // FIX 5C: notification.id is a String UUID.
                        onClick = { onNavigateToDetail(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedSourceChip(
    packageName: String,
    appName: String,
    count: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconResolver(packageName = packageName, size = 32.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}