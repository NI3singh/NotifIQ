package com.notifiq.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.designsystem.component.AppIconResolver
import com.notifiq.core.designsystem.component.CategoryChip
import com.notifiq.core.designsystem.component.ConfidenceBadge
import com.notifiq.core.designsystem.component.FeedbackActionBar
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.core.designsystem.theme.EyebrowStyle
import com.notifiq.core.designsystem.theme.color

@Composable
fun DetailScreen(
    notificationId: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.FeedbackGiven -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    // Back row
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(8.dp))

                        // Source row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIconResolver(packageName = notification.packageName, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${notification.channelName.ifBlank { "Default" }} · " +
                                        DateTimeUtils.toRelativeTimeString(notification.postTime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CategoryChip(label = notification.classificationLabel)
                        }

                        Spacer(Modifier.height(22.dp))

                        // Title (serif) + body — the reading focus
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = notification.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                        )

                        Spacer(Modifier.height(26.dp))
                        Hairline()
                        Spacer(Modifier.height(20.dp))

                        // Classification reasoning
                        SectionLabel("AI Classification")
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryChip(label = notification.classificationLabel)
                            Spacer(Modifier.width(8.dp))
                            ConfidenceBadge(confidence = notification.classificationScore)
                        }
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(notification.classificationScore)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(notification.classificationLabel.color())
                            )
                        }

                        if (notification.classificationReasons.isNotEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            Text(
                                text = "WHY",
                                style = EyebrowStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            notification.classificationReasons.forEach { reason ->
                                Row(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 7.dp)
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Hairline()
                        Spacer(Modifier.height(16.dp))

                        // Feedback
                        SectionLabel("Teach NotifIQ")
                        Spacer(Modifier.height(10.dp))
                        FeedbackActionBar(
                            onMarkImportant = viewModel::onMarkImportant,
                            onMarkUseful = viewModel::onMarkUseful,
                            onMarkSpam = viewModel::onMarkSpam,
                            onArchive = viewModel::onArchive,
                            onWhitelist = viewModel::onWhitelistApp,
                            onMute = viewModel::onMuteApp,
                        )

                        Spacer(Modifier.height(24.dp))
                        Hairline()
                        Spacer(Modifier.height(20.dp))

                        // Source metadata
                        SectionLabel("Details")
                        Spacer(Modifier.height(8.dp))
                        MetadataRow("Package", notification.packageName)
                        MetadataRow("Channel ID", notification.channelId.ifBlank { "—" })
                        MetadataRow("Category", notification.category.ifBlank { "Unknown" })
                        MetadataRow("Importance", notification.importance.toString())
                        MetadataRow("Action", notification.action.name)
                        MetadataRow(
                            "Posted",
                            DateTimeUtils.formatTimestamp(notification.postTime)
                        )

                        Spacer(Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AppTheme.ext.hairline)
        )
    }
}

@Composable
private fun Hairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.ext.hairline)
    )
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppTheme.ext.hairline)
        )
    }
}
