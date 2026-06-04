package com.notifiq.classification

import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.ClassificationResult
import com.notifiq.core.model.NotificationAction
import com.notifiq.core.model.UserPreference
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PolicyEngineTest {

    private lateinit var policyEngine: PolicyEngine
    private lateinit var safetyGuard: SafetyGuard
    private lateinit var userPreferenceDataStore: UserPreferenceDataStore

    @Before
    fun setup() {
        safetyGuard = SafetyGuard()
        userPreferenceDataStore = mockk(relaxed = true)
        policyEngine = PolicyEngine(userPreferenceDataStore, safetyGuard)
    }

    // Mirrors ScoreAggregator.labelToAction so the fixture's recommendedAction
    // matches what a real classification would carry for the given label.
    private fun createClassificationResult(
        label: ClassificationLabel,
        confidence: Float = 0.5f
    ): ClassificationResult {
        val action = when (label) {
            ClassificationLabel.LOW_VALUE -> NotificationAction.INBOX_ONLY
            ClassificationLabel.SPAM -> NotificationAction.SUPPRESS
            else -> NotificationAction.SHOW_AND_INBOX
        }
        return ClassificationResult(
            label = label,
            confidence = confidence,
            reasons = listOf("test"),
            recommendedAction = action,
            sourceRuleId = "test",
            isAmbiguous = false
        )
    }

    @Test
    fun `Safety protected notification always returns SHOW_AND_INBOX`() = runBlocking {
        val result = policyEngine.decide(
            classification = createClassificationResult(ClassificationLabel.LOW_VALUE),
            packageName = "com.example.app",
            text = "Your OTP is 123456",
            bigText = "",
            category = null,
            importance = 3,
            postTime = System.currentTimeMillis()
        )
        assertEquals(NotificationAction.SHOW_AND_INBOX, result)
    }

    @Test
    fun `Suppression disabled always returns SHOW_AND_INBOX`() = runBlocking {
        coEvery { userPreferenceDataStore.userPreference } returns flowOf(
            UserPreference(suppressionEnabled = false)
        )

        val result = policyEngine.decide(
            classification = createClassificationResult(ClassificationLabel.SPAM, 0.15f),
            packageName = "com.flipkart.android",
            text = "Flash sale!",
            bigText = "",
            category = null,
            importance = 3,
            postTime = System.currentTimeMillis()
        )

        assertEquals(
            "Suppression disabled should return SHOW_AND_INBOX",
            NotificationAction.SHOW_AND_INBOX,
            result
        )
    }

    @Test
    fun `Suppression enabled with SPAM classification returns SUPPRESS`() = runBlocking {
        // Suppression on, quiet hours off, so the SPAM -> SUPPRESS path is deterministic.
        coEvery { userPreferenceDataStore.userPreference } returns flowOf(
            UserPreference(suppressionEnabled = true, quietHoursEnabled = false)
        )

        val result = policyEngine.decide(
            classification = createClassificationResult(ClassificationLabel.SPAM, 0.15f),
            packageName = "com.flipkart.android",
            text = "Sale! 80% off!",
            bigText = "",
            category = null,
            importance = 3,
            postTime = System.currentTimeMillis()
        )

        assertEquals(
            "SPAM with suppression enabled should return SUPPRESS",
            NotificationAction.SUPPRESS,
            result
        )
    }

    @Test
    fun `Quiet hours active with NORMAL classification returns INBOX_ONLY`() = runBlocking {
        coEvery { userPreferenceDataStore.userPreference } returns flowOf(
            UserPreference(
                suppressionEnabled = true,
                quietHoursEnabled = true,
                quietHoursStart = 23,
                quietHoursEnd = 7
            )
        )

        // Pin the clock-dependent check so the test is deterministic.
        mockkObject(DateTimeUtils)
        try {
            every { DateTimeUtils.isQuietHours(any(), any()) } returns true

            val result = policyEngine.decide(
                classification = createClassificationResult(ClassificationLabel.NORMAL),
                packageName = "com.example.app",
                text = "Regular update",
                bigText = "",
                category = null,
                importance = 3,
                postTime = System.currentTimeMillis()
            )

            assertEquals(
                "NORMAL during quiet hours should return INBOX_ONLY",
                NotificationAction.INBOX_ONLY,
                result
            )
        } finally {
            unmockkObject(DateTimeUtils)
        }
    }

    @Test
    fun `Quiet hours active with IMPORTANT classification returns SHOW_AND_INBOX`() = runBlocking {
        // IMPORTANT bypasses the quiet-hours branch regardless of the clock.
        // importance = 3 keeps the SafetyGuard out of it so we test the label path.
        coEvery { userPreferenceDataStore.userPreference } returns flowOf(
            UserPreference(
                suppressionEnabled = true,
                quietHoursEnabled = true,
                quietHoursStart = 23,
                quietHoursEnd = 7
            )
        )

        val result = policyEngine.decide(
            classification = createClassificationResult(ClassificationLabel.IMPORTANT),
            packageName = "com.example.app",
            text = "Important update",
            bigText = "",
            category = null,
            importance = 3,
            postTime = System.currentTimeMillis()
        )

        assertEquals(
            "IMPORTANT should bypass quiet hours",
            NotificationAction.SHOW_AND_INBOX,
            result
        )
    }

    @Test
    fun `Focus mode active with LOW_VALUE returns INBOX_ONLY`() = runBlocking {
        // Quiet hours off so we exercise the focus-mode path specifically.
        coEvery { userPreferenceDataStore.userPreference } returns flowOf(
            UserPreference(
                suppressionEnabled = true,
                quietHoursEnabled = false,
                focusModeEnabled = true
            )
        )

        val result = policyEngine.decide(
            classification = createClassificationResult(ClassificationLabel.LOW_VALUE),
            packageName = "com.example.app",
            text = "Flash sale!",
            bigText = "",
            category = null,
            importance = 3,
            postTime = System.currentTimeMillis()
        )

        assertEquals(
            "LOW_VALUE during focus mode should return INBOX_ONLY",
            NotificationAction.INBOX_ONLY,
            result
        )
    }
}
