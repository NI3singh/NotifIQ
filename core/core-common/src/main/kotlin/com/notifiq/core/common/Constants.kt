package com.notifiq.core.common

object Constants {
    const val MAX_NOTIFICATIONS_IN_MEMORY = 1000
    const val DEDUP_WINDOW_MS = 60_000L
    const val DEDUP_CACHE_SIZE = 200
    const val DEFAULT_DATA_RETENTION_DAYS = 30
    const val FEEDBACK_POSITIVE_DELTA = 0.08
    const val FEEDBACK_NEGATIVE_DELTA = -0.10
    const val FEEDBACK_CAP_POSITIVE = 0.25
    const val FEEDBACK_CAP_NEGATIVE = -0.25

    // Safety-related constants
    val PROTECTED_APP_PACKAGES = listOf(
        "com.android.phone",
        "com.android.dialer",
        "com.google.android.apps.dialer",
        "com.samsung.android.dialer",
        "com.sbi.lotusflowerbanking",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "in.org.npci.upiapp",
        "com.yesbank"
    )

    // Android notification category string VALUES (all lowercase, e.g.
    // Notification.CATEGORY_ALARM == "alarm", CATEGORY_SYSTEM == "sys").
    // Compared case-insensitively in SafetyGuard.
    val SAFETY_CATEGORIES = listOf(
        "alarm",          // CATEGORY_ALARM
        "call",           // CATEGORY_CALL
        "navigation",     // CATEGORY_NAVIGATION
        "transport",      // CATEGORY_TRANSPORT
        "sys",            // CATEGORY_SYSTEM (literal value is "sys", not "system")
        "reminder",       // CATEGORY_REMINDER
        "emergency",      // defensive: non-standard value used by some OEMs
        "car_emergency"   // CATEGORY_CAR_EMERGENCY
    )

    const val BASE_SCORE = 0.5f
}