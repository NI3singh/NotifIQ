package com.notifiq.capture

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notifiq.classification.ClassificationEngine
import com.notifiq.core.common.Constants
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.common.HashUtils
import com.notifiq.core.database.NotifIQDatabase
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.database.entity.SenderEntity
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotifIQListenerService : NotificationListenerService() {

    @Inject
    lateinit var database: NotifIQDatabase

    @Inject
    lateinit var classificationEngine: ClassificationEngine

    @Inject
    lateinit var userPreferenceDataStore: UserPreferenceDataStore

    @Inject
    lateinit var notificationNormalizer: NotificationNormalizer

    @Inject
    lateinit var notificationDeduplicator: NotificationDeduplicator

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        serviceScope.launch {
            try {
                processNotification(sbn)
            } catch (e: Exception) {
                // Log error but don't crash the service
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Could track removed notifications if needed
    }

    private suspend fun processNotification(sbn: StatusBarNotification) {
        // Check if app is blocked
        val appPreferenceDao = database.appPreferenceDao()
        val existingPref = appPreferenceDao.getByPackage(sbn.packageName)
        if (existingPref?.isBlocked == true) {
            return
        }

        // Normalize the notification
        val normalized = notificationNormalizer.normalize(sbn)

        // Check for duplicates
        if (notificationDeduplicator.isDuplicate(normalized)) {
            return
        }

        // Classify the notification
        val scoringContext = com.notifiq.classification.ScoringContext(
            packageName = normalized.packageName,
            appName = normalized.appName,
            sender = normalized.sender,
            title = normalized.title,
            text = normalized.text,
            channelName = normalized.channelName,
            channelImportance = normalized.channelImportance,
            importance = normalized.importance,
            category = normalized.category
        )

        val classificationResult = classificationEngine.classify(scoringContext)

        // Get user preferences for suppression
        val preferences = userPreferenceDataStore.userPreferences.first()
        val isQuietHours = DateTimeUtils.isWithinQuietHours(
            preferences.quietHoursStart,
            preferences.quietHoursEnd
        )

        val policyDecision = classificationEngine.decide(
            classificationResult,
            scoringContext,
            preferences.suppressionEnabled,
            isQuietHours
        )

        // Store notification
        val notificationEntity = NotificationEntity(
            packageName = normalized.packageName,
            appName = normalized.appName,
            sender = normalized.sender,
            title = normalized.title,
            text = normalized.text,
            channelName = normalized.channelName,
            channelImportance = normalized.channelImportance,
            importance = normalized.importance,
            category = normalized.category,
            postedTime = normalized.postedTime,
            classificationLabel = classificationResult.label.name,
            classificationScore = classificationResult.score,
            action = policyDecision.action.name,
            isSuppressed = policyDecision.shouldSuppress,
            isRead = false,
            hashedPackageName = normalized.hashedPackageName,
            hashedSender = normalized.hashedSender
        )

        val notificationDao = database.notificationDao()
        notificationDao.insert(notificationEntity)

        // Update app preference tracking
        appPreferenceDao.insert(
            AppPreferenceEntity(
                packageName = normalized.packageName,
                appName = normalized.appName,
                lastSeen = System.currentTimeMillis(),
                notificationCount = (existingPref?.notificationCount ?: 0) + 1
            )
        )

        // Update sender tracking
        normalized.hashedSender?.let { hashedSender ->
            val senderDao = database.senderDao()
            val existingSender = senderDao.getByHash(hashedSender)
            if (existingSender != null) {
                senderDao.incrementCount(hashedSender)
            } else {
                senderDao.insert(
                    SenderEntity(
                        hashedSender = hashedSender,
                        senderName = normalized.sender,
                        notificationCount = 1,
                        lastSeen = System.currentTimeMillis()
                    )
                )
            }
        }

        // Suppress notification if needed
        if (policyDecision.shouldSuppress) {
            try {
                cancelNotification(sbn.key)
            } catch (e: Exception) {
                // May fail if notification is from a different package or already removed
            }
        }
    }
}