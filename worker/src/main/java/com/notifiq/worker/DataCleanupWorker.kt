package com.notifiq.worker

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.datastore.UserPreferenceDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted private val context: android.content.Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val analyticsDao: AnalyticsDao,
    private val userPreferenceDataStore: UserPreferenceDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Read data retention days from DataStore (default 30)
            val userPrefs = userPreferenceDataStore.userPreference.first()
            val retentionDays = userPrefs.dataRetentionDays

            // Calculate cutoff timestamp for notifications
            val cutoffTimestamp = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)

            // Delete old notification records
            notificationDao.deleteOlderThan(cutoffTimestamp)

            // Calculate analytics cutoff date string
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -retentionDays)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cutoffDate = dateFormat.format(calendar.time)

            // Delete old analytics entries
            analyticsDao.deleteOlderThan(cutoffDate)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}