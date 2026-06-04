@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.notifiq.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.notifiq.core.designsystem.R

/*
 * Fraunces (serif display) carries the editorial personality; Inter (clean text)
 * does the reading work. Both are bundled as VARIABLE fonts — each weight is a
 * `wght`-axis instance via FontVariation (honored on API 26+, and minSdk is 26).
 */

private fun fraunces(weight: FontWeight, axis: Int) = Font(
    resId = R.font.fraunces_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

private fun inter(weight: FontWeight, axis: Int) = Font(
    resId = R.font.inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axis)),
)

val Fraunces = FontFamily(
    fraunces(FontWeight.Normal, 400),
    fraunces(FontWeight.Medium, 500),
    fraunces(FontWeight.SemiBold, 600),
    fraunces(FontWeight.Bold, 700),
)

val Inter = FontFamily(
    inter(FontWeight.Normal, 400),
    inter(FontWeight.Medium, 500),
    inter(FontWeight.SemiBold, 600),
    inter(FontWeight.Bold, 700),
)

val NotifIQTypography = Typography(
    // Display + headlines: Fraunces, with dramatic size jumps and tight tracking.
    displayLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Fraunces, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    // Titles / body / labels: Inter.
    titleLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp,
    ),
)

/* ---- Extra styles (outside the M3 scale) ---- */

/** Oversized serif figure for the screen hero. Tabular so digits don't jitter while animating. */
val HeroNumberStyle = TextStyle(
    fontFamily = Fraunces, fontWeight = FontWeight.SemiBold,
    fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-1).sp,
    fontFeatureSettings = "tnum",
)

/** Secondary dashboard figures — Inter, tabular, aligned in columns. */
val StatNumberStyle = TextStyle(
    fontFamily = Inter, fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp, lineHeight = 30.sp,
    fontFeatureSettings = "tnum",
)

/** Uppercase eyebrow / section kicker. Apply `.uppercase()` to the text at the call site. */
val EyebrowStyle = TextStyle(
    fontFamily = Inter, fontWeight = FontWeight.Medium,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.6.sp,
)
