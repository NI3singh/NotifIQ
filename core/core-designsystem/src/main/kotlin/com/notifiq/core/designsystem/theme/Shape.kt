package com.notifiq.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Shape as a language, not one radius everywhere: crisp small chips, gently
 * rounded cards, and soft hero containers. The editorial direction leans on
 * hairline borders over heavy elevation.
 */
val NotifIQShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Spacing scale — prefer these over scattering raw .dp values. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val base = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object CornerRadius {
    val sm = 8.dp
    val md = 14.dp
    val lg = 22.dp
    val xl = 28.dp
}
