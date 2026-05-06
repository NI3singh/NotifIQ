package com.notifiq.core.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

class SeedDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        seedAppPreferences(db)
        seedSenderRules(db)
        seedClassificationRules(db)
    }

    private fun seedAppPreferences(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // Allowlisted apps
        val allowlisted = listOf(
            Pair("com.whatsapp", "WhatsApp"),
            Pair("com.android.phone", "Phone"),
            Pair("com.android.mms", "Messages"),
            Pair("com.google.android.calendar", "Google Calendar"),
            Pair("com.sbi.lotusflowerbanking", "SBI Bank"),
            Pair("com.csam.icici.bank.imobile", "ICICI Bank"),
            Pair("com.axis.mobile", "Axis Bank"),
            Pair("com.phonepe.app", "PhonePe"),
            Pair("com.google.android.apps.nbu.paisa.user", "Google Pay"),
            Pair("net.one97.paytm", "Paytm"),
            Pair("org.telegram.messenger", "Telegram")
        )

        allowlisted.forEach { (pkg, name) ->
            db.execSQL("""
                INSERT INTO app_preferences (id, package_name, app_name, is_allowlisted, is_blocklisted, trust_score, total_notifications, important_count, spam_count, is_muted, created_at, updated_at)
                VALUES (?, ?, ?, 1, 0, 0.8, 0, 0, 0, 0, ?, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), pkg, name, now, now))
        }

        // Blocklisted apps
        val blocklisted = listOf(
            Pair("com.flipkart.android", "Flipkart"),
            Pair("com.myntra.android", "Myntra"),
            Pair("com.meesho.supply", "Meesho"),
            Pair("in.amazon.mShop.android.shopping", "Amazon"),
            Pair("com.snapdeal.main", "Snapdeal"),
            Pair("com.ajio.android", "Ajio")
        )

        blocklisted.forEach { (pkg, name) ->
            db.execSQL("""
                INSERT INTO app_preferences (id, package_name, app_name, is_allowlisted, is_blocklisted, trust_score, total_notifications, important_count, spam_count, is_muted, created_at, updated_at)
                VALUES (?, ?, ?, 0, 1, 0.1, 0, 0, 0, 0, ?, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), pkg, name, now, now))
        }
    }

    private fun seedSenderRules(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // Allowlisted senders
        listOf("Mom", "Priya Sharma").forEach { name ->
            db.execSQL("""
                INSERT INTO sender_rules (id, display_name, identifier, is_allowlisted, is_blocklisted, trust_score, created_at)
                VALUES (?, ?, ?, 1, 0, 0.8, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), name, name.lowercase(), now))
        }

        // Blocklisted senders
        listOf("Naukri Jobs", "Promotional SMS").forEach { name ->
            db.execSQL("""
                INSERT INTO sender_rules (id, display_name, identifier, is_allowlisted, is_blocklisted, trust_score, created_at)
                VALUES (?, ?, ?, 0, 1, 0.1, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), name, name.lowercase(), now))
        }
    }

    private fun seedClassificationRules(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // Muted keywords (PENALIZE)
        listOf("sale", "offer", "discount", "cashback", "limited time", "hurry", "exclusive").forEach { keyword ->
            db.execSQL("""
                INSERT INTO classification_rules (id, type, condition_value, action, weight, priority, is_user_created, is_active, hit_count, created_at)
                VALUES (?, 'KEYWORD', ?, 'PENALIZE', 0.7, 70, 0, 1, 0, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), keyword.lowercase(), now))
        }

        // Protected keywords (PROTECT)
        listOf("OTP", "payment", "bank", "emergency", "delivery", "flight").forEach { keyword ->
            db.execSQL("""
                INSERT INTO classification_rules (id, type, condition_value, action, weight, priority, is_user_created, is_active, hit_count, created_at)
                VALUES (?, 'KEYWORD', ?, 'PROTECT', 0.9, 90, 0, 1, 0, ?)
            """.trimIndent(), arrayOf(UUID.randomUUID().toString(), keyword.lowercase(), now))
        }
    }
}