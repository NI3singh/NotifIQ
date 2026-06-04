package com.notifiq.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
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
import com.notifiq.core.designsystem.theme.AppTheme

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
    val ext = AppTheme.ext
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FeedbackButton(Icons.Default.Bolt, "Important", ext.labelImportant, onMarkImportant)
        FeedbackButton(Icons.Default.ThumbUp, "Useful", ext.labelUseful, onMarkUseful)
        FeedbackButton(Icons.Default.DoNotDisturb, "Spam", ext.labelSpam, onMarkSpam)
        FeedbackButton(
            Icons.Default.Archive, "Archive",
            MaterialTheme.colorScheme.onSurfaceVariant, onArchive
        )
        FeedbackButton(
            Icons.Default.Shield, "Whitelist",
            MaterialTheme.colorScheme.primary, onWhitelist
        )
        FeedbackButton(Icons.Default.NotificationsOff, "Mute", ext.warning, onMute)
    }
}

@Composable
private fun FeedbackButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
