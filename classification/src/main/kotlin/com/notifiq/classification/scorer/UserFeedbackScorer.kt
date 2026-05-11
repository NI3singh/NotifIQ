package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.FeedbackType
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.common.DateTimeUtils
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class UserFeedbackScorer @Inject constructor(
    private val appPreferenceDao: AppPreferenceDao,
    private val feedbackDao: FeedbackDao
) : IScorer {

    override fun score(context: ScoringContext): ScoringResult {
        return runBlocking {
            var totalDelta = 0.0
            val reasons = mutableListOf<String>()

            // Look up app preference
            val appPref = appPreferenceDao.getByPackage(context.packageName)

            if (appPref != null) {
                // Allowlisted - hard override to USEFUL
                if (appPref.isAllowlisted) {
                    return@runBlocking ScoringResult(
                        delta = 0.28,
                        reasons = listOf("You allowlisted ${context.appName}"),
                        isHardOverride = true,
                        overrideLabel = ClassificationLabel.USEFUL
                    )
                }

                // Blocklisted - hard override to SPAM
                if (appPref.isBlocklisted) {
                    return@runBlocking ScoringResult(
                        delta = -0.45,
                        reasons = listOf("You blocklisted ${context.appName}"),
                        isHardOverride = true,
                        overrideLabel = ClassificationLabel.SPAM
                    )
                }

                // Trust score adjustment
                if (appPref.trustScore != 0.5) {
                    val trustDelta = (appPref.trustScore - 0.5) * 0.4
                    totalDelta += trustDelta
                    val percentage = (appPref.trustScore * 100).toInt()
                    reasons.add("Learned trust for ${context.appName}: $percentage%")
                }
            }

            // Look up recent feedback from last 7 days
            val sevenDaysAgo = DateTimeUtils.daysAgoMillis(7)
            val recentFeedback = feedbackDao.getRecentByPackage(context.packageName, sevenDaysAgo)

            if (recentFeedback.size >= 3) {
                val positiveTypes = setOf(
                    FeedbackType.MARK_IMPORTANT.name,
                    FeedbackType.MARK_USEFUL.name,
                    FeedbackType.WHITELIST_APP.name
                )
                val negativeTypes = setOf(
                    FeedbackType.MARK_SPAM.name,
                    FeedbackType.BLOCKLIST_APP.name,
                    FeedbackType.MUTE_SIMILAR.name,
                    FeedbackType.ARCHIVE.name
                )

                val positiveCount = recentFeedback.count { it.feedbackType in positiveTypes }
                val negativeCount = recentFeedback.count { it.feedbackType in negativeTypes }

                val feedbackDelta = ((positiveCount - negativeCount) * 0.03).coerceIn(-0.15, 0.15)
                totalDelta += feedbackDelta

                if (feedbackDelta != 0.0) {
                    val signal = if (feedbackDelta > 0) "positive" else "negative"
                    reasons.add("$positiveCount positive, $negativeCount negative feedback signal ($signal)")
                }
            }

            if (reasons.isEmpty()) {
                ScoringResult(delta = 0.0, reasons = emptyList())
            } else {
                ScoringResult(delta = totalDelta.coerceIn(-0.45, 0.50), reasons = reasons)
            }
        }
    }
}