package com.notifiq.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkTextPrimary,
    primaryContainer = DarkSurface2,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkAccent,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurface3,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkAccent,
    onTertiary = DarkTextPrimary,
    tertiaryContainer = DarkSurface3,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkTextSecondary,
    outlineVariant = DarkSurface3
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightSurface,
    primaryContainer = LightSurface2,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightAccent,
    onSecondary = LightSurface,
    secondaryContainer = LightSurface3,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightAccent,
    onTertiary = LightSurface,
    tertiaryContainer = LightSurface3,
    onTertiaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextSecondary,
    outline = LightTextSecondary,
    outlineVariant = LightSurface3
)

@Composable
fun NotifIQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalView.current.context
    val useDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamicColors && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColors && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotifIQTypography,
        shapes = NotifIQShapes,
        content = content
    )
}