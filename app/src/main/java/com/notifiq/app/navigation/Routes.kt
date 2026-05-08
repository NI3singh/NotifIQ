package com.notifiq.app.navigation

sealed class Routes(val route: String) {
    data object Onboarding : Routes("onboarding")
    data object Home : Routes("home")
    data object Inbox : Routes("inbox")
    data object Analytics : Routes("analytics")
    data object Rules : Routes("rules")
    data object Settings : Routes("settings")

    // FIX 5C: NotificationRecord.id is a UUID String, not a Long.
    // Changed createRoute parameter and template to use String so the
    // entire detail/{notificationId} route is consistently typed as String.
    data object Detail : Routes("detail/{notificationId}") {
        fun createRoute(notificationId: String) = "detail/$notificationId"
    }

    data object Summary : Routes("summary")
}