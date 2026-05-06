package com.notifiq.capture

import com.notifiq.core.common.HashUtils

class NotificationDeduplicator {
    private val recentNotifications = mutableMapOf<String, Long>()
    private val deduplicationWindowMs = 5000L // 5 seconds

    fun isDuplicate(notification: NormalizedNotification): Boolean {
        val key = generateKey(notification)

        val lastSeen = recentNotifications[key]
        val now = System.currentTimeMillis()

        if (lastSeen != null && now - lastSeen < deduplicationWindowMs) {
            return true
        }

        recentNotifications[key] = now
        cleanupOldEntries(now)
        return false
    }

    private fun generateKey(notification: NormalizedNotification): String {
        return "${notification.hashedPackageName}_${notification.title}_${notification.text.take(50)}"
    }

    private fun cleanupOldEntries(now: Long) {
        val iterator = recentNotifications.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > deduplicationWindowMs * 2) {
                iterator.remove()
            }
        }
    }

    fun clear() {
        recentNotifications.clear()
    }
}