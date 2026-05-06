package com.notifiq.classification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SafetyGuardTest {

    private lateinit var safetyGuard: SafetyGuard

    @Before
    fun setup() {
        safetyGuard = SafetyGuard()
    }

    @Test
    fun `OTP in text returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "Your OTP is 483921",
            bigText = "",
            category = null,
            importance = 3
        )

        assertTrue("OTP in text should return true", result)
    }

    @Test
    fun `OTP code in big text returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "",
            bigText = "Verification code: 1234. Your one-time password for login.",
            category = null,
            importance = 3
        )

        assertTrue("OTP in bigText should return true", result)
    }

    @Test
    fun `Protected package returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.android.phone",
            text = "Missed call from John",
            bigText = "",
            category = null,
            importance = 3
        )

        assertTrue("Protected package should return true", result)
    }

    @Test
    fun `Banking package returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.phonepe.app",
            text = "Payment received",
            bigText = "",
            category = null,
            importance = 3
        )

        assertTrue("Banking package should return true", result)
    }

    @Test
    fun `Alarm category returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "Your alarm is set for 7 AM",
            bigText = "",
            category = "alarm",
            importance = 3
        )

        assertTrue("Alarm category should return true", result)
    }

    @Test
    fun `High importance returns true`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "Regular notification",
            bigText = "",
            category = null,
            importance = 4 // IMPORTANCE_HIGH = 4
        )

        assertTrue("High importance should return true", result)
    }

    @Test
    fun `Normal promotional text returns false`() {
        val result = safetyGuard.isSafetyProtected(
            packageName = "com.flipkart.android",
            text = "Flash sale 80% off",
            bigText = "",
            category = null,
            importance = 3
        )

        assertFalse("Normal promotional text should return false", result)
    }

    @Test
    fun `OTP regex with various formats`() {
        // "one-time password" format
        val result1 = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "Your one-time password is 123456",
            bigText = "",
            category = null,
            importance = 3
        )
        assertTrue("one-time password format should return true", result1)

        // "your code is" format
        val result2 = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "Your code is 567890",
            bigText = "",
            category = null,
            importance = 3
        )
        assertTrue("your code is format should return true", result2)

        // "2FA code:" format
        val result3 = safetyGuard.isSafetyProtected(
            packageName = "com.example.app",
            text = "2FA code: 987654",
            bigText = "",
            category = null,
            importance = 3
        )
        assertTrue("2FA code format should return true", result3)
    }
}