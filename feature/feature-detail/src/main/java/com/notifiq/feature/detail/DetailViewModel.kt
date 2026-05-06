package com.notifiq.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.FeedbackDao
import com.notifiq.core.database.dao.NotificationDao
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.FeedbackEntity
import com.notifiq.core.database.mapper.toDomainModel
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.FeedbackType
import com.notifiq.core.model.NotificationRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = true,
    val notification: NotificationRecord? = null
)

sealed class DetailEvent {
    data class FeedbackGiven(val message: String) : DetailEvent()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val feedbackDao: FeedbackDao,
    private val appPreferenceDao: AppPreferenceDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val notificationId: String = savedStateHandle.get<String>("notificationId") ?: ""

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    init {
        loadNotification()
    }

    private fun loadNotification() {
        viewModelScope.launch {
            val entity = notificationDao.getById(notificationId)
            if (entity != null) {
                // Mark as read
                notificationDao.markRead(notificationId)

                _uiState.value = DetailUiState(
                    isLoading = false,
                    notification = entity.toDomainModel()
                )
            } else {
                _uiState.value = DetailUiState(isLoading = false)
            }
        }
    }

    fun onMarkImportant() {
        viewModelScope.launch {
            val notification = _uiState.value.notification ?: return@launch
            giveFeedback(notification, FeedbackType.MARK_IMPORTANT, ClassificationLabel.IMPORTANT, 1.0f)
            _events.emit(DetailEvent.FeedbackGiven("Marked as Important"))
        }
    }

    fun onMarkUseful() {
        viewModelScope.launch {
            val notification = _uiState.value.notification ?: return@launch
            giveFeedback(notification, FeedbackType.MARK_USEFUL, ClassificationLabel.USEFUL, 0.75f)
            _events.emit(DetailEvent.FeedbackGiven("Marked as Useful"))
        }
    }

    fun onMarkSpam() {
        viewModelScope.launch {
            val notification = _uiState.value.notification ?: return@launch
            giveFeedback(notification, FeedbackType.MARK_SPAM, ClassificationLabel.SPAM, 0.0f)
            _events.emit(DetailEvent.FeedbackGiven("Marked as Spam"))
        }
    }

    fun onArchive() {
        viewModelScope.launch {
            notificationDao.archive(notificationId)
            _events.emit(DetailEvent.FeedbackGiven("Archived"))
        }
    }

    fun onWhitelistApp() {
        viewModelScope.launch {
            val notification = _uiState.value.notification ?: return@launch
            val existing = appPreferenceDao.getByPackage(notification.packageName)
            if (existing != null) {
                appPreferenceDao.setAllowlisted(notification.packageName, true, System.currentTimeMillis())
            } else {
                appPreferenceDao.insert(
                    AppPreferenceEntity(
                        id = notification.packageName,
                        packageName = notification.packageName,
                        appName = notification.appName,
                        isAllowlisted = true,
                        isBlocklisted = false,
                        trustScore = 1.0,
                        totalNotifications = 0,
                        importantCount = 0,
                        spamCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            _events.emit(DetailEvent.FeedbackGiven("App whitelisted"))
        }
    }

    fun onMuteApp() {
        viewModelScope.launch {
            val notification = _uiState.value.notification ?: return@launch
            val existing = appPreferenceDao.getByPackage(notification.packageName)
            if (existing != null) {
                appPreferenceDao.setBlocklisted(notification.packageName, true, System.currentTimeMillis())
            } else {
                appPreferenceDao.insert(
                    AppPreferenceEntity(
                        id = notification.packageName,
                        packageName = notification.packageName,
                        appName = notification.appName,
                        isAllowlisted = false,
                        isBlocklisted = true,
                        trustScore = 0.0,
                        totalNotifications = 0,
                        importantCount = 0,
                        spamCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            _events.emit(DetailEvent.FeedbackGiven("App muted"))
        }
    }

    private suspend fun giveFeedback(
        notification: NotificationRecord,
        feedbackType: FeedbackType,
        label: ClassificationLabel,
        score: Float
    ) {
        // Insert feedback
        val feedback = FeedbackEntity(
            id = System.currentTimeMillis().toString(),
            notificationId = notification.id,
            feedbackType = feedbackType.name,
            packageName = notification.packageName,
            createdAt = System.currentTimeMillis()
        )
        feedbackDao.insert(feedback)

        // Update notification classification
        notificationDao.updateClassification(
            id = notification.id,
            label = label.name,
            score = score,
            updatedAt = System.currentTimeMillis()
        )

        // Update app trust score
        val appPref = appPreferenceDao.getByPackage(notification.packageName)
        if (appPref != null) {
            val newTrust = when (label) {
                ClassificationLabel.IMPORTANT, ClassificationLabel.USEFUL -> (appPref.trustScore + 0.1).coerceAtMost(1.0)
                ClassificationLabel.SPAM, ClassificationLabel.LOW_VALUE -> (appPref.trustScore - 0.1).coerceAtLeast(0.0)
                else -> appPref.trustScore
            }
            appPreferenceDao.updateTrustScore(notification.packageName, newTrust, System.currentTimeMillis())
        }
    }
}