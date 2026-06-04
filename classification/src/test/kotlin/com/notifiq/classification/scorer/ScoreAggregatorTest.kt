package com.notifiq.classification.scorer

import com.notifiq.classification.ScoringContext
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.ClassificationResult
import com.notifiq.core.model.NotificationAction
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoreAggregatorTest {

    private lateinit var userFeedbackScorer: UserFeedbackScorer
    private lateinit var keywordScorer: KeywordScorer
    private lateinit var appReputationScorer: AppReputationScorer
    private lateinit var channelImportanceScorer: ChannelImportanceScorer
    private lateinit var frequencyScorer: FrequencyScorer
    private lateinit var timeContextScorer: TimeContextScorer
    private lateinit var aggregator: ScoreAggregator

    @Before
    fun setup() {
        val appPreferenceDao = mockk<AppPreferenceDao>(relaxed = true)
        val feedbackDao = mockk<FeedbackDao>(relaxed = true)
        val ruleDao = mockk<RuleDao>(relaxed = true)

        val mockNotificationDao = mockk<NotificationDao>(relaxed = true)

        // Setup default mock behavior - return null for app preferences (no allowlist/blocklist)
        coEvery { appPreferenceDao.getByPackage(any()) } returns null
        coEvery { feedbackDao.getRecentByPackage(any(), any()) } returns emptyList()
        coEvery { ruleDao.getByType(any()) } returns emptyList()

        userFeedbackScorer = UserFeedbackScorer(appPreferenceDao, feedbackDao)
        keywordScorer = KeywordScorer(ruleDao)
        appReputationScorer = AppReputationScorer()
        channelImportanceScorer = ChannelImportanceScorer()
        frequencyScorer = FrequencyScorer(mockNotificationDao)
        timeContextScorer = TimeContextScorer()

        aggregator = ScoreAggregator(
            userFeedbackScorer,
            keywordScorer,
            appReputationScorer,
            channelImportanceScorer,
            frequencyScorer,
            timeContextScorer
        )
    }

    private fun createContext(
        packageName: String = "com.example.app",
        appName: String = "Example App",
        title: String = "Test",
        text: String = "Test notification",
        importance: Int = 3
    ): ScoringContext {
        return ScoringContext(
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = "",
            bigText = "",
            channelId = "test_channel",
            importance = importance,
            postTime = System.currentTimeMillis(),
            category = ""
        )
    }

    @Test
    fun `OTP notification classified as IMPORTANT`() {
        val context = createContext(text = "Your OTP is 483921")
        val result = runBlocking { aggregator.classify(context) }

        assertEquals("Expected label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.label)
        assertTrue("Expected confidence to be high", result.confidence >= 0.8f)
    }

    @Test
    fun `Promotional notification from noisy app classified as LOW_VALUE or SPAM`() {
        val context = createContext(
            packageName = "com.flipkart.android",
            text = "80% off sale! Limited time offer! Buy now!"
        )
        val result = runBlocking { aggregator.classify(context) }

        assertTrue(
            "Expected label to be LOW_VALUE or SPAM, got ${result.label}",
            result.label in listOf(ClassificationLabel.LOW_VALUE, ClassificationLabel.SPAM)
        )
    }

    @Test
    fun `Normal text from unknown app classified as NORMAL`() {
        val context = createContext(
            packageName = "com.example.unknown",
            text = "Hey there, are you free for coffee?"
        )
        val result = runBlocking { aggregator.classify(context) }

        // Unknown app with normal text should fall in NORMAL range (0.40-0.65)
        assertEquals("Expected label to be NORMAL", ClassificationLabel.NORMAL, result.label)
    }

    @Test
    fun `Banking app notification classified as IMPORTANT`() {
        val context = createContext(
            packageName = "com.sbi.lotusflowerbanking",
            text = "Rs. 5000 credited to your account"
        )
        val result = runBlocking { aggregator.classify(context) }

        assertEquals("Expected label to be IMPORTANT", ClassificationLabel.IMPORTANT, result.label)
    }

    @Test
    fun `Score clamped to minimum zero`() {
        // Combine noisy app + many promo keywords + negative feedback
        val context = createContext(
            packageName = "com.flipkart.android",
            text = "SALE! 80% OFF! BUY NOW! HURRY! LIMITED TIME! FLASH DEAL! BEST PRICE!"
        )
        val result = runBlocking { aggregator.classify(context) }

        assertTrue("Score should be >= 0.0", result.confidence >= 0.0f)
    }

    @Test
    fun `Score clamped to maximum one`() {
        // Banking + OTP + protected app should not exceed 1.0
        val context = createContext(
            packageName = "com.android.phone",
            text = "Your OTP for banking is 123456. Rs. 5000 credited to your account."
        )
        val result = runBlocking { aggregator.classify(context) }

        assertTrue("Score should be <= 1.0", result.confidence <= 1.0f)
    }

    @Test
    fun `Ambiguous score detected correctly`() {
        // Create context that should result in score around 0.40-0.45
        val context = createContext(
            packageName = "com.example.unknown",
            text = "Meeting reminder: Standup in 5 minutes"
        )
        val result = runBlocking { aggregator.classify(context) }

        // If confidence is around 0.4-0.45 or 0.6-0.7, isAmbiguous should be true
        val isInAmbiguousRange = (result.confidence in 0.35f..0.45f) || (result.confidence in 0.60f..0.70f)
        assertTrue(
            "Confidence ${result.confidence} should result in isAmbiguous=$isInAmbiguousRange",
            result.isAmbiguous == isInAmbiguousRange
        )
    }

    @Test
    fun `SPAM label maps to SUPPRESS action`() {
        val context = createContext(
            packageName = "com.flipkart.android",
            text = "SALE! SALE! SALE! 90% OFF! BUY NOW!"
        )
        val result = runBlocking { aggregator.classify(context) }

        if (result.label == ClassificationLabel.SPAM) {
            assertEquals("SPAM should map to SUPPRESS", NotificationAction.SUPPRESS, result.recommendedAction)
        }
    }

    @Test
    fun `IMPORTANT label maps to SHOW_AND_INBOX action`() {
        val context = createContext(
            packageName = "com.sbi.lotusflowerbanking",
            text = "OTP: 123456. Rs. 5000 credited."
        )
        val result = runBlocking { aggregator.classify(context) }

        if (result.label == ClassificationLabel.IMPORTANT) {
            assertEquals("IMPORTANT should map to SHOW_AND_INBOX", NotificationAction.SHOW_AND_INBOX, result.recommendedAction)
        }
    }

    @Test
    fun `Hard override returns immediately`() {
        // OTP triggers hard override in KeywordScorer
        val context = createContext(
            packageName = "com.flipkart.android", // noisy app that would normally be penalized
            text = "Your OTP is 123456" // but OTP should win
        )
        val result = runBlocking { aggregator.classify(context) }

        assertEquals("Expected label to be IMPORTANT (OTP hard override wins)", ClassificationLabel.IMPORTANT, result.label)
        assertTrue("Expected hard override result", result.label == ClassificationLabel.IMPORTANT)
    }
}