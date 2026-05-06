package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["classification_label"]),
        Index(value = ["post_time"]),
        Index(value = ["is_read"]),
        Index(value = ["is_suppressed"]),
        Index(value = ["created_at"]),
        Index(value = ["raw_payload_hash"], unique = true)
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_name")
    val appName: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "sub_text")
    val subText: String,
    @ColumnInfo(name = "big_text")
    val bigText: String,
    @ColumnInfo(name = "post_time")
    val postTime: Long,
    @ColumnInfo(name = "channel_id")
    val channelId: String,
    @ColumnInfo(name = "channel_name")
    val channelName: String,
    @ColumnInfo(name = "group_key")
    val groupKey: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "priority")
    val priority: Int,
    @ColumnInfo(name = "importance")
    val importance: Int,
    @ColumnInfo(name = "classification_label")
    val classificationLabel: String,
    @ColumnInfo(name = "classification_score")
    val classificationScore: Float,
    @ColumnInfo(name = "classification_reasons")
    val classificationReasons: String,
    @ColumnInfo(name = "action")
    val action: String,
    @ColumnInfo(name = "is_read", defaultValue = "0")
    val isRead: Boolean = false,
    @ColumnInfo(name = "is_suppressed", defaultValue = "0")
    val isSuppressed: Boolean = false,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "raw_payload_hash")
    val rawPayloadHash: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)