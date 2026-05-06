package com.notifiq.core.model

data class FeedbackRecord(
    val id: String,
    val notificationId: String,
    val packageName: String,
    val feedbackType: FeedbackType,
    val createdAt: Long
)