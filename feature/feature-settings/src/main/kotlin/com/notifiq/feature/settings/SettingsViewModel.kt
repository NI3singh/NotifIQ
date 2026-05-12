package com.notifiq.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.database.mapper.toDomainModel
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationRecord
import com.notifiq.core.model.SummaryReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userPreference: com.notifiq.core.model.UserPreference = com.notifiq.core.model.UserPreference(),
    val todayImportant: Int = 0,
    val todayUseful: Int = 0,
    val todaySuppressed: Int = 0,
    val todaySpam: Int = 0,
    val usefulPercentage: Float = 0f,
    val latestSummary: SummaryReport? = null
)

sealed class SettingsEvent {
    data object DataDeleted : SettingsEvent()
    data class DataExported(val json: String) : SettingsEvent()
    data class ShowMessage(val message: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferenceDataStore: UserPreferenceDataStore,
    private val notificationDao: NotificationDao,
    private val feedbackDao: FeedbackDao,
    private val appPreferenceDao: AppPreferenceDao,
    private val senderDao: SenderDao,
    private val ruleDao: RuleDao,
    private val analyticsDao: AnalyticsDao,
    private val summaryDao: SummaryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // FIX 7B: combine() with 6 named lambda parameters does not compile —
        // the overload only exists for up to 5 flows.
        // Fix: nest two valid combines (5-flow inner, 2-flow outer).
        viewModelScope.launch {
            val countFlows = combine(
                notificationDao.getTodayCount(todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.IMPORTANT.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.USEFUL.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.SPAM.name, todayStart),
                notificationDao.getSuppressedTodayCount(todayStart)
            ) { total, important, useful, spam, suppressed ->
                intArrayOf(total, important, useful, spam, suppressed)
            }

            combine(
                userPreferenceDataStore.userPreference,
                countFlows
            ) { prefs, counts ->
                val total      = counts[0]
                val important  = counts[1]
                val useful     = counts[2]
                val spam       = counts[3]
                val suppressed = counts[4]

                val usefulPct = if (total > 0) {
                    ((important + useful).toFloat() / total.toFloat()) * 100f
                } else 0f

                _uiState.value.copy(
                    isLoading = false,
                    userPreference = prefs,
                    todayImportant = important,
                    todayUseful = useful,
                    todaySuppressed = suppressed,
                    todaySpam = spam,
                    usefulPercentage = usefulPct
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        // FIX 4C: The old code accessed non-existent fields on SummaryEntity
        // (date, totalNotifications, normalCount, lowValueCount) and constructed
        // SummaryReport with a non-existent topApps parameter.
        // Fix: use the existing toDomainModel() mapper from EntityMappers which maps
        // the correct SummaryEntity fields to SummaryReport fields.
        viewModelScope.launch {
            val latest = summaryDao.getLatest("DAILY")
            if (latest != null) {
                _uiState.value = _uiState.value.copy(latestSummary = latest.toDomainModel())
            }
        }
    }

    fun onSetSuppressionEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferenceDataStore.setSuppressionEnabled(enabled) }
    }

    fun onSetLearningEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferenceDataStore.setLearningEnabled(enabled) }
    }

    fun onSetDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferenceDataStore.setDarkModeEnabled(enabled) }
    }

    fun onSetDataRetentionDays(days: Int) {
        viewModelScope.launch { userPreferenceDataStore.setDataRetentionDays(days) }
    }

    fun onSetSummaryFrequency(frequency: String) {
        viewModelScope.launch { userPreferenceDataStore.setSummaryFrequency(frequency) }
    }

    fun onDeleteAllData() {
        viewModelScope.launch {
            // Preserve a few display preferences before wiping
            val currentPrefs = userPreferenceDataStore.userPreference.first()

            notificationDao.deleteAll()
            feedbackDao.deleteAll()
            appPreferenceDao.deleteAll()
            senderDao.deleteAll()
            ruleDao.deleteAll()
            analyticsDao.deleteAll()
            summaryDao.deleteAll()

            // FIX 5D: UserPreferenceDataStore has no resetToDefaults() method.
            // Replace with explicit calls to each individual setter using default values
            // from UserPreference's default constructor.
            userPreferenceDataStore.setSuppressionEnabled(false)
            userPreferenceDataStore.setLearningEnabled(true)
            userPreferenceDataStore.setSummaryEnabled(true)
            userPreferenceDataStore.setSummaryFrequency("daily")
            userPreferenceDataStore.setQuietHoursEnabled(true)
            userPreferenceDataStore.setQuietHoursStart(23)
            userPreferenceDataStore.setQuietHoursEnd(7)
            userPreferenceDataStore.setFocusModeEnabled(false)
            userPreferenceDataStore.setDataRetentionDays(30)
            userPreferenceDataStore.setOemBannerDismissed(false)
            // Keep onboarding complete so the user isn't shown setup again
            userPreferenceDataStore.setOnboardingComplete()
            // Restore dark-mode preference so the theme doesn't unexpectedly flip
            userPreferenceDataStore.setDarkModeEnabled(currentPrefs.darkModeEnabled)

            _events.emit(SettingsEvent.DataDeleted)
        }
    }

    fun onExportData() {
        viewModelScope.launch {
            try {
                val notifications = notificationDao.getAllNotifications().first()
                // FIX: use toDomainModel() instead of manual construction to avoid
                // the classificationReasons String→List<String> mismatch.
                val records = notifications.map { it.toDomainModel() }

                val serializer = ListSerializer(NotificationRecord.serializer())
                val jsonString = Json.encodeToString(serializer, records)

                _events.emit(SettingsEvent.DataExported(jsonString))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }
}