package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analytics_daily",
    indices = [Index(value = ["date"], unique = true)]
)
data class AnalyticsDailyEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "total_notifications")
    val totalNotifications: Int,
    @ColumnInfo(name = "important_count")
    val importantCount: Int,
    @ColumnInfo(name = "useful_count")
    val usefulCount: Int,
    @ColumnInfo(name = "normal_count")
    val normalCount: Int,
    @ColumnInfo(name = "low_value_count")
    val lowValueCount: Int,
    @ColumnInfo(name = "spam_count")
    val spamCount: Int,
    @ColumnInfo(name = "suppressed_count")
    val suppressedCount: Int,
    @ColumnInfo(name = "average_confidence")
    val averageConfidence: Float,
    @ColumnInfo(name = "top_apps_json")
    val topAppsJson: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)