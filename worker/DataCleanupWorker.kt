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
class DataCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: NotifIQDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val notificationDao = database.notificationDao()
            val feedbackDao = database.feedbackDao()

            // Delete notifications older than retention period
            val cutoffTime = DateTimeUtils.getDaysAgo(Constants.DEFAULT_RETENTION_DAYS)
            notificationDao.deleteOlderThan(cutoffTime)
            feedbackDao.deleteOlderThan(cutoffTime)

            // Delete old analytics
            val analyticsDao = database.analyticsDao()
            val oldAnalyticsCutoff = DateTimeUtils.getDaysAgo(90) // Keep 90 days of analytics
            analyticsDao.deleteOlderThan(oldAnalyticsCutoff)

            // Delete old summaries
            val summaryDao = database.summaryDao()
            val oldSummaryCutoff = DateTimeUtils.getDaysAgo(30)
            summaryDao.deleteOlderThan(oldSummaryCutoff)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}