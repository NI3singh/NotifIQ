package com.notifiq.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.mapper.toDomainModel
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val importantCount: Int = 0,
    val usefulCount: Int = 0,
    val lowValueCount: Int = 0,
    val spamCount: Int = 0,
    val totalToday: Int = 0,
    val noiseReductionPercent: Float = 0f,
    val recentImportantNotifications: List<NotificationRecord> = emptyList(),
    val topNoisyApps: List<AppNotificationCount> = emptyList()
)

data class AppNotificationCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val userPreferenceDataStore: UserPreferenceDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val todayStart = DateTimeUtils.todayStartMillis()

            // Combine today's counts
            combine(
                notificationDao.getTodayCount(todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.IMPORTANT.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.USEFUL.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.LOW_VALUE.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.SPAM.name, todayStart)
            ) { total, important, useful, lowValue, spam ->
                val noiseReduction = if (total > 0) {
                    ((lowValue + spam).toFloat() / total.toFloat()) * 100f
                } else {
                    0f
                }

                HomeUiState(
                    isLoading = false,
                    totalToday = total,
                    importantCount = important,
                    usefulCount = useful,
                    lowValueCount = lowValue,
                    spamCount = spam,
                    noiseReductionPercent = noiseReduction
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        // Load recent important notifications
        viewModelScope.launch {
            notificationDao.getPriorityNotifications().collect { entities ->
                val recentRecords = entities.take(5).map { it.toDomainModel() }
                _uiState.value = _uiState.value.copy(recentImportantNotifications = recentRecords)
            }
        }

        // Load top noisy apps (by spam + lowValue count today)
        viewModelScope.launch {
            val todayNotifications = notificationDao.getNotificationsBetween(
                DateTimeUtils.todayStartMillis(),
                DateTimeUtils.todayEndMillis()
            )

            val noisyAppCounts = todayNotifications
                .filter {
                    it.classificationLabel == ClassificationLabel.LOW_VALUE.name ||
                    it.classificationLabel == ClassificationLabel.SPAM.name
                }
                .groupBy { it.packageName }
                .map { (pkg, notifs) ->
                    AppNotificationCount(
                        packageName = pkg,
                        appName = notifs.firstOrNull()?.appName ?: pkg,
                        count = notifs.size
                    )
                }
                .sortedByDescending { it.count }
                .take(5)

            _uiState.value = _uiState.value.copy(topNoisyApps = noisyAppCounts)
        }
    }

    fun dismissOemBanner() {
        viewModelScope.launch {
            userPreferenceDataStore.setOemBannerDismissed(true)
        }
    }
}