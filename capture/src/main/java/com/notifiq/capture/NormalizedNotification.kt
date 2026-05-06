package com.notifiq.capture

data class NormalizedNotification(
    val id: String,
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val postTime: Long,
    val channelId: String,
    val channelName: String,
    val groupKey: String,
    val category: String,
    val priority: Int,
    val importance: Int,
    val rawPayloadHash: String,
    val isGroupSummary: Boolean
)