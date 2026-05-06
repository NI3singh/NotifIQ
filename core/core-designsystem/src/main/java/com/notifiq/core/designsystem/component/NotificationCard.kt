package com.notifiq.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifiq.core.designsystem.theme.color
import com.notifiq.core.model.ClassificationLabel

@Composable
fun NotificationCard(
    appName: String,
    title: String,
    text: String,
    timestamp: Long,
    label: ClassificationLabel,
    confidence: Float,
    isSuppressed: Boolean,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = label.color(false)
    val relativeTime = com.notifiq.core.common.DateTimeUtils.toRelativeTimeString(timestamp)
    val confidencePercent = (confidence * 100).toInt()

    val semanticDescription = "$appName notification: $title. Classified as ${label.name} with $confidencePercent percent confidence"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { },
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
                    .height(80.dp)
                    .background(labelColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Header row: app name left, relative time right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title text (bold if unread)
                Text(
                    text = title,
                    style = if (!isRead) {
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Body text (secondary color)
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer row: CategoryChip and ConfidenceBadge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(label = label)
                    Spacer(modifier = Modifier.width(8.dp))
                    ConfidenceBadge(confidence = confidence)
                    if (isSuppressed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suppressed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}