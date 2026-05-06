package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_preferences",
    indices = [Index(value = ["package_name"], unique = true)]
)
data class AppPreferenceEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_name")
    val appName: String,
    @ColumnInfo(name = "is_allowlisted", defaultValue = "0")
    val isAllowlisted: Boolean = false,
    @ColumnInfo(name = "is_blocklisted", defaultValue = "0")
    val isBlocklisted: Boolean = false,
    @ColumnInfo(name = "trust_score", defaultValue = "0.5")
    val trustScore: Double = 0.5,
    @ColumnInfo(name = "total_notifications", defaultValue = "0")
    val totalNotifications: Int = 0,
    @ColumnInfo(name = "important_count", defaultValue = "0")
    val importantCount: Int = 0,
    @ColumnInfo(name = "spam_count", defaultValue = "0")
    val spamCount: Int = 0,
    @ColumnInfo(name = "is_muted", defaultValue = "0")
    val isMuted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)