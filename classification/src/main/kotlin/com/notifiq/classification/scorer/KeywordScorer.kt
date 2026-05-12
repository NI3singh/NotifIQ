package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.RuleAction
import com.notifiq.core.database.dao.RuleDao
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class KeywordScorer @Inject constructor(
    private val ruleDao: RuleDao
) : IScorer {

    override fun score(context: ScoringContext): ScoringResult {
        val combinedText = "${context.title} ${context.text} ${context.bigText}".lowercase()
        val reasons = mutableListOf<String>()
        var totalDelta = 0.0

        // OTP keywords - highest priority, hard override
        val matchedOtp = OTP_KEYWORDS.firstOrNull { combinedText.contains(it) }
        if (matchedOtp != null) {
            return ScoringResult(
                delta = 0.46,
                reasons = listOf("Contains OTP keyword"),
                isHardOverride = true,
                overrideLabel = ClassificationLabel.IMPORTANT
            )
        }

        // Banking keywords
        val matchedBanking = BANKING_KEYWORDS.firstOrNull { combinedText.contains(it) }
        if (matchedBanking != null) {
            totalDelta = 0.38
            reasons.add("Banking keyword: $matchedBanking")
        }

        // Important keywords
        val matchedImportant = IMPORTANT_KEYWORDS.firstOrNull { combinedText.contains(it) }
        if (matchedImportant != null) {
            totalDelta = 0.35
            reasons.add("Important keyword: $matchedImportant")
        }

        // Delivery keywords
        val matchedDelivery = DELIVERY_KEYWORDS.firstOrNull { combinedText.contains(it) }
        if (matchedDelivery != null) {
            totalDelta = 0.24
            reasons.add("Delivery keyword: $matchedDelivery")
        }

        // Meeting/Calendar keywords
        val matchedMeeting = MEETING_KEYWORDS.firstOrNull { combinedText.contains(it) }
        if (matchedMeeting != null) {
            totalDelta = 0.20
            reasons.add("Meeting/Calendar keyword: $matchedMeeting")
        }

        // Promotional keywords (additive)
        val promoPenalty = calculatePromoPenalty(combinedText)
        if (promoPenalty != 0.0) {
            totalDelta += promoPenalty
            reasons.add("Promotional keyword detected")
        }

        // Check user-added keyword rules from database
        totalDelta += checkUserRules(combinedText, reasons)

        if (reasons.isEmpty()) {
            return ScoringResult(delta = 0.0, reasons = emptyList())
        }

        return ScoringResult(
            delta = totalDelta.coerceIn(-0.45, 0.50),
            reasons = reasons
        )
    }

    private fun checkUserRules(text: String, reasons: MutableList<String>): Double {
        return runBlocking {
            var extraDelta = 0.0

            try {
                val keywordRules = ruleDao.getByType("KEYWORD")
                for (rule in keywordRules) {
                    if (text.contains(rule.conditionValue.lowercase())) {
                        when (rule.action) {
                            "PROTECT" -> {
                                extraDelta += 0.30
                                reasons.add("User rule: PROTECT keyword '${rule.conditionValue}'")
                            }
                            "PENALIZE" -> {
                                extraDelta -= 0.15
                                reasons.add("User rule: PENALIZE keyword '${rule.conditionValue}'")
                            }
                            else -> { /* Other actions ignored */ }
                        }
                    }
                }
            } catch (e: Exception) {
                // If database lookup fails, continue without user rules
            }

            extraDelta
        }
    }

    private fun calculatePromoPenalty(text: String): Double {
        var penalty = 0.0
        var hitCount = 0
        for (keyword in PROMOTIONAL_KEYWORDS) {
            if (text.contains(keyword) && hitCount < 3) {
                penalty -= 0.12
                hitCount++
            }
        }
        return penalty
    }

    companion object {
        val OTP_KEYWORDS = setOf(
            "otp", "verification code", "one-time password", "one time password",
            "2fa", "two-factor", "authentication code", "security code",
            "login code", "verify your", "confirm your identity"
        )

        val BANKING_KEYWORDS = setOf(
            "credited", "debited", "payment received", "payment failed",
            "upi", "neft", "imps", "balance", "transaction", "transfer",
            "emi due", "loan", "account statement", "insufficient balance"
        )

        val IMPORTANT_KEYWORDS = setOf(
            "emergency", "urgent", "missed call", "interview", "offer letter",
            "boarding pass", "flight", "doctor", "appointment", "hospital",
            "earthquake", "flood", "amber alert", "security breach",
            "unauthorized access", "password changed"
        )

        val DELIVERY_KEYWORDS = setOf(
            "out for delivery", "dispatched", "shipped", "arriving today",
            "delivered", "delivery attempt", "in transit", "tracking",
            "order confirmed"
        )

        val MEETING_KEYWORDS = setOf(
            "meeting", "standup", "reminder", "starts soon", "starts in",
            "calendar", "scheduled"
        )

        val PROMOTIONAL_KEYWORDS = setOf(
            "sale", "offer", "discount", "cashback", "coupon", "deal",
            "limited time", "flash sale", "hurry", "exclusive", "special price",
            "buy now", "shop now", "% off", "free delivery", "order now",
            "don't miss", "last chance", "ending soon", "best price",
            "lowest price", "clearance", "mega", "bonanza", "jackpot",
            "reward points", "earn coins", "scratch card", "spin the wheel",
            "lucky draw", "refer and earn", "invite friends", "share and win",
            "flat off", "click here", "act now", "win big"
        )
    }
}