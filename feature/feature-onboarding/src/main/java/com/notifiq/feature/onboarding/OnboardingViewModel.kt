package com.notifiq.feature.onboarding

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.datastore.UserPreferenceDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceDataStore: UserPreferenceDataStore
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            preferenceDataStore.setOnboardingComplete()
        }
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        // On API 33+ use the proper method
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val enabledPackages = nm.getEnabledNotificationListenerPackages()
                enabledPackages.contains(context.packageName)
            } catch (e: Exception) {
                false
            }
        } else {
            // For older versions, we can't reliably check
            // Default to false, user must verify manually
            false
        }
    }
}