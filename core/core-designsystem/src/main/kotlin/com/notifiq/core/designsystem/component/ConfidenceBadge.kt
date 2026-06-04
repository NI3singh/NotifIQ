package com.notifiq.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.notifiq.core.designsystem.theme.AppTheme

@Composable
fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (confidence * 100).toInt()
    val ext = AppTheme.ext
    val color = when {
        confidence >= 0.65f -> ext.success
        confidence >= 0.40f -> ext.warning
        else -> ext.labelSpam
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .semantics { }
    ) {
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            color = color
        )
    }
}
