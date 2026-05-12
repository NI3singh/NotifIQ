package com.notifiq.feature.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.NotificationCard
import com.notifiq.core.designsystem.component.OemInstructionsBanner
import com.notifiq.core.designsystem.component.PermissionHealthBanner
import com.notifiq.core.designsystem.component.StatCard
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // FIX 5C: changed from (Long) to (String) — notification IDs are UUID strings.
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInboxWithFilter: ((String?) -> Unit)? = null,
    userPreferenceDataStore: UserPreferenceDataStore? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NotifIQ") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { PermissionHealthBanner() }

                // FIX 5E: OemInstructionsBanner signature is (onDismiss, modifier).
                // Removed the non-existent userPreferenceDataStore parameter.
                if (userPreferenceDataStore != null) {
                    item {
                        OemInstructionsBanner(
                            onDismiss = { viewModel.dismissOemBanner() }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Important",
                            value = uiState.importantCount.toString(),
                            icon = Icons.Default.FlashOn,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onNavigateToInboxWithFilter?.invoke(ClassificationLabel.IMPORTANT.name)
                            }
                        )
                        StatCard(
                            title = "Useful",
                            value = uiState.usefulCount.toString(),
                            icon = Icons.Default.FlashOn,
                            iconTint = Color(0xFF34D399),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onNavigateToInboxWithFilter?.invoke(ClassificationLabel.USEFUL.name)
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val filteredCount = uiState.lowValueCount + uiState.spamCount
                        StatCard(
                            title = "Filtered",
                            value = filteredCount.toString(),
                            icon = Icons.Default.FlashOn,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToInboxWithFilter?.invoke(null) }
                        )
                        StatCard(
                            title = "Spam",
                            value = uiState.spamCount.toString(),
                            icon = Icons.Default.FlashOn,
                            iconTint = Color(0xFFF87171),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onNavigateToInboxWithFilter?.invoke(ClassificationLabel.SPAM.name)
                            }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Noise Reduction",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.noiseReductionPercent.toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.noiseReductionPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                if (uiState.recentImportantNotifications.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recent Important",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(uiState.recentImportantNotifications) { notification ->
                        NotificationCard(
                            appName = notification.appName,
                            title = notification.title,
                            text = notification.text,
                            // FIX 4A: postTime is the correct field name on NotificationRecord.
                            // postedTime does not exist.
                            timestamp = notification.postTime,
                            label = notification.classificationLabel,
                            confidence = notification.classificationScore,
                            isSuppressed = notification.isSuppressed,
                            isRead = notification.isRead,
                            // FIX 5C: notification.id is a String UUID; lambda now accepts String.
                            onClick = { onNavigateToDetail(notification.id) }
                        )
                    }
                }

                if (uiState.topNoisyApps.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Top Noisy Apps",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(uiState.topNoisyApps) { app ->
                        val maxCount = uiState.topNoisyApps.maxOfOrNull { it.count } ?: 1
                        NoisyAppRow(
                            appName = app.appName,
                            count = app.count,
                            maxCount = maxCount,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoisyAppRow(
    appName: String,
    count: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { count.toFloat() / maxCount.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFFFACC15),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}