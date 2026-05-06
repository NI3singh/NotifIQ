package com.notifiq.core.model

enum class ClassificationLabel {
    IMPORTANT,
    USEFUL,
    NORMAL,
    LOW_VALUE,
    SPAM,
    UNKNOWN;

    companion object {
        fun fromScore(score: Double): ClassificationLabel {
            return when {
                score >= 0.80 -> IMPORTANT
                score >= 0.65 -> USEFUL
                score >= 0.40 -> NORMAL
                score >= 0.20 -> LOW_VALUE
                score > 0.0 -> SPAM
                else -> UNKNOWN
            }
        }
    }
}