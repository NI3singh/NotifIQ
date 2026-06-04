package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import java.util.Calendar
import javax.inject.Inject

class TimeContextScorer @Inject constructor() : IScorer {

    override suspend fun score(context: ScoringContext): ScoringResult {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = context.postTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Late night notifications (midnight to 6 AM)
        if (hour in 0..5) {
            return ScoringResult(
                delta = -0.05,
                reasons = listOf("Late night notification")
            )
        }

        return ScoringResult(delta = 0.0, reasons = emptyList())
    }
}