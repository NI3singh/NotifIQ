package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feedback",
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["notification_id"])
    ]
)
data class FeedbackEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "notification_id")
    val notificationId: String,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "feedback_type")
    val feedbackType: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)