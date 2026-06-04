package com.notifiq.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notifiq.classification.ClassificationEngine
import com.notifiq.classification.PolicyEngine
import com.notifiq.classification.ScoringContext
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationAction
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class NotifIQListenerService : NotificationListenerService() {

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            NotifIQListenerServiceEntryPoint::class.java
        )
    }

    private val notificationNormalizer by lazy { entryPoint.notificationNormalizer() }
    private val notificationDeduplicator by lazy { entryPoint.notificationDeduplicator() }
    private val classificationEngine by lazy { entryPoint.classificationEngine() }
    private val policyEngine by lazy { entryPoint.policyEngine() }
    private val notificationDao by lazy { entryPoint.notificationDao() }
    private val appPreferenceDao by lazy { entryPoint.appPreferenceDao() }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Single Json instance reused for encoding classification reasons
    private val json = Json { ignoreUnknownKeys = true }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap?) {
        if (sbn.packageName == applicationContext.packageName) return
        if ((sbn.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) return

        // Resolve the system Ranking now: it is a snapshot tied to this callback
        // and is the only correct source of channel importance + name for a
        // notification owned by another app. Guarded against malformed RankingMaps
        // on some OEM builds - worst case we fall back to default importance.
        val ranking = rankingMap?.let { map ->
            try {
                val r = Ranking()
                if (map.getRanking(sbn.key, r)) r else null
            } catch (t: Throwable) {
                null
            }
        }

        serviceScope.launch {
            try {
                processNotification(sbn, ranking)
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
        if (sbn.packageName == applicationContext.packageName) return

        serviceScope.launch {
            try {
                handleNotificationRemoved(sbn, reason)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification, ranking: Ranking?) {
        val normalized = notificationNormalizer.normalize(sbn, ranking)

        if (notificationDeduplicator.isDuplicate(normalized.rawPayloadHash)) return

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

        val classification = classificationEngine.classify(scoringContext)

        val action = policyEngine.decide(
            classification = classification,
            packageName = normalized.packageName,
            text = normalized.text,
            bigText = normalized.bigText,
            category = normalized.category,
            importance = normalized.importance,
            postTime = normalized.postTime
        )

        val isSuppressed = action == NotificationAction.SUPPRESS ||
                action == NotificationAction.INBOX_ONLY

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
            classificationReasons = json.encodeToString(classification.reasons),
            action = action.name,
            isRead = false,
            isSuppressed = isSuppressed,
            isArchived = false,
            rawPayloadHash = normalized.rawPayloadHash,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        notificationDao.insert(entity)
        updateAppStats(normalized.packageName, normalized.appName, classification.label)

        if (isSuppressed) {
            try {
                cancelNotification(normalized.key)
            } catch (e: SecurityException) {
                // Notification access may have been revoked
            }
        }
    }

    private suspend fun updateAppStats(
        packageName: String,
        appName: String,
        label: ClassificationLabel
    ) {
        val existing = appPreferenceDao.getByPackage(packageName)
        val now = System.currentTimeMillis()

        if (existing != null) {
            when (label) {
                ClassificationLabel.IMPORTANT, ClassificationLabel.USEFUL ->
                    appPreferenceDao.incrementImportantCount(packageName, now)
                ClassificationLabel.SPAM ->
                    appPreferenceDao.incrementSpamCount(packageName, now)
                else -> { /* No special increment */ }
            }
            appPreferenceDao.incrementNotificationCount(packageName, now)
        } else {
            val entity = AppPreferenceEntity(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                appName = appName,
                isAllowlisted = false,
                isBlocklisted = false,
                trustScore = 0.5,
                totalNotifications = 1,
                importantCount = if (label == ClassificationLabel.IMPORTANT ||
                    label == ClassificationLabel.USEFUL) 1 else 0,
                spamCount = if (label == ClassificationLabel.SPAM) 1 else 0,
                isMuted = false,
                createdAt = now,
                updatedAt = now
            )
            appPreferenceDao.insert(entity)
        }
    }

    private suspend fun handleNotificationRemoved(sbn: StatusBarNotification, reason: Int) {
        when (reason) {
            REASON_CLICK, REASON_CANCEL -> { /* Could log for learning */ }
            else -> { /* Ignore */ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}