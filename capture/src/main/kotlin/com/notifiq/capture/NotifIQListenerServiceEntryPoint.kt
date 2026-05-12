package com.notifiq.capture

import com.notifiq.classification.ClassificationEngine
import com.notifiq.classification.PolicyEngine
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.NotificationDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotifIQListenerServiceEntryPoint {

    fun notificationNormalizer(): NotificationNormalizer
    fun notificationDeduplicator(): NotificationDeduplicator
    fun classificationEngine(): ClassificationEngine
    fun policyEngine(): PolicyEngine
    fun notificationDao(): NotificationDao
    fun appPreferenceDao(): AppPreferenceDao
}