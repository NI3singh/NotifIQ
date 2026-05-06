package com.notifiq.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FeedbackActionBar(
    onMarkImportant: () -> Unit,
    onMarkUseful: () -> Unit,
    onMarkSpam: () -> Unit,
    onArchive: () -> Unit,
    onWhitelist: () -> Unit,
    onMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FeedbackButton(
            icon = Icons.Default.Bolt,
            label = "Important",
            color = Color(0xFFFF8A4C),
            onClick = onMarkImportant
        )
        FeedbackButton(
            icon = Icons.Default.ThumbUp,
            label = "Useful",
            color = Color(0xFF34D399),
            onClick = onMarkUseful
        )
        FeedbackButton(
            icon = Icons.Default.DoNotDisturb,
            label = "Spam",
            color = Color(0xFFF87171),
            onClick = onMarkSpam
        )
        FeedbackButton(
            icon = Icons.Default.Archive,
            label = "Archive",
            color = Color(0xFF6B7280),
            onClick = onArchive
        )
        FeedbackButton(
            icon = Icons.Default.Shield,
            label = "Whitelist",
            color = Color(0xFF4B7EF5),
            onClick = onWhitelist
        )
        FeedbackButton(
            icon = Icons.Default.DoNotDisturb,
            label = "Mute",
            color = Color(0xFFFACC15),
            onClick = onMute
        )
    }
}

@Composable
private fun FeedbackButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}