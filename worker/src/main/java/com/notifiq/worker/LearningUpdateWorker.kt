package com.notifiq.worker

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.model.FeedbackType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LearningUpdateWorker @AssistedInject constructor(
    @Assisted private val context: android.content.Context,
    @Assisted workerParams: WorkerParameters,
    private val feedbackDao: FeedbackDao,
    private val appPreferenceDao: AppPreferenceDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get feedback from the last 7 days
            val sevenDaysAgoMillis = DateTimeUtils.daysAgoMillis(7)
            val allFeedback = feedbackDao.getRecentFeedback(sevenDaysAgoMillis)

            // Group feedback by packageName
            val feedbackByPackage = allFeedback.groupBy { it.packageName }

            // For each package with at least 3 feedback entries, update trust score
            for ((packageName, feedbackList) in feedbackByPackage) {
                if (feedbackList.size < 3) continue

                // Count positive and negative feedback
                var positiveCount = 0
                var negativeCount = 0

                for (feedback in feedbackList) {
                    val feedbackType = try {
                        FeedbackType.valueOf(feedback.feedbackType)
                    } catch (e: Exception) {
                        continue
                    }

                    when (feedbackType) {
                        FeedbackType.MARK_IMPORTANT,
                        FeedbackType.MARK_USEFUL,
                        FeedbackType.WHITELIST_APP -> positiveCount++

                        FeedbackType.MARK_SPAM,
                        FeedbackType.BLOCKLIST_APP,
                        FeedbackType.MUTE_SIMILAR,
                        FeedbackType.ARCHIVE,
                        FeedbackType.DISMISS,
                        FeedbackType.UNDO -> negativeCount++

                        else -> { /* Unknown feedback type, skip */ }
                    }
                }

                val totalCount = positiveCount + negativeCount
                if (totalCount == 0) continue

                // Compute ratio of positive feedback
                val ratio = positiveCount.toDouble() / totalCount

                // Get current trust score
                val existingApp = appPreferenceDao.getByPackage(packageName)
                val existingTrust = existingApp?.trustScore ?: 0.5

                // Calculate new trust score: weighted average favoring recent behavior
                val newTrust = ratio * 0.7 + existingTrust * 0.3

                // Update the trust score
                appPreferenceDao.updateTrustScore(
                    packageName = packageName,
                    trustScore = newTrust,
                    updatedAt = System.currentTimeMillis()
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}