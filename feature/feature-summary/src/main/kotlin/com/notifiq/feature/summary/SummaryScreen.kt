package com.notifiq.feature.summary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.EmptyState
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.theme.color
import com.notifiq.core.model.SummaryReport
import com.notifiq.core.model.SummaryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intelligence Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        } else if (!uiState.hasAnySummary && uiState.liveSummary == null) {
            EmptyState(
                icon = Icons.Default.Notifications,
                title = "No Summaries Yet",
                message = "Your notification summaries will appear here once you start receiving notifications."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily Summary
                uiState.dailySummary?.let { summary ->
                    item {
                        SummaryCard(
                            title = "Daily Summary",
                            summary = summary,
                            subtitle = formatDate(summary.createdAt)
                        )
                    }
                }

                // Weekly Summary
                uiState.weeklySummary?.let { summary ->
                    item {
                        SummaryCard(
                            title = "Weekly Summary",
                            summary = summary,
                            subtitle = formatDate(summary.createdAt)
                        )
                    }
                }

                // Live Summary (when stored summaries don't exist)
                if (uiState.dailySummary == null && uiState.weeklySummary == null) {
                    uiState.liveSummary?.let { summary ->
                        item {
                            SummaryCard(
                                title = "Live Summary",
                                summary = summary,
                                subtitle = "Computed from today's notifications",
                                highlight = true
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    summary: SummaryReport,
    subtitle: String,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highlight) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total notifications
            SummaryStatRow(
                icon = Icons.Default.Notifications,
                label = "Total Notifications",
                value = summary.totalCount.toString(),
                highlight = highlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Important count
            SummaryStatRow(
                icon = Icons.Default.CheckCircle,
                label = "Important",
                value = summary.importantCount.toString(),
                color = Color(0xFF34D399),
                highlight = highlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Useful count
            SummaryStatRow(
                icon = Icons.Default.Shield,
                label = "Useful",
                value = summary.usefulCount.toString(),
                color = Color(0xFF34D399),
                highlight = highlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Spam count
            SummaryStatRow(
                icon = Icons.Default.Warning,
                label = "Spam Filtered",
                value = summary.spamCount.toString(),
                color = Color(0xFFF87171),
                highlight = highlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Suppressed count
            SummaryStatRow(
                icon = Icons.Default.VisibilityOff,
                label = "Suppressed",
                value = summary.suppressedCount.toString(),
                color = Color(0xFFFACC15),
                highlight = highlight
            )

            // Noise reduction bar
            if (summary.noiseReductionPercent > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Noise Reduction",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highlight) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (highlight) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(summary.noiseReductionPercent / 100f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF34D399))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${summary.noiseReductionPercent.toInt()}% noise filtered",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highlight) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun SummaryStatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (highlight) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    color.copy(alpha = 0.7f)
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (highlight) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                color
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
    return sdf.format(java.util.Date(timestamp))
}