package com.notifiq.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifiq.core.common.Constants
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.NotifIQDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AnalyticsAggregationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: NotifIQDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationDao = database.notificationDao()
            val analyticsDao = database.analyticsDao()

            val today = DateTimeUtils.getStartOfDay()

            // Get today's notifications
            val notifications = mutableListOf<com.notifiq.core.database.entity.NotificationEntity>()
            notificationDao.observeByTimeRange(
                DateTimeUtils.getStartOfDay(),
                DateTimeUtils.getEndOfDay()
            ).collect { notifications.addAll(it) }

            // Count by classification
            val importantCount = notifications.count { it.classificationLabel == "IMPORTANT" }
            val usefulCount = notifications.count { it.classificationLabel == "USEFUL" }
            val normalCount = notifications.count { it.classificationLabel == "NORMAL" }
            val lowValueCount = notifications.count { it.classificationLabel == "LOW_VALUE" }
            val spamCount = notifications.count { it.classificationLabel == "SPAM" }
            val suppressedCount = notifications.count { it.isSuppressed }

            // Update or create analytics entry
            val existing = analyticsDao.getByDate(today)
            if (existing != null) {
                analyticsDao.update(
                    existing.copy(
                        totalNotifications = notifications.size,
                        importantCount = importantCount,
                        usefulCount = usefulCount,
                        normalCount = normalCount,
                        lowValueCount = lowValueCount,
                        spamCount = spamCount,
                        suppressedCount = suppressedCount
                    )
                )
            } else {
                analyticsDao.insert(
                    com.notifiq.core.database.entity.AnalyticsDailyEntity(
                        date = today,
                        totalNotifications = notifications.size,
                        importantCount = importantCount,
                        usefulCount = usefulCount,
                        normalCount = normalCount,
                        lowValueCount = lowValueCount,
                        spamCount = spamCount,
                        suppressedCount = suppressedCount
                    )
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}