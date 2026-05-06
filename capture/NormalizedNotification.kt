package com.notifiq.capture

data class NormalizedNotification(
    val packageName: String,
    val appName: String,
    val sender: String?,
    val title: String,
    val text: String,
    val channelName: String?,
    val channelImportance: Int,
    val importance: Int,
    val category: String?,
    val postedTime: Long,
    val hashedPackageName: String,
    val hashedSender: String?
)