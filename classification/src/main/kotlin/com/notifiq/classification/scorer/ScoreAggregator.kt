package com.notifiq.classification.scorer

import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.Constants
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.ClassificationResult
import com.notifiq.core.model.NotificationAction
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ScoreAggregator @Inject constructor(
    private val userFeedbackScorer: UserFeedbackScorer,
    private val keywordScorer: KeywordScorer,
    private val appReputationScorer: AppReputationScorer,
    private val channelImportanceScorer: ChannelImportanceScorer,
    private val frequencyScorer: FrequencyScorer,
    private val timeContextScorer: TimeContextScorer
) {

    suspend fun classify(context: ScoringContext): ClassificationResult {
        var baseScore: Float = Constants.BASE_SCORE
        val reasons = mutableListOf<String>()

        // Scorer evaluation order (highest priority first).
        val scorers = listOf(
            userFeedbackScorer to "UserFeedback",
            keywordScorer to "Keyword",
            appReputationScorer to "AppReputation",
            channelImportanceScorer to "ChannelImportance",
            frequencyScorer to "Frequency",
            timeContextScorer to "TimeContext"
        )

        for ((scorer, _) in scorers) {
            val result: ScoringResult = try {
                scorer.score(context)
            } catch (e: CancellationException) {
                // Never swallow cooperative cancellation - let it propagate.
                throw e
            } catch (e: Exception) {
                // A single misbehaving scorer must not break classification.
                ScoringResult(delta = 0.0, reasons = emptyList())
            }

            // If hard override, return immediately.
            if (result.isHardOverride && result.overrideLabel != null) {
                val confidence = (baseScore + result.delta.toFloat()).coerceIn(0f, 1f)
                return ClassificationResult(
                    label = result.overrideLabel,
                    confidence = confidence,
                    reasons = reasons + result.reasons,
                    recommendedAction = labelToAction(result.overrideLabel),
                    sourceRuleId = "HARD_OVERRIDE",
                    isAmbiguous = false
                )
            }

            // Accumulate delta and reasons.
            baseScore += result.delta.toFloat()
            reasons.addAll(result.reasons)
        }

        // Clamp final score.
        val finalScore = baseScore.coerceIn(0f, 1f)
        val label = ClassificationLabel.fromScore(finalScore.toDouble())

        // Flag scores that sit close to a label boundary.
        val isAmbiguous = (finalScore in 0.35f..0.45f) || (finalScore in 0.60f..0.70f)

        return ClassificationResult(
            label = label,
            confidence = finalScore,
            reasons = reasons,
            recommendedAction = labelToAction(label),
            sourceRuleId = "",
            isAmbiguous = isAmbiguous
        )
    }

    private fun labelToAction(label: ClassificationLabel): NotificationAction {
        return when (label) {
            ClassificationLabel.IMPORTANT -> NotificationAction.SHOW_AND_INBOX
            ClassificationLabel.USEFUL -> NotificationAction.SHOW_AND_INBOX
            ClassificationLabel.NORMAL -> NotificationAction.SHOW_AND_INBOX
            ClassificationLabel.LOW_VALUE -> NotificationAction.INBOX_ONLY
            ClassificationLabel.SPAM -> NotificationAction.SUPPRESS
            ClassificationLabel.UNKNOWN -> NotificationAction.SHOW_AND_INBOX
        }
    }
}
