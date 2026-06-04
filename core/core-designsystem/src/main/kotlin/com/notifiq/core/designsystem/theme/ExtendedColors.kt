package com.notifiq.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.notifiq.core.model.ClassificationLabel

/**
 * Tokens that Material 3's color roles don't cover: the six classification-label
 * hues, success/warning, the background atmosphere gradient, the editorial hairline,
 * and a soft accent wash. Provided by [NotifIQTheme] and read via [AppTheme.ext].
 */
@Immutable
data class ExtendedColors(
    val labelImportant: Color,
    val labelUseful: Color,
    val labelNormal: Color,
    val labelLowValue: Color,
    val labelSpam: Color,
    val labelUnknown: Color,
    val success: Color,
    val warning: Color,
    val gradientTop: Color,
    val gradientBottom: Color,
    val hairline: Color,
    val accentSoft: Color,
)

val DarkExtended = ExtendedColors(
    labelImportant = DarkLabelImportant,
    labelUseful = DarkLabelUseful,
    labelNormal = DarkLabelNormal,
    labelLowValue = DarkLabelLowValue,
    labelSpam = DarkLabelSpam,
    labelUnknown = DarkLabelUnknown,
    success = DarkLabelUseful,
    warning = DarkLabelLowValue,
    gradientTop = Color(0xFF1A1610),
    gradientBottom = Color(0xFF100D0A),
    hairline = Color(0xFF2A241B),
    accentSoft = DarkPrimary.copy(alpha = 0.16f),
)

val LightExtended = ExtendedColors(
    labelImportant = LightLabelImportant,
    labelUseful = LightLabelUseful,
    labelNormal = LightLabelNormal,
    labelLowValue = LightLabelLowValue,
    labelSpam = LightLabelSpam,
    labelUnknown = LightLabelUnknown,
    success = LightLabelUseful,
    warning = LightLabelLowValue,
    gradientTop = Color(0xFFF8F2E8),
    gradientBottom = Color(0xFFEFE6D6),
    hairline = Color(0xFFE6DCCB),
    accentSoft = LightPrimary.copy(alpha = 0.12f),
)

val LocalExtendedColors = staticCompositionLocalOf {
    // Fallback only — NotifIQTheme always provides the real instance.
    LightExtended
}

/** Mirrors `MaterialTheme.colorScheme` for our extended tokens: `AppTheme.ext.success`. */
object AppTheme {
    val ext: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

fun ClassificationLabel.color(ext: ExtendedColors): Color = when (this) {
    ClassificationLabel.IMPORTANT -> ext.labelImportant
    ClassificationLabel.USEFUL -> ext.labelUseful
    ClassificationLabel.NORMAL -> ext.labelNormal
    ClassificationLabel.LOW_VALUE -> ext.labelLowValue
    ClassificationLabel.SPAM -> ext.labelSpam
    ClassificationLabel.UNKNOWN -> ext.labelUnknown
}

/** Theme-aware label color. Replaces the old `color(isDark: Boolean)` that hardcoded `false`. */
@Composable
@ReadOnlyComposable
fun ClassificationLabel.color(): Color = color(LocalExtendedColors.current)

/** Same hue at low alpha, for chip/dot backgrounds. */
@Composable
@ReadOnlyComposable
fun ClassificationLabel.mutedColor(alpha: Float = 0.14f): Color =
    color(LocalExtendedColors.current).copy(alpha = alpha)
