package com.notifiq.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.designsystem.component.AppIconResolver
import com.notifiq.core.designsystem.component.CategoryChip
import com.notifiq.core.designsystem.component.ConfidenceBadge
import com.notifiq.core.designsystem.component.FeedbackActionBar
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.NotificationCard
import com.notifiq.core.designsystem.theme.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    notificationId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.FeedbackGiven -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            uiState.notification?.let { notification ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Main content card with left accent stripe
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 4dp left accent stripe
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(180.dp)
                                    .background(notification.classificationLabel.color(false))
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                // App icon, name, channel, timestamp
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIconResolver(
                                        packageName = notification.packageName,
                                        size = 48.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notification.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = notification.channelName ?: "Default",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateTimeUtils.formatTimestamp(notification.postTime),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    CategoryChip(label = notification.classificationLabel)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Title
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Body text (full, not truncated)
                                Text(
                                    text = notification.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // AI Classification card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "AI Classification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryChip(label = notification.classificationLabel)
                                Spacer(modifier = Modifier.width(12.dp))
                                ConfidenceBadge(confidence = notification.classificationScore)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Horizontal confidence bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(notification.classificationScore)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(notification.classificationLabel.color(false))
                                )
                            }

                            // Reasons section
                            if (notification.classificationReasons.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Reasons",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                notification.classificationReasons.forEach { reason ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feedback Action Bar
                    FeedbackActionBar(
                        onMarkImportant = viewModel::onMarkImportant,
                        onMarkUseful = viewModel::onMarkUseful,
                        onMarkSpam = viewModel::onMarkSpam,
                        onArchive = viewModel::onArchive,
                        onWhitelist = viewModel::onWhitelistApp,
                        onMute = viewModel::onMuteApp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Source Metadata card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Source Metadata",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            MetadataRow("Package", notification.packageName)
                            MetadataRow("Channel ID", notification.channelId)
                            MetadataRow("Category", notification.category ?: "Unknown")
                            MetadataRow("Importance", notification.importance.toString())
                            MetadataRow("Action", notification.action.name)
                            MetadataRow("Timestamp", DateTimeUtils.formatTimestamp(notification.postTime))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}