package com.notifiq.classification

import com.notifiq.classification.scorer.ScoreAggregator
import com.notifiq.core.model.ClassificationResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationEngine @Inject constructor(
    private val scoreAggregator: ScoreAggregator
) {

    suspend fun classify(context: ScoringContext): ClassificationResult {
        return scoreAggregator.classify(context)
    }
}