package com.notifiq.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // The NotificationListenerService will be automatically started
            // by the system after boot if the user has granted permission
        }
    }
}