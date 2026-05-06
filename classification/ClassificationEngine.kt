package com.notifiq.classification

import com.notifiq.core.common.Constants
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.model.ClassificationLabel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationEngine @Inject constructor(
    private val scorers: List<IScorer>,
    private val ruleDao: RuleDao
) {
    private val safetyGuard = SafetyGuard()

    suspend fun classify(context: ScoringContext): ClassificationResult {
        var baseScore = Constants.BASE_SCORE
        val reasons = mutableListOf<String>()
        val matchedRules = mutableListOf<String>()

        // Run scorers in priority order
        val sortedScorers = scorers.sortedBy { it.priority }

        for (scorer in sortedScorers) {
            val result = scorer.score(context)

            if (result.isHardOverride) {
                return ClassificationResult(
                    label = result.label ?: ClassificationLabel.fromScore(result.score),
                    score = result.score,
                    action = com.notifiq.core.model.NotificationAction.SHOW,
                    reasons = listOfNotNull(result.reason),
                    matchedRules = listOfNotNull(result.matchedRule)
                )
            }

            baseScore += result.score - Constants.BASE_SCORE
            result.reason?.let { reasons.add(it) }
            result.matchedRule?.let { matchedRules.add(it) }
        }

        // Clamp score between 0 and 1
        val finalScore = baseScore.coerceIn(0.0, 1.0)
        val label = ClassificationLabel.fromScore(finalScore)

        return ClassificationResult(
            label = label,
            score = finalScore,
            action = com.notifiq.core.model.NotificationAction.SHOW,
            reasons = reasons,
            matchedRules = matchedRules
        )
    }

    fun decide(
        classificationResult: ClassificationResult,
        context: ScoringContext,
        suppressionEnabled: Boolean,
        isQuietHours: Boolean
    ): PolicyDecision {
        val policyEngine = PolicyEngine(safetyGuard)
        return policyEngine.decide(classificationResult, context, suppressionEnabled, isQuietHours)
    }
}