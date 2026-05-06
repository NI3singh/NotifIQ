package com.notifiq.classification

import com.notifiq.core.model.ClassificationLabel

interface IScorer {
    suspend fun score(context: ScoringContext): ScoringResult
    val priority: Int
}

data class ScoringContext(
    val packageName: String,
    val appName: String,
    val sender: String?,
    val title: String,
    val text: String,
    val channelName: String?,
    val channelImportance: Int,
    val importance: Int,
    val category: String?
)

data class ScoringResult(
    val score: Double,
    val label: ClassificationLabel? = null,
    val isHardOverride: Boolean = false,
    val reason: String? = null,
    val matchedRule: String? = null
) {
    companion object {
        fun neutral() = ScoringResult(score = 0.5)
        fun fromScore(score: Double): ScoringResult {
            val label = ClassificationLabel.fromScore(score)
            return ScoringResult(score = score, label = label)
        }
    }
}