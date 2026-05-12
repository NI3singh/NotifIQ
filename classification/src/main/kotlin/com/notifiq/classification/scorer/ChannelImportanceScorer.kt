package com.notifiq.classification.scorer

import android.app.NotificationManager
import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import javax.inject.Inject

class ChannelImportanceScorer @Inject constructor() : IScorer {

    override fun score(context: ScoringContext): ScoringResult {
        return when {
            context.importance >= NotificationManager.IMPORTANCE_HIGH -> {
                ScoringResult(
                    delta = 0.15,
                    reasons = listOf("High importance channel")
                )
            }
            context.importance <= NotificationManager.IMPORTANCE_LOW -> {
                ScoringResult(
                    delta = -0.10,
                    reasons = listOf("Low importance channel")
                )
            }
            context.importance == NotificationManager.IMPORTANCE_MIN -> {
                ScoringResult(
                    delta = -0.20,
                    reasons = listOf("Minimum importance channel")
                )
            }
            else -> ScoringResult(delta = 0.0, reasons = emptyList())
        }
    }
}