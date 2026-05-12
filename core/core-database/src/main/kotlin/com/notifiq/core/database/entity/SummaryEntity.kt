package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "total_count")
    val totalCount: Int,
    @ColumnInfo(name = "important_count")
    val importantCount: Int,
    @ColumnInfo(name = "useful_count")
    val usefulCount: Int,
    @ColumnInfo(name = "spam_count")
    val spamCount: Int,
    @ColumnInfo(name = "suppressed_count")
    val suppressedCount: Int,
    @ColumnInfo(name = "noise_reduction_percent")
    val noiseReductionPercent: Float,
    @ColumnInfo(name = "top_noisy_apps_json")
    val topNoisyAppsJson: String,
    @ColumnInfo(name = "top_important_apps_json")
    val topImportantAppsJson: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)