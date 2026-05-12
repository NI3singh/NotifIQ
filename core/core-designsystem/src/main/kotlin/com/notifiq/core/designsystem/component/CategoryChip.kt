package com.notifiq.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.notifiq.core.designsystem.theme.color
import com.notifiq.core.designsystem.theme.mutedColor
import com.notifiq.core.model.ClassificationLabel

@Composable
fun CategoryChip(
    label: ClassificationLabel,
    modifier: Modifier = Modifier
) {
    val labelText = label.name.lowercase().replace("_", " ")

    Text(
        text = labelText,
        style = MaterialTheme.typography.labelSmall,
        color = label.color(false),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(label.mutedColor(false))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .semantics { }
    )
}