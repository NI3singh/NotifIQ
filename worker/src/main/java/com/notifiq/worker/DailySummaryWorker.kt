package com.notifiq.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.database.entity.SummaryEntity
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
class DailySummaryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val summaryDao: SummaryDao
) : CoroutineWorker(context, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        return try {
            val todayStart = DateTimeUtils.todayStartMillis()
            val todayEnd = DateTimeUtils.todayEndMillis()

            val notifications = notificationDao.getNotificationsBetween(todayStart, todayEnd)

            if (notifications.isEmpty()) {
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

            // Calculate noise reduction percent
            val noiseReductionPercent = if (totalCount > 0) {
                ((lowValueCount + spamCount).toFloat() / totalCount) * 100f
            } else 0f

            // Find top 5 apps by total notification count
            val topApps = notifications
                .groupBy { it.packageName }
                .map { (packageName, notifs) ->
                    AppInfo(packageName, notifs.first().appName, notifs.size)
                }
                .sortedByDescending { it.count }
                .take(5)

            // Find top 5 important apps
            val importantApps = notifications
                .filter { it.classificationLabel in listOf(ClassificationLabel.IMPORTANT.name, ClassificationLabel.USEFUL.name) }
                .groupBy { it.packageName }
                .map { (packageName, notifs) ->
                    AppInfo(packageName, notifs.first().appName, notifs.size)
                }
                .sortedByDescending { it.count }
                .take(5)

            val topAppsJson = json.encodeToString(topApps)
            val topImportantAppsJson = json.encodeToString(importantApps)

            // Create title with date
            val dateFormat = SimpleDateFormat("MMM d", Locale.US)
            val title = "Daily Summary — ${dateFormat.format(Date())}"

            val summaryEntity = SummaryEntity(
                id = UUID.randomUUID().toString(),
                type = "DAILY",
                title = title,
                totalCount = totalCount,
                importantCount = importantCount,
                usefulCount = usefulCount,
                spamCount = spamCount,
                suppressedCount = suppressedCount,
                noiseReductionPercent = noiseReductionPercent,
                topNoisyAppsJson = topAppsJson,
                topImportantAppsJson = topImportantAppsJson,
                createdAt = System.currentTimeMillis()
            )

            summaryDao.insert(summaryEntity)

            // Post notification summary
            postSummaryNotification(title, totalCount, importantCount, usefulCount)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun postSummaryNotification(title: String, totalCount: Int, importantCount: Int, usefulCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Calculate useful percentage
        val usefulPercent = if (totalCount > 0) {
            ((importantCount + usefulCount).toFloat() / totalCount * 100).toInt()
        } else 0

        val notification = NotificationCompat.Builder(context, "summaries")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("$totalCount notifications, $usefulPercent% useful")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }

    @Serializable
    private data class AppInfo(
        val packageName: String,
        val appName: String,
        val count: Int
    )
}