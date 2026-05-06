package com.notifiq.classification.scorer

import com.notifiq.classification.ScoringContext
import com.notifiq.core.model.ClassificationLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppReputationScorerTest {

    private lateinit var scorer: AppReputationScorer

    @Before
    fun setup() {
        scorer = AppReputationScorer()
    }

    private fun createContext(packageName: String): ScoringContext {
        return ScoringContext(
            packageName = packageName,
            appName = "Test App",
            title = "Test Title",
            text = "Test Text",
            subText = "",
            bigText = "",
            channelId = "test_channel",
            importance = 3,
            postTime = System.currentTimeMillis(),
            category = ""
        )
    }

    @Test
    fun `protected phone app returns IMPORTANT override`() {
        val context = createContext("com.android.phone")
        val result = scorer.score(context)

        assertTrue("Expected isHardOverride to be true", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }

    @Test
    fun `SBI banking app returns IMPORTANT override`() {
        val context = createContext("com.sbi.lotusflowerbanking")
        val result = scorer.score(context)

        assertTrue("Expected isHardOverride to be true", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }

    @Test
    fun `WhatsApp returns positive delta`() {
        val context = createContext("com.whatsapp")
        val result = scorer.score(context)

        assertTrue("Expected delta to be positive", result.delta > 0)
        assertTrue("Expected reasons to mention messaging", result.reasons.any { it.contains("Messaging") })
    }

    @Test
    fun `Telegram returns positive delta`() {
        val context = createContext("org.telegram.messenger")
        val result = scorer.score(context)

        assertTrue("Expected delta to be positive", result.delta > 0)
        assertTrue("Expected reasons to mention messaging", result.reasons.any { it.contains("Messaging") })
    }

    @Test
    fun `Flipkart returns negative delta`() {
        val context = createContext("com.flipkart.android")
        val result = scorer.score(context)

        assertTrue("Expected delta to be negative", result.delta < 0)
        assertTrue("Expected reasons to mention promotional", result.reasons.any { it.contains("promotional") || it.contains("Known") })
    }

    @Test
    fun `Amazon returns negative delta`() {
        val context = createContext("in.amazon.mShop.android.shopping")
        val result = scorer.score(context)

        assertTrue("Expected delta to be negative", result.delta < 0)
        assertTrue("Expected reasons to mention promotional", result.reasons.any { it.contains("promotional") || it.contains("Known") })
    }

    @Test
    fun `Instagram returns slightly negative delta`() {
        val context = createContext("com.instagram.android")
        val result = scorer.score(context)

        assertTrue("Expected delta to be slightly negative", result.delta < 0)
        assertTrue("Expected delta to be > -0.1", result.delta > -0.1)
        assertTrue("Expected reasons to mention social media", result.reasons.any { it.contains("Social") })
    }

    @Test
    fun `GitHub returns positive delta`() {
        val context = createContext("com.github.android")
        val result = scorer.score(context)

        assertTrue("Expected delta to be positive", result.delta > 0)
        assertTrue("Expected reasons to mention developer or work tool", result.reasons.any { it.contains("Developer") || it.contains("Work") })
    }

    @Test
    fun `Unknown app returns zero delta`() {
        val context = createContext("com.example.unknown")
        val result = scorer.score(context)

        assertEquals("Expected delta to be 0 for unknown app", 0.0, result.delta, 0.001)
        assertTrue("Expected reasons to be empty", result.reasons.isEmpty())
    }

    @Test
    fun `Google Pay returns IMPORTANT override`() {
        val context = createContext("com.google.android.apps.nbu.paisa.user")
        val result = scorer.score(context)

        assertTrue("Expected isHardOverride to be true", result.isHardOverride)
        assertEquals("Expected override label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.overrideLabel)
    }
}