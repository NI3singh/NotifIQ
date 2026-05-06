package com.notifiq.core.designsystem.component

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppIconResolver(
    packageName: String,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val appIcon: Drawable? = remember(packageName) {
        AppIconCache.getAppIcon(context, packageName)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            val bitmap: Bitmap? = remember(appIcon) {
                if (appIcon is BitmapDrawable) {
                    appIcon.bitmap
                } else {
                    val width = size.value.toInt().coerceAtLeast(1)
                    val height = size.value.toInt().coerceAtLeast(1)
                    val canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(canvasBitmap)
                    appIcon.setBounds(0, 0, width, height)
                    appIcon.draw(canvas)
                    canvasBitmap
                }
            }
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(size)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.7f)
            )
        }
    }
}