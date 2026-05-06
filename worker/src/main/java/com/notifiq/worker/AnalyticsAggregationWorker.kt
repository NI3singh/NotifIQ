package com.notifiq.worker

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.entity.AnalyticsDailyEntity
import com.notifiq.core.model.ClassificationLabel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@HiltWorker
class AnalyticsAggregationWorker @AssistedInject constructor(
    @Assisted private val context: android.content.Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val analyticsDao: AnalyticsDao
) : CoroutineWorker(context, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        return try {
            // Get today's date as string
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())

            val todayStart = DateTimeUtils.todayStartMillis()
            val todayEnd = DateTimeUtils.todayEndMillis()

            val notifications = notificationDao.getNotificationsBetween(todayStart, todayEnd)

            if (notifications.isEmpty()) {
                // Still create/update the record to show zero notifications
                val zeroEntity = AnalyticsDailyEntity(
                    id = UUID.randomUUID().toString(),
                    date = today,
                    totalNotifications = 0,
                    importantCount = 0,
                    usefulCount = 0,
                    normalCount = 0,
                    lowValueCount = 0,
                    spamCount = 0,
                    suppressedCount = 0,
                    averageConfidence = 0f,
                    topAppsJson = "[]",
                    createdAt = System.currentTimeMillis()
                )
                analyticsDao.insert(zeroEntity)
                return Result.success()
            }

            // Count by classification label
            val importantCount = notifications.count { it.classificationLabel == ClassificationLabel.IMPORTANT.name }
            val usefulCount = notifications.count { it.classificationLabel == ClassificationLabel.USEFUL.name }
            val normalCount = notifications.count { it.classificationLabel == ClassificationLabel.NORMAL.name }
            val lowValueCount = notifications.count { it.classificationLabel == ClassificationLabel.LOW_VALUE.name }
            val spamCount = notifications.count { it.classificationLabel == ClassificationLabel.SPAM.name }
            val suppressedCount = notifications.count { it.isSuppressed }

            val totalCount = notifications.size

            // Compute average confidence
            val avgConfidence = if (notifications.isNotEmpty()) {
                notifications.map { it.classificationScore }.average().toFloat()
            } else 0f

            // Find top 5 apps
            val topApps = notifications
                .groupBy { it.packageName }
                .map { (packageName, notifs) ->
                    AppInfo(packageName, notifs.first().appName, notifs.size)
                }
                .sortedByDescending { it.count }
                .take(5)

            val topAppsJson = json.encodeToString(topApps)

            val entity = AnalyticsDailyEntity(
                id = UUID.randomUUID().toString(),
                date = today,
                totalNotifications = totalCount,
                importantCount = importantCount,
                usefulCount = usefulCount,
                normalCount = normalCount,
                lowValueCount = lowValueCount,
                spamCount = spamCount,
                suppressedCount = suppressedCount,
                averageConfidence = avgConfidence,
                topAppsJson = topAppsJson,
                createdAt = System.currentTimeMillis()
            )

            analyticsDao.insert(entity)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @Serializable
    private data class AppInfo(
        val packageName: String,
        val appName: String,
        val count: Int
    )
}