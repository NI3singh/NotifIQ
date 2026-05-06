package com.notifiq.feature.priority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PriorityUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationRecord> = emptyList(),
    val pinnedSources: List<AppSourceCount> = emptyList(),
    val count: Int = 0
)

data class AppSourceCount(
    val packageName: String,
    val appName: String,
    val count: Int
)

@HiltViewModel
class PriorityViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriorityUiState())
    val uiState: StateFlow<PriorityUiState> = _uiState.asStateFlow()

    init {
        loadPriorityNotifications()
    }

    private fun loadPriorityNotifications() {
        viewModelScope.launch {
            notificationDao.getPriorityNotifications().collect { notifications ->
                val records = notifications.map { entity ->
                    NotificationRecord(
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

                // Compute pinned sources (apps with important notifications)
                val sourceCounts = records
                    .groupBy { it.packageName }
                    .map { (pkg, notifs) ->
                        AppSourceCount(
                            packageName = pkg,
                            appName = notifs.firstOrNull()?.appName ?: pkg,
                            count = notifs.size
                        )
                    }
                    .sortedByDescending { it.count }
                    .take(10)

                _uiState.value = PriorityUiState(
                    isLoading = false,
                    notifications = records,
                    pinnedSources = sourceCounts,
                    count = records.size
                )
            }
        }
    }
}