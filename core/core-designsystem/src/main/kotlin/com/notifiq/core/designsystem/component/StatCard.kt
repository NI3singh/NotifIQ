package com.notifiq.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.core.designsystem.theme.EyebrowStyle
import com.notifiq.core.designsystem.theme.StatNumberStyle

/**
 * Editorial stat figure: a thin accent rule, an oversized tabular number, and an
 * uppercase eyebrow label. Subordinate to a screen's serif hero by design.
 *
 * `icon` is retained for source compatibility with existing callers but is no
 * longer drawn; `iconTint` is reused as the accent-rule color.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, AppTheme.ext.hairline),
    ) {
        Column(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(iconTint)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = StatNumberStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title.uppercase(),
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}
