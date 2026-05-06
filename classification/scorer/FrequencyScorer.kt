package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.common.DateTimeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrequencyScorer @Inject constructor(
    private val notificationDao: NotificationDao
) : IScorer {
    override val priority: Int = 50

    override suspend fun score(context: ScoringContext): ScoringResult {
        val today = DateTimeUtils.getStartOfDay()
        val todayNotifications = notificationDao.countByTimeRange(today, System.currentTimeMillis())

        // Very high frequency (>50 today) - likely spam/promotional
        if (todayNotifications > 50) {
            return ScoringResult(
                score = 0.20,
                reason = "Very high notification frequency",
                matchedRule = "HIGH_FREQUENCY"
            )
        }

        // High frequency (20-50 today) - slightly penalized
        if (todayNotifications > 20) {
            return ScoringResult(
                score = 0.40,
                reason = "High notification frequency",
                matchedRule = "MEDIUM_FREQUENCY"
            )
        }

        return ScoringResult.neutral()
    }
}