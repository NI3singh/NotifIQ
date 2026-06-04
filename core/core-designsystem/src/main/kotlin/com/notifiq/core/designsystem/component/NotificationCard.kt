package com.notifiq.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.core.designsystem.theme.EyebrowStyle
import com.notifiq.core.designsystem.theme.color
import com.notifiq.core.model.ClassificationLabel

/**
 * Editorial "paper" row: a soft warm card (no shadow), a small label dot, an
 * uppercase app eyebrow, a strong title, and quiet supporting metadata.
 */
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
    val labelColor = label.color()
    val relativeTime = DateTimeUtils.toRelativeTimeString(timestamp)
    val confidencePercent = (confidence * 100).toInt()
    val semanticLabel =
        "$appName notification: $title. Classified as ${label.name} with $confidencePercent percent confidence"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .semantics { contentDescription = semanticLabel },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, AppTheme.ext.hairline),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(labelColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appName.uppercase(),
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
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
