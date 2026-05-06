package com.notifiq.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.database.entity.AnalyticsDailyEntity
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.FeedbackEntity
import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.database.entity.RuleEntity
import com.notifiq.core.database.entity.SenderEntity
import com.notifiq.core.database.entity.SummaryEntity

@Database(
    entities = [
        NotificationEntity::class,
        FeedbackEntity::class,
        AppPreferenceEntity::class,
        SenderEntity::class,
        RuleEntity::class,
        AnalyticsDailyEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotifIQDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun senderDao(): SenderDao
    abstract fun ruleDao(): RuleDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun summaryDao(): SummaryDao
}