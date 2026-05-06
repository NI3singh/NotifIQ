package com.notifiq.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sender_rules")
data class SenderEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "identifier")
    val identifier: String,
    @ColumnInfo(name = "is_allowlisted", defaultValue = "0")
    val isAllowlisted: Boolean = false,
    @ColumnInfo(name = "is_blocklisted", defaultValue = "0")
    val isBlocklisted: Boolean = false,
    @ColumnInfo(name = "trust_score", defaultValue = "0.5")
    val trustScore: Double = 0.5,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)