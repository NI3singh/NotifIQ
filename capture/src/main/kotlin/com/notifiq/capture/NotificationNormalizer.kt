package com.notifiq.capture

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import com.notifiq.core.common.HashUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

class NotificationNormalizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun normalize(sbn: StatusBarNotification): NormalizedNotification {
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

        // Get channel info
        val channelId = notification.channelId ?: ""
        val channelName = notification.channelId ?: ""

        // Get category
        val category = notification.category ?: ""

        // Check if group summary
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0

        // Generate unique ID and hash
        val id = UUID.randomUUID().toString()
        val key = sbn.key
        val rawPayloadHash = computeHash(sbn)

        // Get importance from channel or default
        val importance = if (notification.channelId != null) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = nm.getNotificationChannel(notification.channelId)
                channel?.importance ?: NotificationManager.IMPORTANCE_DEFAULT
            } catch (e: Exception) {
                NotificationManager.IMPORTANCE_DEFAULT
            }
        } else {
            NotificationManager.IMPORTANCE_DEFAULT
        }

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