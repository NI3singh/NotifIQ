package com.notifiq.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NormalizerHashTest {

    @Test
    fun `same input produces same hash`() {
        val packageName = "com.whatsapp"
        val title = "New message"
        val text = "Hello!"
        val timeBucket = 1700000000L / 60000 // minute granularity

        val payload1 = "$packageName|$title|$text|$timeBucket"
        val payload2 = "$packageName|$title|$text|$timeBucket"

        val hash1 = com.notifiq.core.common.HashUtils.sha256(payload1)
        val hash2 = com.notifiq.core.common.HashUtils.sha256(payload2)

        assertEquals("Same payload should produce same hash", hash1, hash2)
    }

    @Test
    fun `different input produces different hash`() {
        val timeBucket = 1700000000L / 60000

        val payload1 = "com.whatsapp|New message|Hello!|$timeBucket"
        val payload2 = "com.whatsapp|New message|Goodbye!|$timeBucket"

        val hash1 = com.notifiq.core.common.HashUtils.sha256(payload1)
        val hash2 = com.notifiq.core.common.HashUtils.sha256(payload2)

        assertNotEquals("Different payloads should produce different hashes", hash1, hash2)
    }

    @Test
    fun `timestamps 30 seconds apart produce same hash`() {
        val packageName = "com.whatsapp"
        val title = "New message"
        val text = "Hello!"

        // 30 seconds = 30,000 ms, which is within 1 minute granularity
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000000000L + 30000L

        val timeBucket1 = timestamp1 / 60000
        val timeBucket2 = timestamp2 / 60000

        assertEquals("30 second apart should be same time bucket", timeBucket1, timeBucket2)

        val payload1 = "$packageName|$title|$text|$timeBucket1"
        val payload2 = "$packageName|$title|$text|$timeBucket2"

        val hash1 = com.notifiq.core.common.HashUtils.sha256(payload1)
        val hash2 = com.notifiq.core.common.HashUtils.sha256(payload2)

        assertEquals("30 second apart should produce same hash", hash1, hash2)
    }

    @Test
    fun `timestamps 90 seconds apart produce different hashes`() {
        val packageName = "com.whatsapp"
        val title = "New message"
        val text = "Hello!"

        // 90 seconds = 90,000 ms, which crosses 1 minute boundary
        val timestamp1 = 1700000000000L
        val timestamp2 = 1700000000000L + 90000L

        val timeBucket1 = timestamp1 / 60000
        val timeBucket2 = timestamp2 / 60000

        assertNotEquals("90 second apart should be different time bucket", timeBucket1, timeBucket2)

        val payload1 = "$packageName|$title|$text|$timeBucket1"
        val payload2 = "$packageName|$title|$text|$timeBucket2"

        val hash1 = com.notifiq.core.common.HashUtils.sha256(payload1)
        val hash2 = com.notifiq.core.common.HashUtils.sha256(payload2)

        assertNotEquals("90 second apart should produce different hash", hash1, hash2)
    }

    @Test
    fun `different package names produce different hashes`() {
        val title = "New message"
        val text = "Hello!"
        val timeBucket = 1700000000L / 60000

        val payload1 = "com.whatsapp|$title|$text|$timeBucket"
        val payload2 = "com.instagram.android|$title|$text|$timeBucket"

        val hash1 = com.notifiq.core.common.HashUtils.sha256(payload1)
        val hash2 = com.notifiq.core.common.HashUtils.sha256(payload2)

        assertNotEquals("Different packages should produce different hashes", hash1, hash2)
    }
}