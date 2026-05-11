package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.NotificationDao
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class FrequencyScorer @Inject constructor(
    private val notificationDao: NotificationDao
) : IScorer {

    override fun score(context: ScoringContext): ScoringResult {
        return runBlocking {
            val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000L) // ✅ actually 1 hour ago
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // ✅ actually 24 hours ago

            val hourlyCount = notificationDao.getCountByPackageSince(context.packageName, oneHourAgo)
            val dailyCount = notificationDao.getCountByPackageSince(context.packageName, oneDayAgo)

            when {
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
}