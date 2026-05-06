package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.common.Constants
import com.notifiq.core.model.ClassificationLabel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppReputationScorer @Inject constructor() : IScorer {
    override val priority: Int = 30

    override suspend fun score(context: ScoringContext): ScoringResult {
        // Protected apps (banking, phone, etc.) - always important
        if (context.packageName in Constants.PROTECTED_APP_PACKAGES) {
            return ScoringResult(
                score = 0.95,
                reason = "Protected app package",
                matchedRule = "PROTECTED_APP"
            )
        }

        // Messaging apps - boost useful
        if (context.packageName in Constants.MESSAGING_APP_PACKAGES) {
            return ScoringResult(
                score = 0.70,
                reason = "Messaging app",
                matchedRule = "MESSAGING_APP"
            )
        }

        // Noisy/promotional apps - penalize heavily
        if (context.packageName in Constants.NOISY_APP_PACKAGES) {
            return ScoringResult(
                score = 0.15,
                reason = "Noisy/promotional app",
                matchedRule = "NOISY_APP"
            )
        }

        // Social apps - slightly below normal
        if (context.packageName in Constants.SOCIAL_APP_PACKAGES) {
            return ScoringResult(
                score = 0.35,
                reason = "Social app",
                matchedRule = "SOCIAL_APP"
            )
        }

        // Developer tools - boost useful
        if (context.packageName in Constants.DEVELOPER_APP_PACKAGES) {
            return ScoringResult(
                score = 0.72,
                reason = "Developer tool",
                matchedRule = "DEVELOPER_APP"
            )
        }

        return ScoringResult.neutral()
    }
}