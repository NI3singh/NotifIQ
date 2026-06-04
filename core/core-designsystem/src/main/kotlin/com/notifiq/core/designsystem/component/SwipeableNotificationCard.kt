package com.notifiq.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.core.model.ClassificationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationCard(
    appName: String,
    title: String,
    text: String,
    timestamp: Long,
    label: ClassificationLabel,
    confidence: Float,
    isSuppressed: Boolean,
    isRead: Boolean,
    onClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeLeft()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeRight()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    val ext = AppTheme.ext

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val background = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> ext.labelImportant   // mark important
                SwipeToDismissBoxValue.EndToStart -> ext.labelSpam        // archive
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Bolt
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Archive
                SwipeToDismissBoxValue.Settled -> Icons.Default.Bolt
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }
            val contentDesc = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> "Mark as important"
                SwipeToDismissBoxValue.EndToStart -> "Archive"
                SwipeToDismissBoxValue.Settled -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(background.copy(alpha = 0.92f))
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDesc,
                    tint = Color.White
                )
            }
        },
        content = {
            NotificationCard(
                appName = appName,
                title = title,
                text = text,
                timestamp = timestamp,
                label = label,
                confidence = confidence,
                isSuppressed = isSuppressed,
                isRead = isRead,
                onClick = onClick
            )
        }
    )
}
