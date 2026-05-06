package com.notifiq.core.model

data class AppRule(
    val packageName: String,
    val appName: String,
    val isAllowlisted: Boolean,
    val isBlocklisted: Boolean,
    val trustScore: Double = 0.5,
    val totalNotifications: Int = 0,
    val importantCount: Int = 0,
    val spamCount: Int = 0
)