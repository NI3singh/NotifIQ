package com.notifiq.classification

import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.ClassificationResult
import com.notifiq.core.model.NotificationAction
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolicyEngine @Inject constructor(
    private val preferenceDataStore: UserPreferenceDataStore,
    private val safetyGuard: SafetyGuard
) {

    suspend fun decide(
        classification: ClassificationResult,
        packageName: String,
        text: String,
        bigText: String,
        category: String?,
        importance: Int,
        postTime: Long
    ): NotificationAction {
        // Safety check - never suppress protected content
        if (safetyGuard.isSafetyProtected(packageName, text, bigText, category, importance)) {
            return NotificationAction.SHOW_AND_INBOX
        }

        // Get user preferences
        val prefs = preferenceDataStore.userPreference.first()

        // If suppression is disabled, show all
        if (!prefs.suppressionEnabled) {
            return NotificationAction.SHOW_AND_INBOX
        }

        // Check quiet hours
        if (prefs.quietHoursEnabled && classification.label != ClassificationLabel.IMPORTANT) {
            if (DateTimeUtils.isQuietHours(prefs.quietHoursStart, prefs.quietHoursEnd)) {
                return NotificationAction.INBOX_ONLY
            }
        }

        // Check focus mode
        if (prefs.focusModeEnabled) {
            if (classification.label in listOf(
                    ClassificationLabel.NORMAL,
                    ClassificationLabel.LOW_VALUE,
                    ClassificationLabel.SPAM
                )
            ) {
                return NotificationAction.INBOX_ONLY
            }
        }

        // Return the recommended action from classification
        return classification.recommendedAction
    }
}