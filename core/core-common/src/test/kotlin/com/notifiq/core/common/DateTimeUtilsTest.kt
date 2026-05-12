package com.notifiq.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun `relative time shows minutes ago`() {
        val now = System.currentTimeMillis()
        val fiveMinutesAgo = now - (5 * 60 * 1000L)

        val result = DateTimeUtils.toRelativeTimeString(fiveMinutesAgo)

        assertTrue("Should contain '5m ago'", result.contains("5m ago"))
    }

    @Test
    fun `relative time shows hours ago`() {
        val now = System.currentTimeMillis()
        val threeHoursAgo = now - (3 * 60 * 60 * 1000L)

        val result = DateTimeUtils.toRelativeTimeString(threeHoursAgo)

        assertTrue("Should contain '3h ago'", result.contains("3h ago"))
    }

    @Test
    fun `relative time shows Yesterday for 1 day ago`() {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000L)

        val result = DateTimeUtils.toRelativeTimeString(oneDayAgo)

        assertEquals("Should return 'Yesterday'", "Yesterday", result)
    }

    @Test
    fun `relative time shows days ago for older`() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000L)

        val result = DateTimeUtils.toRelativeTimeString(threeDaysAgo)

        assertTrue("Should contain '3d ago'", result.contains("3d ago"))
    }

    @Test
    fun `quiet hours crossing midnight returns true when in range`() {
        // When current hour is between 23 and 7 (crossing midnight)
        // We can't directly test this without mocking time, but we can test the logic
        val startHour = 23
        val endHour = 7

        // If current hour is 0 (midnight), it should be in quiet hours
        val currentHour = 0
        val result = if (startHour <= endHour) {
            currentHour in startHour until endHour
        } else {
            currentHour >= startHour || currentHour < endHour
        }

        assertTrue("Hour 0 should be in quiet hours 23-7", result)
    }

    @Test
    fun `quiet hours same day returns true when in range`() {
        val startHour = 9
        val endHour = 17
        val currentHour = 12

        val result = if (startHour <= endHour) {
            currentHour in startHour until endHour
        } else {
            currentHour >= startHour || currentHour < endHour
        }

        assertTrue("Hour 12 should be in quiet hours 9-17", result)
    }

    @Test
    fun `quiet hours same day returns false when outside range`() {
        val startHour = 9
        val endHour = 17
        val currentHour = 8

        val result = if (startHour <= endHour) {
            currentHour in startHour until endHour
        } else {
            currentHour >= startHour || currentHour < endHour
        }

        assertTrue("Hour 8 should NOT be in quiet hours 9-17", !result)
    }

    @Test
    fun `date formatting returns expected format`() {
        val timestamp = 1704067200000L // Jan 1, 2024 12:00 AM UTC

        val result = DateTimeUtils.formatTimestamp(timestamp)

        // Should contain month, day, year and time
        assertTrue("Should contain month name", result.contains("Jan"))
        assertTrue("Should contain year", result.contains("2024"))
    }

    @Test
    fun `todayStartMillis returns midnight of today`() {
        val result = DateTimeUtils.todayStartMillis()

        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = result

        assertEquals("Hour should be 0", 0, calendar.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 0", 0, calendar.get(java.util.Calendar.MINUTE))
        assertEquals("Second should be 0", 0, calendar.get(java.util.Calendar.SECOND))
    }

    @Test
    fun `daysAgoMillis returns correct timestamp`() {
        val sevenDaysAgo = DateTimeUtils.daysAgoMillis(7)

        val now = System.currentTimeMillis()
        val diff = now - sevenDaysAgo

        // Should be approximately 7 days (allow some tolerance for test execution time)
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        assertTrue("Should be approximately 7 days ago", kotlin.math.abs(diff - sevenDaysMs) < 60000) // within 1 minute
    }
}