package com.notifiq.feature.priority

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.mapper.toDomainModel
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
            notificationDao.getPriorityNotifications().collect { entities ->
                // FIX 4B: The old code manually constructed NotificationRecord and set
                //   classificationReasons = entity.classificationReasons
                // but entity.classificationReasons is a raw JSON String while
                // NotificationRecord.classificationReasons expects List<String>.
                // Using toDomainModel() from EntityMappers handles the JSON decoding correctly,
                // matching exactly how HomeViewModel and InboxViewModel already do it.
                val records = entities.map { it.toDomainModel() }

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