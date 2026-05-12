package com.notifiq.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.notifiq.core.model.ClassificationLabel

// Dark Mode Palette
val DarkBackground = Color(0xFF09090D)
val DarkSurface = Color(0xFF12121A)
val DarkSurface2 = Color(0xFF1C1C28)
val DarkSurface3 = Color(0xFF262635)
val DarkTextPrimary = Color(0xFFEEEEF4)
val DarkTextSecondary = Color(0xFF9494A8)
val DarkAccent = Color(0xFF7DA2FF)
val DarkAccentContainer = Color(0xFF243B67)
val DarkTabBar = Color(0xFF0D0D14)

// Light Mode Palette
val LightBackground = Color(0xFFF5F5FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFEDEDF5)
val LightSurface3 = Color(0xFFDDDDE8)
val LightTextPrimary = Color(0xFF0F0F1A)
val LightTextSecondary = Color(0xFF5A5A70)
val LightAccent = Color(0xFF4B7EF5)
val LightAccentContainer = Color(0xFFDDE6FF)
val LightTabBar = Color(0xFFFFFFFF)

// Classification Label Colors - Dark Mode
val DarkImportant = Color(0xFFFF8A4C)
val DarkUseful = Color(0xFF34D399)
val DarkNormal = Color(0xFFA78BFA)
val DarkLowValue = Color(0xFFFACC15)
val DarkSpam = Color(0xFFF87171)
val DarkUnknown = Color(0xFF6B7280)

// Classification Label Colors - Light Mode
val LightImportant = Color(0xFFE86400)
val LightUseful = Color(0xFF059669)
val LightNormal = Color(0xFF7C3AED)
val LightLowValue = Color(0xFFB8960A)
val LightSpam = Color(0xFFDC2626)
val LightUnknown = Color(0xFF4B5563)

fun ClassificationLabel.color(isDark: Boolean): Color {
    return when (this) {
        ClassificationLabel.IMPORTANT -> if (isDark) DarkImportant else LightImportant
        ClassificationLabel.USEFUL -> if (isDark) DarkUseful else LightUseful
        ClassificationLabel.NORMAL -> if (isDark) DarkNormal else LightNormal
        ClassificationLabel.LOW_VALUE -> if (isDark) DarkLowValue else LightLowValue
        ClassificationLabel.SPAM -> if (isDark) DarkSpam else LightSpam
        ClassificationLabel.UNKNOWN -> if (isDark) DarkUnknown else LightUnknown
    }
}

fun ClassificationLabel.mutedColor(isDark: Boolean): Color {
    return color(isDark).copy(alpha = 0.12f)
}