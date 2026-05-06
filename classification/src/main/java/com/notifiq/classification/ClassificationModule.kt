package com.notifiq.classification

import com.notifiq.classification.scorer.AppReputationScorer
import com.notifiq.classification.scorer.ChannelImportanceScorer
import com.notifiq.classification.scorer.FrequencyScorer
import com.notifiq.classification.scorer.KeywordScorer
import com.notifiq.classification.scorer.ScoreAggregator
import com.notifiq.classification.scorer.TimeContextScorer
import com.notifiq.classification.scorer.UserFeedbackScorer
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClassificationModule {

    @Provides
    @Singleton
    fun provideKeywordScorer(ruleDao: RuleDao): KeywordScorer = KeywordScorer(ruleDao)

    @Provides
    @Singleton
    fun provideAppReputationScorer(): AppReputationScorer = AppReputationScorer()

    @Provides
    @Singleton
    fun provideFrequencyScorer(notificationDao: NotificationDao): FrequencyScorer = FrequencyScorer(notificationDao)

    @Provides
    @Singleton
    fun provideChannelImportanceScorer(): ChannelImportanceScorer = ChannelImportanceScorer()

    @Provides
    @Singleton
    fun provideTimeContextScorer(): TimeContextScorer = TimeContextScorer()

    @Provides
    @Singleton
    fun provideUserFeedbackScorer(
        appPreferenceDao: AppPreferenceDao,
        feedbackDao: FeedbackDao
    ): UserFeedbackScorer = UserFeedbackScorer(appPreferenceDao, feedbackDao)

    @Provides
    @Singleton
    fun provideScoreAggregator(
        userFeedbackScorer: UserFeedbackScorer,
        keywordScorer: KeywordScorer,
        appReputationScorer: AppReputationScorer,
        channelImportanceScorer: ChannelImportanceScorer,
        frequencyScorer: FrequencyScorer,
        timeContextScorer: TimeContextScorer
    ): ScoreAggregator = ScoreAggregator(
        userFeedbackScorer,
        keywordScorer,
        appReputationScorer,
        channelImportanceScorer,
        frequencyScorer,
        timeContextScorer
    )
}