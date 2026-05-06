package com.notifiq.classification

import com.notifiq.core.common.Constants
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationAction
import com.notifiq.core.model.ClassificationResult

data class PolicyDecision(
    val action: NotificationAction,
    val shouldSuppress: Boolean,
    val reason: String
)

class PolicyEngine(
    private val safetyGuard: SafetyGuard
) {
    fun decide(
        classificationResult: ClassificationResult,
        context: ScoringContext,
        suppressionEnabled: Boolean,
        isQuietHours: Boolean
    ): PolicyDecision {
        // Safety guard must NEVER allow suppression of protected notifications
        if (suppressionEnabled && safetyGuard.canSuppress(context).not()) {
            return PolicyDecision(
                action = NotificationAction.SHOW,
                shouldSuppress = false,
                reason = "Safety guard blocked suppression"
            )
        }

        // Quiet hours logic
        if (isQuietHours && suppressionEnabled) {
            if (classificationResult.label in listOf(
                    ClassificationLabel.LOW_VALUE,
                    ClassificationLabel.SPAM,
                    ClassificationLabel.NORMAL
                )
            ) {
                return PolicyDecision(
                    action = NotificationAction.INBOX_ONLY,
                    shouldSuppress = true,
                    reason = "Quiet hours active"
                )
            }
        }

        // Regular suppression based on label
        if (suppressionEnabled) {
            when (classificationResult.label) {
                ClassificationLabel.SPAM -> {
                    return PolicyDecision(
                        action = NotificationAction.SUPPRESS,
                        shouldSuppress = true,
                        reason = "Spam classification"
                    )
                }
                ClassificationLabel.LOW_VALUE -> {
                    return PolicyDecision(
                        action = NotificationAction.SUPPRESS,
                        shouldSuppress = true,
                        reason = "Low value classification"
                    )
                }
                else -> {
                    return PolicyDecision(
                        action = NotificationAction.SHOW,
                        shouldSuppress = false,
                        reason = "Above threshold"
                    )
                }
            }
        }

        return PolicyDecision(
            action = NotificationAction.SHOW,
            shouldSuppress = false,
            reason = "Suppression disabled"
        )
    }
}