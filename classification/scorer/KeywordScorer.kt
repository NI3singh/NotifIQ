package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.Constants
import com.notifiq.core.model.ClassificationLabel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordScorer @Inject constructor() : IScorer {
    override val priority: Int = 20

    override suspend fun score(context: ScoringContext): ScoringResult {
        val combinedText = "${context.title} ${context.text}".lowercase()

        // Check for protected keywords (boost important)
        Constants.PROTECTED_KEYWORDS.forEach { keyword ->
            if (combinedText.contains(keyword)) {
                return ScoringResult(
                    score = 1.0,
                    isHardOverride = true,
                    reason = "Protected keyword detected: $keyword",
                    matchedRule = "KEYWORD_BOOST"
                )
            }
        }

        // Check for promotional keywords (penalize)
        Constants.PROMOTIONAL_KEYWORDS.forEach { keyword ->
            if (combinedText.contains(keyword)) {
                return ScoringResult(
                    score = 0.15,
                    reason = "Promotional keyword detected: $keyword",
                    matchedRule = "KEYWORD_PENALIZE"
                )
            }
        }

        // Neutral score if no keywords matched
        return ScoringResult.neutral()
    }
}