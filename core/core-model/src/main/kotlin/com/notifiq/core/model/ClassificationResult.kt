package com.notifiq.core.model

data class ClassificationResult(
    val label: ClassificationLabel,
    val confidence: Float,
    val reasons: List<String>,
    val recommendedAction: NotificationAction,
    val sourceRuleId: String,
    val isAmbiguous: Boolean
)