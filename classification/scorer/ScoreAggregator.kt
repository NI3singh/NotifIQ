package com.notifiq.classification.scorer

import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreAggregator @Inject constructor() {
    fun aggregate(scores: List<ScoringResult>): Double {
        if (scores.isEmpty()) return Constants.BASE_SCORE

        var weightedSum = 0.0
        var totalWeight = 0.0

        scores.forEachIndexed { index, score ->
            val weight = when (index) {
                0 -> Constants.USER_FEEDBACK_SCORER_WEIGHT
                1 -> Constants.KEYWORD_SCORER_WEIGHT
                2 -> Constants.APP_REPUTATION_SCORER_WEIGHT
                3 -> Constants.CHANNEL_IMPORTANCE_SCORER_WEIGHT
                4 -> Constants.FREQUENCY_SCORER_WEIGHT
                5 -> Constants.TIME_CONTEXT_SCORER_WEIGHT
                else -> 0.1
            }
            weightedSum += score.score * weight
            totalWeight += weight
        }

        return if (totalWeight > 0) weightedSum / totalWeight else Constants.BASE_SCORE
    }
}