package com.notifiq.classification.scorer

import com.notifiq.classification.IScorer
import com.notifiq.classification.ScoringContext
import com.notifiq.classification.ScoringResult
import com.notifiq.core.model.ClassificationLabel
import javax.inject.Inject

class AppReputationScorer @Inject constructor() : IScorer {

    override fun score(context: ScoringContext): ScoringResult {
        return when {
            // Protected packages - hard override to IMPORTANT
            PROTECTED_PACKAGES.contains(context.packageName) -> {
                ScoringResult(
                    delta = 0.30,
                    reasons = listOf("Protected system/banking app"),
                    isHardOverride = true,
                    overrideLabel = ClassificationLabel.IMPORTANT
                )
            }
            // Messaging packages - boost toward Useful
            MESSAGING_PACKAGES.contains(context.packageName) -> {
                ScoringResult(
                    delta = 0.22,
                    reasons = listOf("Messaging app")
                )
            }
            // Developer tool packages - boost toward Useful
            DEVELOPER_TOOL_PACKAGES.contains(context.packageName) -> {
                ScoringResult(
                    delta = 0.25,
                    reasons = listOf("Developer/Work tool")
                )
            }
            // Social media packages - slightly below normal
            SOCIAL_MEDIA_PACKAGES.contains(context.packageName) -> {
                ScoringResult(
                    delta = -0.04,
                    reasons = listOf("Social media app")
                )
            }
            // Noisy/promotional packages - penalize toward Low Value/Spam
            NOISY_PROMOTIONAL_PACKAGES.contains(context.packageName) -> {
                ScoringResult(
                    delta = -0.30,
                    reasons = listOf("Known promotional app")
                )
            }
            else -> ScoringResult(delta = 0.0, reasons = emptyList())
        }
    }

    companion object {
        val PROTECTED_PACKAGES = setOf(
            "com.android.phone",
            "com.android.mms",
            "com.android.dialer",
            "com.android.calendar",
            "com.google.android.calendar",
            "com.samsung.android.calendar",
            "com.google.android.apps.messaging",
            "com.sbi.lotusflowerbanking",
            "com.csam.icici.bank.imobile",
            "com.axis.mobile",
            "com.kotak.mobile.banking",
            "net.one97.paytm",
            "com.phonepe.app",
            "com.google.android.apps.nbu.paisa.user",
            "in.org.npci.upiapp"
        )

        val MESSAGING_PACKAGES = setOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.Slack",
            "com.discord",
            "org.thoughtcrime.securesms",
            "com.google.android.apps.tachyon"
        )

        val DEVELOPER_TOOL_PACKAGES = setOf(
            "com.github.android",
            "com.microsoft.teams",
            "com.atlassian.android.jira.core",
            "com.microsoft.office.outlook"
        )

        val SOCIAL_MEDIA_PACKAGES = setOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.linkedin.android",
            "com.reddit.frontpage",
            "com.snapchat.android"
        )

        val NOISY_PROMOTIONAL_PACKAGES = setOf(
            "com.flipkart.android",
            "com.myntra.android",
            "club.cred",
            "com.meesho.supply",
            "in.amazon.mShop.android.shopping",
            "com.snapdeal.main",
            "com.ajio.android",
            "com.application.zomato",
            "in.swiggy.android",
            "com.grofers.customerapp"
        )
    }
}