package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_rules")
data class RuleEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "condition_value")
    val conditionValue: String,
    @ColumnInfo(name = "action")
    val action: String,
    @ColumnInfo(name = "weight")
    val weight: Double,
    @ColumnInfo(name = "priority")
    val priority: Int,
    @ColumnInfo(name = "is_user_created", defaultValue = "0")
    val isUserCreated: Boolean = false,
    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,
    @ColumnInfo(name = "hit_count", defaultValue = "0")
    val hitCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)