package com.notifiq.classification

data class ScoringContext(
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String,
    val bigText: String,
    val channelId: String,
    val importance: Int,
    val postTime: Long,
    val category: String
)