package com.notifiq.core.model

data class ClassificationRule(
    val id: String,
    val type: RuleType,
    val condition: String,
    val action: RuleAction,
    val weight: Double,
    val priority: Int,
    val isUserCreated: Boolean,
    val isActive: Boolean,
    val hitCount: Int = 0,
    val createdAt: Long
)