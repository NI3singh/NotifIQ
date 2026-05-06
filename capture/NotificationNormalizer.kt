package com.notifiq.capture

import android.app.Notification
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.StatusBarNotification
import com.notifiq.core.common.HashUtils

class NotificationNormalizer {

    fun normalize(sbn: StatusBarNotification): NormalizedNotification {
        val notification = sbn.notification
        val packageManager = sbn.packageManager

        val appName = try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val extras = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        val channel = notification.channelId

        val channelImportance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notification.importance
        } else {
            Notification.DEFAULT_ALL
        }

        val importance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            notification.importance
        } else {
            0
        }

        val category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.category
        } else {
            null
        }

        val sender = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SENDER_TEXT)?.toString()

        return NormalizedNotification(
            packageName = sbn.packageName,
            appName = appName,
            sender = sender,
            title = title,
            text = text,
            channelName = channel,
            channelImportance = channelImportance,
            importance = importance,
            category = category,
            postedTime = sbn.postTime,
            hashedPackageName = HashUtils.hashPackageName(sbn.packageName),
            hashedSender = sender?.let { HashUtils.hashSender(it) }
        )
    }
}