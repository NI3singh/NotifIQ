package com.notifiq.classification

import com.notifiq.core.common.Constants
import com.notifiq.core.model.ClassificationLabel

class SafetyGuard {
    fun canSuppress(context: ScoringContext): Boolean {
        // Never suppress OTP messages
        if (isOtpMessage(context.title, context.text)) {
            return false
        }

        // Never suppress banking app notifications
        if (isBankingApp(context.packageName)) {
            return false
        }

        // Never suppress safety-critical categories
        if (context.category in Constants.SAFETY_CATEGORIES) {
            return false
        }

        // Never suppress high importance notifications
        if (context.importance >= Constants.HIGH_IMPORTANCE_THRESHOLD) {
            return false
        }

        return true
    }

    private fun isOtpMessage(title: String, text: String): Boolean {
        val combined = "$title $text".lowercase()
        return Constants.OTP_PATTERN.containsMatchIn(combined)
    }

    private fun isBankingApp(packageName: String): Boolean {
        val bankingKeywords = listOf("bank", "banking", "sbi", "icici", "hdfc", "axis", "kotak", "paytm", "phonepe", "upi")
        return bankingKeywords.any { packageName.lowercase().contains(it) }
    }
}