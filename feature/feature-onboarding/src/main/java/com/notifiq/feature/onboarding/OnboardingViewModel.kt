package com.notifiq.feature.onboarding

import android.content.Context
import androidx.core.app.NotificationManagerCompat
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

    // FIX 5B: NotificationManager has no getEnabledNotificationListenerPackages() method.
    // The correct API is NotificationManagerCompat.getEnabledListenerPackages(context)
    // from androidx.core.app, available on all API levels NotifIQ supports (26+).
    fun isNotificationListenerEnabled(context: Context): Boolean {
        return NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
    }
}