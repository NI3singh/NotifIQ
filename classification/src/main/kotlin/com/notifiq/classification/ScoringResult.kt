package com.notifiq.classification

import com.notifiq.core.model.ClassificationLabel

data class ScoringResult(
    val delta: Double,
    val reasons: List<String>,
    val isHardOverride: Boolean = false,
    val overrideLabel: ClassificationLabel? = null
)