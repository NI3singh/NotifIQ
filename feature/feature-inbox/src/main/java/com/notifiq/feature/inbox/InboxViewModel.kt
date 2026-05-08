package com.notifiq.feature.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.mapper.toDomainModel
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

// FIX 5A: InboxUiState was declared in both InboxUiState.kt AND here, causing a redeclaration
// compile error. The canonical declaration lives here; InboxUiState.kt must be DELETED.
data class InboxUiState(
    val searchQuery: String = "",
    val selectedFilter: ClassificationLabel? = null,
    val showUnreadOnly: Boolean = false,
    val filterCounts: Map<ClassificationLabel, Int> = emptyMap()
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val feedbackDao: FeedbackDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow<ClassificationLabel?>(null)
    private val _showUnreadOnly = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedNotifications: Flow<PagingData<NotificationRecord>> = _searchQuery
        .flatMapLatest { query ->
            val filter = _selectedFilter.value
            val unreadOnly = _showUnreadOnly.value

            val pagingSource = when {
                query.isNotBlank() -> notificationDao.searchPaged(query)
                filter != null -> notificationDao.getByLabelPaged(filter.name)
                unreadOnly -> notificationDao.getUnreadPaged()
                else -> notificationDao.getNotificationsPaged()
            }

            Pager(
                config = PagingConfig(pageSize = 30, prefetchDistance = 10),
                pagingSourceFactory = { pagingSource }
            ).flow
        }
        .map { pagingData -> pagingData.map { entity -> entity.toDomainModel() } }
        .cachedIn(viewModelScope)

    init {
        loadFilterCounts()
    }

    private fun loadFilterCounts() {
        viewModelScope.launch {
            // FIX 3A dependency: getCountByLabel now exists in NotificationDao.
            val counts = ClassificationLabel.entries.associate { label ->
                label to notificationDao.getCountByLabel(label.name)
            }
            _uiState.value = _uiState.value.copy(filterCounts = counts)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterSelected(label: ClassificationLabel?) {
        _selectedFilter.value = label
        _uiState.value = _uiState.value.copy(selectedFilter = label)
    }

    fun onToggleUnread() {
        val newValue = !_showUnreadOnly.value
        _showUnreadOnly.value = newValue
        _uiState.value = _uiState.value.copy(showUnreadOnly = newValue)
    }

    fun onArchive(notificationId: String) {
        viewModelScope.launch {
            notificationDao.archive(notificationId)
        }
    }

    fun onUndoArchive(notificationId: String) {
        // Would need an un-archive query; no-op for now to avoid crash
    }

    fun onMarkImportant(notificationId: String) {
        viewModelScope.launch {
            notificationDao.updateClassification(
                id = notificationId,
                label = ClassificationLabel.IMPORTANT.name,
                score = 1.0f,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}