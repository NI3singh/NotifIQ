package com.notifiq.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SummaryReport(
    val id: String,
    val type: SummaryType,
    val title: String,
    val totalCount: Int,
    val importantCount: Int,
    val usefulCount: Int,
    val spamCount: Int,
    val suppressedCount: Int,
    val noiseReductionPercent: Float,
    val createdAt: Long
)