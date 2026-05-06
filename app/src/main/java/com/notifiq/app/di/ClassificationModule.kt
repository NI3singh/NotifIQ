package com.notifiq.app.di

import com.notifiq.classification.ClassificationEngine
import com.notifiq.classification.PolicyEngine
import com.notifiq.classification.SafetyGuard
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
import com.notifiq.core.datastore.UserPreferenceDataStore
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
    fun provideKeywordScorer(
        ruleDao: com.notifiq.core.database.dao.RuleDao
    ): KeywordScorer = KeywordScorer(ruleDao)

    @Provides
    @Singleton
    fun provideAppReputationScorer(): AppReputationScorer = AppReputationScorer()

    @Provides
    @Singleton
    fun provideFrequencyScorer(
        notificationDao: NotificationDao
    ): FrequencyScorer = FrequencyScorer(notificationDao)

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

    @Provides
    @Singleton
    fun provideClassificationEngine(
        scoreAggregator: ScoreAggregator
    ): ClassificationEngine = ClassificationEngine(scoreAggregator)

    @Provides
    @Singleton
    fun provideSafetyGuard(): SafetyGuard = SafetyGuard()

    @Provides
    @Singleton
    fun providePolicyEngine(
        preferenceDataStore: UserPreferenceDataStore,
        safetyGuard: SafetyGuard
    ): PolicyEngine = PolicyEngine(preferenceDataStore, safetyGuard)
}