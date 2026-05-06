package com.notifiq.classification

import android.app.NotificationManager
import com.notifiq.core.common.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafetyGuard @Inject constructor() {

    fun isSafetyProtected(
        packageName: String,
        text: String,
        bigText: String,
        category: String?,
        importance: Int
    ): Boolean {
        // Check OTP in text or bigText
        if (containsOtp(text) || containsOtp(bigText)) {
            return true
        }

        // Check protected packages
        if (Constants.PROTECTED_APP_PACKAGES.contains(packageName)) {
            return true
        }

        // Check safety-critical categories
        if (category != null) {
            val upperCategory = category.uppercase()
            if (Constants.SAFETY_CATEGORIES.contains(upperCategory)) {
                return true
            }
        }

        // Check high importance
        if (importance >= NotificationManager.IMPORTANCE_HIGH) {
            return true
        }

        return false
    }

    private fun containsOtp(text: String): Boolean {
        if (text.isBlank()) return false

        val lowerText = text.lowercase()

        // Pattern 1: 4-8 digit number followed by otp/code/verify/verification
        val pattern1 = Regex("\\b\\d{4,8}\\b.*(?:otp|code|verify|verification)", RegexOption.IGNORE_CASE)
        if (pattern1.containsMatchIn(lowerText)) return true

        // Pattern 2: otp/code/verify/verification followed by 4-8 digit number
        val pattern2 = Regex("(?:otp|code|verify|verification).*\\b\\d{4,8}\\b", RegexOption.IGNORE_CASE)
        if (pattern2.containsMatchIn(lowerText)) return true

        // Pattern 3: "one-time password" or "one time password"
        if (lowerText.contains("one-time password") || lowerText.contains("one time password")) {
            return true
        }

        // Pattern 4: "your otp is" or "your code is"
        val pattern4 = Regex("your (?:otp|code) is", RegexOption.IGNORE_CASE)
        if (pattern4.containsMatchIn(lowerText)) return true

        return false
    }
}