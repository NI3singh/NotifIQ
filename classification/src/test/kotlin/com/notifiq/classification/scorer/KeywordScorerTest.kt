package com.notifiq.classification.scorer

import com.notifiq.classification.ScoringContext
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.model.ClassificationLabel
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeywordScorerTest {

    private lateinit var scorer: KeywordScorer

    @Before
    fun setup() {
        val mockRuleDao = mockk<RuleDao>(relaxed = true)
        scorer = KeywordScorer(mockRuleDao)
    }

    private fun createContext(
        title: String = "",
        text: String = "",
        bigText: String = "",
        packageName: String = "com.example.app"
    ): ScoringContext {
        return ScoringContext(
            packageName = packageName,
            appName = "Test App",
            title = title,
            text = text,
            subText = "",
            bigText = bigText,
            channelId = "test_channel",
            importance = 3,
            postTime = System.currentTimeMillis(),
            category = ""
        )
    }

    @Test
    fun `otp keyword in text returns IMPORTANT hard override`() {
        val context = createContext(text = "Your OTP is 483921")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected isHardOverride to be true", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }

    @Test
    fun `verification code keyword returns IMPORTANT`() {
        val context = createContext(text = "Verification code: 123456")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected isHardOverride to be true", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }

    @Test
    fun `banking keyword credited returns high positive delta`() {
        val context = createContext(text = "Rs. 5,000 credited to your account")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.3)
        assertTrue("Expected reasons to contain banking keyword", result.reasons.any { it.contains("Banking") })
    }

    @Test
    fun `banking keyword debited returns high positive delta`() {
        val context = createContext(text = "Rs. 500 debited from your account")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.3)
        assertTrue("Expected reasons to contain banking keyword", result.reasons.any { it.contains("Banking") })
    }

    @Test
    fun `important keyword emergency returns high positive delta`() {
        val context = createContext(text = "Emergency alert: earthquake warning in your area")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.3)
        assertTrue("Expected reasons to contain Important keyword", result.reasons.any { it.contains("Important") })
    }

    @Test
    fun `delivery keyword returns moderate positive delta`() {
        val context = createContext(text = "Your order has been shipped via FedEx")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.2)
        assertTrue("Expected reasons to contain delivery keyword", result.reasons.any { it.contains("Delivery") })
    }

    @Test
    fun `single promo keyword returns negative delta`() {
        val context = createContext(text = "Flash sale on electronics!")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be negative", result.delta < 0)
        assertTrue("Expected reasons to contain promotional", result.reasons.any { it.contains("Promotional") })
    }

    @Test
    fun `multiple promo keywords cap at 3 hits`() {
        val context = createContext(text = "Flash sale! Buy now! Limited time offer! Hurry! Best price!")
        val result = runBlocking { scorer.score(context) }

        // Each promo hit is -0.12, but capped at 3 hits = -0.36
        assertTrue("Expected delta to be negative but capped", result.delta >= -0.4)
        assertTrue("Expected delta should be around -0.36", result.delta <= -0.3)
    }

    @Test
    fun `no keywords returns zero delta`() {
        val context = createContext(text = "Hey, what's up?")
        val result = runBlocking { scorer.score(context) }

        assertEquals("Expected delta to be 0", 0.0, result.delta, 0.001)
        assertTrue("Expected reasons to be empty", result.reasons.isEmpty())
    }

    @Test
    fun `empty text returns zero delta`() {
        val context = createContext(text = "")
        val result = runBlocking { scorer.score(context) }

        assertEquals("Expected delta to be 0", 0.0, result.delta, 0.001)
    }

    @Test
    fun `mixed OTP and promo returns IMPORTANT - OTP wins`() {
        val context = createContext(text = "Your OTP for purchase offer is 5432")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected isHardOverride to be true (OTP should win)", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }

    @Test
    fun `case insensitive matching works`() {
        val context = createContext(text = "YOUR OTP IS 1234")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected isHardOverride to be true (case insensitive)", result.isHardOverride)
    }

    @Test
    fun `banking keyword payment received`() {
        val context = createContext(text = "Payment received: Rs. 10,000")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.3)
        assertTrue("Expected reasons to contain banking", result.reasons.any { it.contains("Banking") })
    }

    @Test
    fun `meeting keyword returns positive delta`() {
        val context = createContext(text = "Meeting starts in 5 minutes")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.15)
        assertTrue("Expected reasons to contain meeting", result.reasons.any { it.contains("Meeting") })
    }

    @Test
    fun `delivery keyword arriving today`() {
        val context = createContext(text = "Your package is arriving today")
        val result = runBlocking { scorer.score(context) }

        assertTrue("Expected delta to be positive", result.delta > 0.2)
        assertTrue("Expected reasons to contain delivery", result.reasons.any { it.contains("Delivery") })
    }
}