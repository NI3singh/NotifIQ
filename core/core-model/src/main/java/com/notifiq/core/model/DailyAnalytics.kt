package com.notifiq.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyAnalytics(
    val date: String,
    val totalNotifications: Int,
    val importantCount: Int,
    val usefulCount: Int,
    val normalCount: Int,
    val lowValueCount: Int,
    val spamCount: Int,
    val suppressedCount: Int,
    val averageConfidence: Float
)