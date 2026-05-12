package com.notifiq.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.notifiq.core.model.ClassificationLabel

@Composable
fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.65f -> Color(0xFF34D399) // Green
        confidence >= 0.40f -> Color(0xFFFACC15) // Yellow
        else -> Color(0xFFF87171) // Red
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { }
    ) {
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}