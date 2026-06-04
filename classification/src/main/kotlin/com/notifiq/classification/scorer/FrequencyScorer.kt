package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.database.dao.NotificationDao
import javax.inject.Inject

class FrequencyScorer @Inject constructor(
    private val notificationDao: NotificationDao
) : IScorer {

    override suspend fun score(context: ScoringContext): ScoringResult {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000L)
        val oneDayAgo = now - (24 * 60 * 60 * 1000L)

        val hourlyCount = notificationDao.getCountByPackageSince(context.packageName, oneHourAgo)
        val dailyCount = notificationDao.getCountByPackageSince(context.packageName, oneDayAgo)

        return when {
            hourlyCount > 10 -> ScoringResult(
                delta = -0.20,
                reasons = listOf("High frequency: $hourlyCount notifications in last hour")
            )
            dailyCount > 50 -> ScoringResult(
                delta = -0.30,
                reasons = listOf("Very high daily volume: $dailyCount notifications")
            )
            else -> ScoringResult(delta = 0.0, reasons = emptyList())
        }
    }
}
