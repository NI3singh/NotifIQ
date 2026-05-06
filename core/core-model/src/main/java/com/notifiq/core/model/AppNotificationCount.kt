package com.notifiq.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AppNotificationCount(
    val packageName: String,
    val appName: String,
    val count: Int
)