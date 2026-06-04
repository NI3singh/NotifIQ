package com.notifiq.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/*
 * "Calm Editorial" palette — warm paper surfaces, one quiet terracotta accent.
 * Surfaces are deliberately stepped in luminance so raised elements read as raised
 * (the old palette sat everything within a few percent and looked flat).
 */

// ---- Dark ("paper-dark") ----
val DarkBackground = Color(0xFF14110D)
val DarkSurface = Color(0xFF1C1813)
val DarkSurfaceVariant = Color(0xFF2A241C)
val DarkSurfaceContainerLowest = Color(0xFF100D09)
val DarkSurfaceContainerLow = Color(0xFF1A1610)
val DarkSurfaceContainer = Color(0xFF241F18)
val DarkSurfaceContainerHigh = Color(0xFF2C261D)
val DarkSurfaceContainerHighest = Color(0xFF342D22)
val DarkOnSurface = Color(0xFFECE4D6)        // warm ivory
val DarkOnSurfaceVariant = Color(0xFFA89C88) // muted secondary text
val DarkOutline = Color(0xFF4A4234)
val DarkOutlineVariant = Color(0xFF2E2820)

val DarkPrimary = Color(0xFFD2855A)          // terracotta — the ONE accent
val DarkOnPrimary = Color(0xFF2A1709)
val DarkPrimaryContainer = Color(0xFF3A2415)
val DarkOnPrimaryContainer = Color(0xFFF2C9AD)
val DarkSecondary = Color(0xFF9DA98C)        // quiet sage
val DarkOnSecondary = Color(0xFF1E2316)
val DarkSecondaryContainer = Color(0xFF2C3322)
val DarkOnSecondaryContainer = Color(0xFFD6E0C6)

val DarkError = Color(0xFFE08C7A)
val DarkOnError = Color(0xFF2A0E08)
val DarkErrorContainer = Color(0xFF4A1F16)
val DarkOnErrorContainer = Color(0xFFF6C9BF)
val DarkInverseSurface = Color(0xFFECE4D6)
val DarkInverseOnSurface = Color(0xFF2A241C)

// ---- Light ("paper") ----
val LightBackground = Color(0xFFF5EEE2)
val LightSurface = Color(0xFFFFFDF8)
val LightSurfaceVariant = Color(0xFFEBE2D2)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF8F1E6)
val LightSurfaceContainer = Color(0xFFF0E8D9)
val LightSurfaceContainerHigh = Color(0xFFE9E0CE)
val LightSurfaceContainerHighest = Color(0xFFE2D8C4)
val LightOnSurface = Color(0xFF241F18)       // warm near-black ink
val LightOnSurfaceVariant = Color(0xFF6E6353)
val LightOutline = Color(0xFFC8BCA6)
val LightOutlineVariant = Color(0xFFE2D8C6)

val LightPrimary = Color(0xFFB5673C)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFF7DAC6)
val LightOnPrimaryContainer = Color(0xFF4A2410)
val LightSecondary = Color(0xFF5F6B4C)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDEE7CD)
val LightOnSecondaryContainer = Color(0xFF2A3318)

val LightError = Color(0xFFB3402E)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF8D7CF)
val LightOnErrorContainer = Color(0xFF4A140C)
val LightInverseSurface = Color(0xFF2C261D)
val LightInverseOnSurface = Color(0xFFF5EEE2)

// ---- Classification label colors (warm-tuned, per mode) ----
// Distinct hues that still sit inside the warm palette. Routed through
// ExtendedColors so components read them theme-aware (no hardcoded isDark).
val DarkLabelImportant = Color(0xFFE5A45C)
val DarkLabelUseful = Color(0xFF93B27B)
val DarkLabelNormal = Color(0xFF8E96CC)
val DarkLabelLowValue = Color(0xFFD9B85F)
val DarkLabelSpam = Color(0xFFCD8088)
val DarkLabelUnknown = Color(0xFF9E927E)

val LightLabelImportant = Color(0xFFB5712A)
val LightLabelUseful = Color(0xFF4E7A43)
val LightLabelNormal = Color(0xFF51589C)
val LightLabelLowValue = Color(0xFF8A6A1C)
val LightLabelSpam = Color(0xFFA84E5A)
val LightLabelUnknown = Color(0xFF6E6353)
