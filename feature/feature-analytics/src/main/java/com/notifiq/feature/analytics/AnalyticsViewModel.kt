package com.notifiq.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.mapper.toDomainModel
import com.notifiq.core.model.ClassificationLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val todayTotal: Int = 0,
    val todayImportant: Int = 0,
    val todayUseful: Int = 0,
    val todayNormal: Int = 0,
    val todayLowValue: Int = 0,
    val todaySpam: Int = 0,
    val todaySuppressed: Int = 0,
    val usefulRatioPercent: Float = 0f,
    val averageConfidence: Float = 0f,
    val weeklyData: List<DayData> = emptyList(),
    val todayBreakdown: List<LabelCount> = emptyList(),
    val noisiestApps: List<AppAnalyticsCount> = emptyList(),
    val mostImportantApps: List<AppAnalyticsCount> = emptyList()
)

data class DayData(
    val dayLabel: String,
    val count: Int,
    val isToday: Boolean
)

data class LabelCount(
    val label: ClassificationLabel,
    val count: Int,
    val percentage: Float
)

data class AppAnalyticsCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val todayStart = DateTimeUtils.todayStartMillis()

            combine(
                notificationDao.getTodayCount(todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.IMPORTANT.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.USEFUL.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.NORMAL.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.LOW_VALUE.name, todayStart),
                notificationDao.getTodayCountByLabel(ClassificationLabel.SPAM.name, todayStart)
            ) { values ->
                val total = values[0] as Int
                val important = values[1] as Int
                val useful = values[2] as Int
                val normal = values[3] as Int
                val lowValue = values[4] as Int
                val spam = values[5] as Int

                val usefulRatio = if (total > 0) {
                    ((important + useful).toFloat() / total.toFloat()) * 100f
                } else 0f

                Triple(total, usefulRatio, Unit)
            }.collect { (total, usefulRatio, _) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    todayTotal = total,
                    usefulRatioPercent = usefulRatio
                )
            }
        }

        viewModelScope.launch {
            // Load weekly data
            val weekAgo = DateTimeUtils.daysAgoMillis(7)
            val notifications = notificationDao.getNotificationsBetween(weekAgo, System.currentTimeMillis())
            val records = notifications.map { it.toDomainModel() }

            val weeklyData = (6 downTo 0).map { daysAgo ->
                val dateMillis = DateTimeUtils.daysAgoMillis(daysAgo)
                val dayEnd = dateMillis + (24 * 60 * 60 * 1000) - 1
                val count = records.count { it.postTime in dateMillis..dayEnd }
                val dayFormat = SimpleDateFormat("EEE", Locale.US)
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -daysAgo)
                }
                DayData(
                    dayLabel = dayFormat.format(calendar.time),
                    count = count,
                    isToday = daysAgo == 0
                )
            }

            _uiState.value = _uiState.value.copy(weeklyData = weeklyData)
        }

        viewModelScope.launch {
            val weekAgo = DateTimeUtils.daysAgoMillis(7)
            val notifications = notificationDao.getNotificationsBetween(weekAgo, System.currentTimeMillis())
            val records = notifications.map { it.toDomainModel() }

            // Noisiest apps
            val noisyApps = records
                .filter { it.classificationLabel == ClassificationLabel.LOW_VALUE || it.classificationLabel == ClassificationLabel.SPAM }
                .groupBy { it.packageName }
                .map { (pkg, recs) ->
                    AppAnalyticsCount(
                        packageName = pkg,
                        appName = recs.firstOrNull()?.appName ?: pkg,
                        count = recs.size
                    )
                }
                .sortedByDescending { it.count }
                .take(5)

            // Most important apps
            val importantApps = records
                .filter { it.classificationLabel == ClassificationLabel.IMPORTANT || it.classificationLabel == ClassificationLabel.USEFUL }
                .groupBy { it.packageName }
                .map { (pkg, recs) ->
                    AppAnalyticsCount(
                        packageName = pkg,
                        appName = recs.firstOrNull()?.appName ?: pkg,
                        count = recs.size
                    )
                }
                .sortedByDescending { it.count }
                .take(5)

            _uiState.value = _uiState.value.copy(
                noisiestApps = noisyApps,
                mostImportantApps = importantApps
            )
        }
    }
}