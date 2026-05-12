package com.notifiq.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.notifiq.core.database.NotifIQDatabase
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.database.seed.SeedDatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNotifIQDatabase(
        @ApplicationContext context: Context
    ): NotifIQDatabase {
        return Room.databaseBuilder(
            context,
            NotifIQDatabase::class.java,
            "notifiq_database"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD)
            .addCallback(SeedDatabaseCallback())
            .build()
    }

    @Provides
    fun provideNotificationDao(database: NotifIQDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideFeedbackDao(database: NotifIQDatabase): FeedbackDao {
        return database.feedbackDao()
    }

    @Provides
    fun provideAppPreferenceDao(database: NotifIQDatabase): AppPreferenceDao {
        return database.appPreferenceDao()
    }

    @Provides
    fun provideSenderDao(database: NotifIQDatabase): SenderDao {
        return database.senderDao()
    }

    @Provides
    fun provideRuleDao(database: NotifIQDatabase): RuleDao {
        return database.ruleDao()
    }

    @Provides
    fun provideAnalyticsDao(database: NotifIQDatabase): AnalyticsDao {
        return database.analyticsDao()
    }

    @Provides
    fun provideSummaryDao(database: NotifIQDatabase): SummaryDao {
        return database.summaryDao()
    }
}