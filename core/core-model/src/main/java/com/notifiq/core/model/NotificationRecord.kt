package com.notifiq.core.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRecord(
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
    val classificationLabel: ClassificationLabel,
    val classificationScore: Float,
    val classificationReasons: List<String>,
    val action: NotificationAction,
    val isRead: Boolean,
    val isSuppressed: Boolean,
    val isArchived: Boolean,
    val rawPayloadHash: String,
    val createdAt: Long,
    val updatedAt: Long
)