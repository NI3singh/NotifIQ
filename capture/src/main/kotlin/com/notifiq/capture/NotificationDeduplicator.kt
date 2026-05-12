package com.notifiq.capture

import javax.inject.Inject

class NotificationDeduplicator @Inject constructor() {

    private val cache = object : LinkedHashMap<String, Long>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > MAX_SIZE
        }
    }

    @Synchronized
    fun isDuplicate(hash: String): Boolean {
        val now = System.currentTimeMillis()

        val storedTimestamp = cache[hash]
        if (storedTimestamp != null && (now - storedTimestamp) <= DEDUP_WINDOW_MS) {
            return true
        }

        // Add hash with current timestamp
        cache[hash] = now

        // Evict entries older than 5 minutes
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if ((now - entry.value) > EVICTION_WINDOW_MS) {
                iterator.remove()
            }
        }

        return false
    }

    companion object {
        private const val MAX_SIZE = 200
        private const val DEDUP_WINDOW_MS = 60_000L // 1 minute
        private const val EVICTION_WINDOW_MS = 300_000L // 5 minutes
    }
}