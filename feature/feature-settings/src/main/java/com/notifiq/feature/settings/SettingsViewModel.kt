package com.notifiq.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.AnalyticsDao
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.DailyAnalytics
import com.notifiq.core.model.SummaryReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userPreference: com.notifiq.core.model.UserPreference = com.notifiq.core.model.UserPreference(),
    val todayImportant: Int = 0,
    val todayUseful: Int = 0,
    val todaySuppressed: Int = 0,
    val todaySpam: Int = 0,
    val usefulPercentage: Float = 0f,
    val latestSummary: SummaryReport? = null,
    val exportJson: String? = null
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
        viewModelScope.launch {
            val todayStart = DateTimeUtils.todayStartMillis()

            // Combine user preference and today's stats
            kotlinx.coroutines.flow.combine(
                userPreferenceDataStore.userPreference,
                notificationDao.getTodayCount(todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.IMPORTANT.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.USEFUL.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.SPAM.name, todayStart),
                notificationDao.getSuppressedTodayCount(todayStart)
            ) { prefs, total, important, useful, spam, suppressed ->
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

        // Load latest summary
        viewModelScope.launch {
            val latest = summaryDao.getLatest("DAILY")
            if (latest != null) {
                val summary = SummaryReport(
                    id = latest.id,
                    type = com.notifiq.core.model.SummaryType.valueOf(latest.type),
                    date = latest.date,
                    totalNotifications = latest.totalNotifications,
                    importantCount = latest.importantCount,
                    usefulCount = latest.usefulCount,
                    normalCount = latest.normalCount,
                    lowValueCount = latest.lowValueCount,
                    spamCount = latest.spamCount,
                    suppressedCount = latest.suppressedCount,
                    noiseReductionPercent = latest.noiseReductionPercent,
                    topApps = emptyList(), // Would need parsing if stored
                    createdAt = latest.createdAt
                )
                _uiState.value = _uiState.value.copy(latestSummary = summary)
            }
        }
    }

    fun onSetSuppressionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceDataStore.setSuppressionEnabled(enabled)
        }
    }

    fun onSetLearningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceDataStore.setLearningEnabled(enabled)
        }
    }

    fun onSetDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceDataStore.setDarkModeEnabled(enabled)
        }
    }

    fun onSetDataRetentionDays(days: Int) {
        viewModelScope.launch {
            userPreferenceDataStore.setDataRetentionDays(days)
        }
    }

    fun onSetSummaryFrequency(frequency: String) {
        viewModelScope.launch {
            userPreferenceDataStore.setSummaryFrequency(frequency)
        }
    }

    fun onDeleteAllData() {
        viewModelScope.launch {
            // Delete all data from all tables
            notificationDao.deleteAll()
            feedbackDao.deleteAll()
            appPreferenceDao.deleteAll()
            senderDao.deleteAll()
            ruleDao.deleteAll()
            analyticsDao.deleteAll()
            summaryDao.deleteAll()

            // Reset DataStore preferences (keep onboardingComplete and darkModeEnabled)
            val currentPrefs = userPreferenceDataStore.userPreference.first()
            userPreferenceDataStore.resetToDefaults()
            userPreferenceDataStore.setOnboardingComplete()
            userPreferenceDataStore.setDarkModeEnabled(currentPrefs.darkModeEnabled)

            _events.emit(SettingsEvent.DataDeleted)
        }
    }

    fun onExportData() {
        viewModelScope.launch {
            try {
                val notifications = notificationDao.getAllNotifications().first()
                val records = notifications.map { entity ->
                    com.notifiq.core.model.NotificationRecord(
                        id = entity.id,
                        key = entity.key,
                        packageName = entity.packageName,
                        appName = entity.appName,
                        title = entity.title,
                        text = entity.text,
                        subText = entity.subText,
                        bigText = entity.bigText,
                        postTime = entity.postTime,
                        channelId = entity.channelId,
                        channelName = entity.channelName,
                        groupKey = entity.groupKey,
                        category = entity.category,
                        priority = entity.priority,
                        importance = entity.importance,
                        classificationLabel = ClassificationLabel.valueOf(entity.classificationLabel),
                        classificationScore = entity.classificationScore,
                        classificationReasons = entity.classificationReasons,
                        action = com.notifiq.core.model.NotificationAction.valueOf(entity.action),
                        isRead = entity.isRead,
                        isSuppressed = entity.isSuppressed,
                        isArchived = entity.isArchived,
                        rawPayloadHash = entity.rawPayloadHash,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }

                val json = kotlinx.serialization.json.Json.encodeToString(
                    kotlinx.serialization.builtins.ListSerializer(com.notifiq.core.model.NotificationRecord.serializer()),
                    records
                )

                _events.emit(SettingsEvent.DataExported(json))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }
}