package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.DateTimeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeContextScorer @Inject constructor() : IScorer {
    override val priority: Int = 60

    override suspend fun score(context: ScoringContext): ScoringResult {
        val hour = DateTimeUtils.getHourOfDay()

        // Business hours (9 AM - 6 PM) - notifications more expected
        if (hour in 9..18) {
            return ScoringResult.neutral()
        }

        // Night hours (10 PM - 7 AM) - lower value unless urgent
        if (hour >= 22 || hour <= 7) {
            // Check if this might be urgent based on other factors
            return ScoringResult(
                score = 0.45,
                reason = "Night hours - notifications less expected",
                matchedRule = "NIGHT_HOURS"
            )
        }

        // Early morning / late evening
        return ScoringResult.neutral()
    }
}