package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelImportanceScorer @Inject constructor() : IScorer {
    override val priority: Int = 40

    override suspend fun score(context: ScoringContext): ScoringResult {
        val importance = context.channelImportance

        return when {
            importance >= 5 -> {
                // High importance channel
                ScoringResult(
                    score = 0.75,
                    reason = "High importance channel",
                    matchedRule = "HIGH_CHANNEL"
                )
            }
            importance >= 3 -> {
                // Default importance channel
                ScoringResult.neutral()
            }
            else -> {
                // Low importance channel
                ScoringResult(
                    score = 0.35,
                    reason = "Low importance channel",
                    matchedRule = "LOW_CHANNEL"
                )
            }
        }
    }
}