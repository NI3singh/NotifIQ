package com.notifiq.core.model

data class SenderRule(
    val id: String,
    val displayName: String,
    val identifier: String,
    val isAllowlisted: Boolean,
    val isBlocklisted: Boolean,
    val trustScore: Double = 0.5
)