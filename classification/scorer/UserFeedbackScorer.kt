package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.model.FeedbackType
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.entity.FeedbackEntity
import com.notifiq.core.model.ClassificationLabel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFeedbackScorer @Inject constructor(
    private val feedbackDao: FeedbackDao,
    private val notificationDao: NotificationDao
) : IScorer {
    override val priority: Int = 10 // Highest priority - user feedback overrides

    override suspend fun score(context: ScoringContext): ScoringResult {
        // This would check if user has previously marked similar notifications
        // For now, return neutral as we need actual feedback data
        return ScoringResult.neutral()
    }

    suspend fun recordFeedback(notificationId: Long, feedbackType: FeedbackType) {
        feedbackDao.insert(
            FeedbackEntity(
                notificationId = notificationId,
                feedbackType = feedbackType.name,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}