package com.notifiq.app.navigation

sealed class Routes(val route: String) {
    data object Onboarding : Routes("onboarding")
    data object Home : Routes("home")
    data object Inbox : Routes("inbox")
    data object Analytics : Routes("analytics")
    data object Rules : Routes("rules")
    data object Settings : Routes("settings")
    data object Detail : Routes("detail/{notificationId}") {
        fun createRoute(notificationId: Long) = "detail/$notificationId"
    }
    data object Summary : Routes("summary")
}