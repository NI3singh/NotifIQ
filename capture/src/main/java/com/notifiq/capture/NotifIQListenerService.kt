package com.notifiq.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import com.notifiq.classification.ClassificationEngine
import com.notifiq.classification.PolicyEngine
import com.notifiq.classification.ScoringContext
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NotifIQListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationNormalizer: NotificationNormalizer

    @Inject
    lateinit var notificationDeduplicator: NotificationDeduplicator

    @Inject
    lateinit var classificationEngine: ClassificationEngine

    @Inject
    lateinit var policyEngine: PolicyEngine

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var appPreferenceDao: AppPreferenceDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap?) {
        // Self-guard - don't process own notifications
        if (sbn.packageName == applicationContext.packageName) {
            return
        }

        // Skip group summaries - only process individual notifications
        if ((sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        serviceScope.launch {
            try {
                processNotification(sbn)
            } catch (e: Exception) {
                // Log error but don't crash the service
            }
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        // Self-guard
        if (sbn.packageName == applicationContext.packageName) {
            return
        }

        serviceScope.launch {
            try {
                handleNotificationRemoved(sbn, reason)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        // Normalize the notification
        val normalized = notificationNormalizer.normalize(sbn)

        // Check for duplicate
        if (notificationDeduplicator.isDuplicate(normalized.rawPayloadHash)) {
            return
        }

        // Build scoring context
        val scoringContext = ScoringContext(
            packageName = normalized.packageName,
            appName = normalized.appName,
            title = normalized.title,
            text = normalized.text,
            subText = normalized.subText,
            bigText = normalized.bigText,
            channelId = normalized.channelId,
            importance = normalized.importance,
            postTime = normalized.postTime,
            category = normalized.category
        )

        // Classify the notification
        val classification = classificationEngine.classify(scoringContext)

        // Determine policy action
        val action = policyEngine.decide(
            classification = classification,
            packageName = normalized.packageName,
            text = normalized.text,
            bigText = normalized.bigText,
            category = normalized.category,
            importance = normalized.importance,
            postTime = normalized.postTime
        )

        // Determine suppression status
        val isSuppressed = action == NotificationAction.SUPPRESS || action == NotificationAction.INBOX_ONLY

        // Build notification entity
        val entity = NotificationEntity(
            id = normalized.id,
            key = normalized.key,
            packageName = normalized.packageName,
            appName = normalized.appName,
            title = normalized.title,
            text = normalized.text,
            subText = normalized.subText,
            bigText = normalized.bigText,
            postTime = normalized.postTime,
            channelId = normalized.channelId,
            channelName = normalized.channelName,
            groupKey = normalized.groupKey,
            category = normalized.category,
            priority = normalized.priority,
            importance = normalized.importance,
            classificationLabel = classification.label.name,
            classificationScore = classification.confidence,
            classificationReasons = classification.reasons.joinToString("|"),
            action = action.name,
            isRead = false,
            isSuppressed = isSuppressed,
            isArchived = false,
            rawPayloadHash = normalized.rawPayloadHash,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Insert into database
        notificationDao.insert(entity)

        // Update app statistics
        updateAppStats(normalized.packageName, normalized.appName, classification.label)

        // Suppress notification if needed
        if (action == NotificationAction.SUPPRESS || action == NotificationAction.INBOX_ONLY) {
            try {
                cancelNotification(normalized.key)
            } catch (e: SecurityException) {
                // May fail if notification access removed during processing
            }
        }
    }

    private suspend fun updateAppStats(packageName: String, appName: String, label: ClassificationLabel) {
        val existing = appPreferenceDao.getByPackage(packageName)
        val now = System.currentTimeMillis()

        if (existing != null) {
            // Update existing app preference
            when (label) {
                ClassificationLabel.IMPORTANT, ClassificationLabel.USEFUL -> {
                    appPreferenceDao.incrementImportantCount(packageName, now)
                }
                ClassificationLabel.SPAM -> {
                    appPreferenceDao.incrementSpamCount(packageName, now)
                }
                else -> { /* No special increment */ }
            }
            appPreferenceDao.incrementNotificationCount(packageName, now)
        } else {
            // Create new app preference
            val entity = AppPreferenceEntity(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                isAllowlisted = false,
                isBlocklisted = false,
                trustScore = 0.5,
                totalNotifications = 1,
                importantCount = if (label == ClassificationLabel.IMPORTANT || label == ClassificationLabel.USEFUL) 1 else 0,
                spamCount = if (label == ClassificationLabel.SPAM) 1 else 0,
                isMuted = false,
                createdAt = now,
                updatedAt = now
            )
            appPreferenceDao.insert(entity)
        }
    }

    private suspend fun handleNotificationRemoved(sbn: StatusBarNotification, reason: Int) {
        // Log interaction for future learning
        // Reason codes: CLICK, CANCEL, ERROR, PACKAGE_CHANGED, UNAUTHORIZED, CLOUD
        when (reason) {
            android.service.notification.NotificationListenerService.REASON_CLICK,
            android.service.notification.NotificationListenerService.REASON_CANCEL -> {
                // User dismissed or tapped - could use for learning
            }
            else -> { /* Other reasons - ignore */ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}