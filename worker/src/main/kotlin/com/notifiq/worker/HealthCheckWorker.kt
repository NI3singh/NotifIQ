package com.notifiq.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted private val context: android.content.Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if notification listener is still enabled
            if (!isNotificationListenerEnabled()) {
                postNotificationAccessWarning()
            }

            // Check database file size
            checkDatabaseSize()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        val myPackage = context.packageName
        return enabledListeners.contains(myPackage)
    }

    private fun postNotificationAccessWarning() {
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open notification listener settings
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "system")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Notification access disabled")
            .setContentText("NotifIQ cannot capture notifications. Tap to fix.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun checkDatabaseSize() {
        try {
            val databasePath = context.getDatabasePath(DATABASE_NAME)
            if (databasePath.exists()) {
                val fileSizeBytes = databasePath.length()
                val fileSizeMB = fileSizeBytes / (1024 * 1024)

                if (fileSizeMB > MAX_DATABASE_SIZE_MB) {
                    Log.w(TAG, "Database size warning: ${fileSizeMB.toInt()} MB exceeds ${MAX_DATABASE_SIZE_MB} MB limit")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check database size", e)
        }
    }

    companion object {
        const val TAG = "HealthCheckWorker"
        const val NOTIFICATION_ID = 2001
        const val DATABASE_NAME = "notifiq_database"
        const val MAX_DATABASE_SIZE_MB = 100
    }
}