package com.notifiq.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.util.Calendar

object WorkManagerInitializer {

    private const val DAILY_SUMMARY_WORK = "daily_summary"
    private const val ANALYTICS_AGGREGATION_WORK = "analytics_aggregation"
    private const val DATA_CLEANUP_WORK = "data_cleanup"
    private const val LEARNING_UPDATE_WORK = "learning_update"
    private const val HEALTH_CHECK_WORK = "health_check"

    fun initialize(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // DailySummaryWorker - runs daily at 9 PM
        workManager.enqueueUniquePeriodicWork(
            DAILY_SUMMARY_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DailySummaryWorker>(Duration.ofDays(1))
                .setInitialDelay(Duration.ofMillis(calculateDelayToHour(21))) // 9 PM
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )

        // AnalyticsAggregationWorker - runs daily at 2 AM
        workManager.enqueueUniquePeriodicWork(
            ANALYTICS_AGGREGATION_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AnalyticsAggregationWorker>(Duration.ofDays(1))
                .setInitialDelay(Duration.ofMillis(calculateDelayToHour(2))) // 2 AM
                .build()
        )

        // DataCleanupWorker - runs every 7 days
        workManager.enqueueUniquePeriodicWork(
            DATA_CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DataCleanupWorker>(Duration.ofDays(7))
                .setInitialDelay(Duration.ofDays(1))
                .build()
        )

        // LearningUpdateWorker - runs every 6 hours
        workManager.enqueueUniquePeriodicWork(
            LEARNING_UPDATE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<LearningUpdateWorker>(Duration.ofHours(6))
                .setInitialDelay(Duration.ofMinutes(30))
                .build()
        )

        // HealthCheckWorker - runs every 4 hours
        workManager.enqueueUniquePeriodicWork(
            HEALTH_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HealthCheckWorker>(Duration.ofHours(4))
                .setInitialDelay(Duration.ofMinutes(15))
                .build()
        )
    }

    /**
     * Calculate the delay from now until the next occurrence of the target hour.
     * If the hour has already passed today, it calculates for tomorrow.
     */
    private fun calculateDelayToHour(targetHour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If target time is in the past, add one day
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}