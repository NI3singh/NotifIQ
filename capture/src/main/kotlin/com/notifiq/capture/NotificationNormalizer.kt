package com.notifiq.capture

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import com.notifiq.core.common.HashUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class NotificationNormalizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun normalize(sbn: StatusBarNotification, ranking: Ranking? = null): NormalizedNotification {
        val notification = sbn.notification
        val extras = notification.extras

        // Extract text fields
        val title = extras.getCharSequence(Notification.EXTRA_TITLE, "")?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT, "")?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT, "")?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT, "")?.toString()?.trim() ?: ""

        // Resolve app name
        val appName = try {
            val appInfo = context.packageManager.getApplicationInfo(sbn.packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName.substringAfterLast(".")
        }

        // Channel + importance MUST come from the system Ranking. The previous
        // implementation called NotificationManager.getNotificationChannel(), but
        // that only returns channels owned by NotifIQ itself - never the source
        // app's - so importance was always DEFAULT (breaking ChannelImportanceScorer
        // and the SafetyGuard importance check) and the channel name was never
        // available. Ranking.getChannel() needs API 28+; importance needs only 26+.
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ranking?.channel else null
        val channelId = channel?.id ?: notification.channelId ?: ""
        val channelName = channel?.name?.toString() ?: ""

        val rankImportance = ranking?.importance
        val importance = if (rankImportance != null && rankImportance >= 0) {
            rankImportance
        } else {
            NotificationManager.IMPORTANCE_DEFAULT
        }

        // Get category
        val category = notification.category ?: ""

        // Check if group summary
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        // Generate unique ID and hash
        val id = UUID.randomUUID().toString()
        val key = sbn.key
        val rawPayloadHash = computeHash(sbn)

        return NormalizedNotification(
            id = id,
            key = key,
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            postTime = sbn.postTime,
            channelId = channelId,
            channelName = channelName,
            groupKey = sbn.groupKey ?: "",
            category = category,
            priority = notification.priority,
            importance = importance,
            rawPayloadHash = rawPayloadHash,
            isGroupSummary = isGroupSummary
        )
    }

    fun computeHash(sbn: StatusBarNotification): String {
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE, "")?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT, "")?.toString() ?: ""
        // 1-minute granularity for deduplication
        val timeBucket = sbn.postTime / 60000
        val payload = "${sbn.packageName}|$title|$text|$timeBucket"
        return HashUtils.sha256(payload)
    }
}
