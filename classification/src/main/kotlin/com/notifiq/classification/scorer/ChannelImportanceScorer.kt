package com.notifiq.classification.scorer

import android.app.NotificationManager
import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import javax.inject.Inject

class ChannelImportanceScorer @Inject constructor() : IScorer {

    override suspend fun score(context: ScoringContext): ScoringResult {
        return when {
            context.importance >= NotificationManager.IMPORTANCE_HIGH -> {
                ScoringResult(
                    delta = 0.15,
                    reasons = listOf("High importance channel")
                )
            }
            // MIN must be checked before LOW: IMPORTANCE_MIN (1) <= IMPORTANCE_LOW (2),
            // so if LOW were checked first the MIN branch would be unreachable.
            context.importance <= NotificationManager.IMPORTANCE_MIN -> {
                ScoringResult(
                    delta = -0.20,
                    reasons = listOf("Minimum importance channel")
                )
            }
            context.importance <= NotificationManager.IMPORTANCE_LOW -> {
                ScoringResult(
                    delta = -0.10,
                    reasons = listOf("Low importance channel")
                )
            }
            else -> ScoringResult(delta = 0.0, reasons = emptyList())
        }
    }
}
