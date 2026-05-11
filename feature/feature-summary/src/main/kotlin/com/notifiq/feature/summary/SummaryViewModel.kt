package com.notifiq.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.common.DateTimeUtils
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.dao.SummaryDao
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.SummaryReport
import com.notifiq.core.model.SummaryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SummaryUiState(
    val isLoading: Boolean = true,
    val dailySummary: SummaryReport? = null,
    val weeklySummary: SummaryReport? = null,
    val liveSummary: SummaryReport? = null,
    val hasAnySummary: Boolean = false
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryDao: SummaryDao,
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummaries()
    }

    private fun loadSummaries() {
        viewModelScope.launch {
            // Load stored summaries
            val dailyEntity = summaryDao.getLatest("DAILY")
            val weeklyEntity = summaryDao.getLatest("WEEKLY")

            val dailySummary = dailyEntity?.toSummaryReport()
            val weeklySummary = weeklyEntity?.toSummaryReport()

            // Compute live summary if no stored summaries exist
            val liveSummary = computeLiveSummary()

            _uiState.value = SummaryUiState(
                isLoading = false,
                dailySummary = dailySummary,
                weeklySummary = weeklySummary,
                liveSummary = liveSummary,
                hasAnySummary = dailySummary != null || weeklySummary != null
            )
        }
    }

    private suspend fun computeLiveSummary(): SummaryReport? {
        val todayStart = DateTimeUtils.todayStartMillis()
        val todayEnd = DateTimeUtils.todayEndMillis()

        val todayNotifications = notificationDao.getNotificationsBetween(todayStart, todayEnd)

        if (todayNotifications.isEmpty()) {
            return null
        }

        val todayImportant = todayNotifications.count {
            it.classificationLabel == ClassificationLabel.IMPORTANT.name
        }
        val todayUseful = todayNotifications.count {
            it.classificationLabel == ClassificationLabel.USEFUL.name
        }
        val todaySpam = todayNotifications.count {
            it.classificationLabel == ClassificationLabel.SPAM.name
        }
        val todaySuppressed = todayNotifications.count { it.isSuppressed }

        // Noise reduction = (spam + suppressed) / total
        val noiseReduction = if (todayNotifications.isNotEmpty()) {
            ((todaySpam + todaySuppressed).toFloat() / todayNotifications.size) * 100f
        } else 0f

        return SummaryReport(
            id = "live",
            type = SummaryType.DAILY,
            title = "Live Summary",
            totalCount = todayNotifications.size,
            importantCount = todayImportant,
            usefulCount = todayUseful,
            spamCount = todaySpam,
            suppressedCount = todaySuppressed,
            noiseReductionPercent = noiseReduction,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun com.notifiq.core.database.entity.SummaryEntity.toSummaryReport(): SummaryReport {
        return SummaryReport(
            id = id,
            type = SummaryType.valueOf(type),
            title = title,
            totalCount = totalCount,
            importantCount = importantCount,
            usefulCount = usefulCount,
            spamCount = spamCount,
            suppressedCount = suppressedCount,
            noiseReductionPercent = noiseReductionPercent,
            createdAt = createdAt
        )
    }
}