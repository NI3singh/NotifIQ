package com.notifiq.core.model

data class UserPreference(
    val suppressionEnabled: Boolean = false,
    val learningEnabled: Boolean = true,
    val summaryEnabled: Boolean = true,
    val summaryFrequency: String = "daily",
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: Int = 23,
    val quietHoursEnd: Int = 7,
    val focusModeEnabled: Boolean = false,
    val darkModeEnabled: Boolean = true,
    val dataRetentionDays: Int = 30,
    val hasCompletedOnboarding: Boolean = false,
    val oemBannerDismissed: Boolean = false
)