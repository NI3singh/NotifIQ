package com.notifiq.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.notifiq.worker.WorkManagerInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotifIQApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        WorkManagerInitializer.initialize(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Channel for daily/weekly summaries - low importance
            val summariesChannel = NotificationChannel(
                CHANNEL_SUMMARIES,
                "NotifIQ Summaries",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily and weekly intelligence summaries"
            }
            notificationManager.createNotificationChannel(summariesChannel)

            // Channel for system alerts - high importance
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "NotifIQ System",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important system alerts from NotifIQ (e.g., permission revoked warnings)"
            }
            notificationManager.createNotificationChannel(systemChannel)
        }
    }

    companion object {
        const val CHANNEL_SUMMARIES = "notifiq_summaries"
        const val CHANNEL_SYSTEM = "notifiq_system"
    }
}