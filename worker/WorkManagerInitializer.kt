package com.notifiq.worker

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notifiq.core.common.Constants
import java.util.concurrent.TimeUnit

class WorkManagerInitializer : Initializer<WorkManager>, Configuration.Provider {

    override fun create(context: Context): WorkManager {
        val workManager = WorkManager.getInstance(context)

        // Schedule daily summary worker
        val dailySummaryRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.DAILY_SUMMARY_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            dailySummaryRequest
        )

        // Schedule analytics aggregation worker
        val analyticsRequest = PeriodicWorkRequestBuilder<AnalyticsAggregationWorker>(
            1, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.ANALYTICS_AGGREGATION_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            analyticsRequest
        )

        // Schedule data cleanup worker (weekly)
        val cleanupRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(
            7, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.DATA_CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )

        // Schedule learning update worker (daily)
        val learningRequest = PeriodicWorkRequestBuilder<LearningUpdateWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.LEARNING_UPDATE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            learningRequest
        )

        // Schedule health check worker (daily)
        val healthCheckRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Constants.HEALTH_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            healthCheckRequest
        )

        return workManager
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}