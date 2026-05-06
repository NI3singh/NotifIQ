package com.notifiq.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notifiq.core.model.UserPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferenceDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val SUPPRESSION_ENABLED = booleanPreferencesKey("suppression_enabled")
        val LEARNING_ENABLED = booleanPreferencesKey("learning_enabled")
        val SUMMARY_ENABLED = booleanPreferencesKey("summary_enabled")
        val SUMMARY_FREQUENCY = stringPreferencesKey("summary_frequency")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val FOCUS_MODE_ENABLED = booleanPreferencesKey("focus_mode_enabled")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val DATA_RETENTION_DAYS = intPreferencesKey("data_retention_days")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val OEM_BANNER_DISMISSED = booleanPreferencesKey("oem_banner_dismissed")
    }

    val userPreference: Flow<UserPreference> = context.dataStore.data.map { preferences ->
        UserPreference(
            suppressionEnabled = preferences[PreferencesKeys.SUPPRESSION_ENABLED] ?: false,
            learningEnabled = preferences[PreferencesKeys.LEARNING_ENABLED] ?: true,
            summaryEnabled = preferences[PreferencesKeys.SUMMARY_ENABLED] ?: true,
            summaryFrequency = preferences[PreferencesKeys.SUMMARY_FREQUENCY] ?: "daily",
            quietHoursEnabled = preferences[PreferencesKeys.QUIET_HOURS_ENABLED] ?: true,
            quietHoursStart = preferences[PreferencesKeys.QUIET_HOURS_START] ?: 23,
            quietHoursEnd = preferences[PreferencesKeys.QUIET_HOURS_END] ?: 7,
            focusModeEnabled = preferences[PreferencesKeys.FOCUS_MODE_ENABLED] ?: false,
            darkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: true,
            dataRetentionDays = preferences[PreferencesKeys.DATA_RETENTION_DAYS] ?: 30,
            hasCompletedOnboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
            oemBannerDismissed = preferences[PreferencesKeys.OEM_BANNER_DISMISSED] ?: false
        )
    }

    suspend fun setSuppressionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUPPRESSION_ENABLED] = enabled
        }
    }

    suspend fun setLearningEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LEARNING_ENABLED] = enabled
        }
    }

    suspend fun setSummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_ENABLED] = enabled
        }
    }

    suspend fun setSummaryFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_FREQUENCY] = frequency
        }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled
        }
    }

    suspend fun setQuietHoursStart(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_START] = hour
        }
    }

    suspend fun setQuietHoursEnd(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_END] = hour
        }
    }

    suspend fun setFocusModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_MODE_ENABLED] = enabled
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setDataRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_RETENTION_DAYS] = days
        }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETE] = true
        }
    }

    suspend fun setOemBannerDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OEM_BANNER_DISMISSED] = dismissed
        }
    }
}