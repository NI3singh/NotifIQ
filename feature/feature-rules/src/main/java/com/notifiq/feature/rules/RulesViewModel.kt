package com.notifiq.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifiq.core.database.dao.AppPreferenceDao
import com.notifiq.core.database.dao.RuleDao
import com.notifiq.core.database.dao.SenderDao
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.RuleEntity
import com.notifiq.core.database.entity.SenderEntity
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.RuleAction
import com.notifiq.core.model.RuleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RulesUiState(
    val isLoading: Boolean = true,
    // App preferences
    val allowlistedApps: List<AppPreferenceEntity> = emptyList(),
    val blocklistedApps: List<AppPreferenceEntity> = emptyList(),
    // Senders
    val allowlistedSenders: List<SenderEntity> = emptyList(),
    val blocklistedSenders: List<SenderEntity> = emptyList(),
    // Keywords
    val mutedKeywords: List<RuleEntity> = emptyList(),
    val protectedKeywords: List<RuleEntity> = emptyList(),
    // Settings from UserPreferenceDataStore
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
    val focusModeEnabled: Boolean = false,
    // Search
    val searchQuery: String = ""
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val appPreferenceDao: AppPreferenceDao,
    private val senderDao: SenderDao,
    private val ruleDao: RuleDao,
    private val userPreferenceDataStore: UserPreferenceDataStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            // Combine all flows
            combine(
                appPreferenceDao.getAllowlistedApps(),
                appPreferenceDao.getBlocklistedApps(),
                senderDao.getAllowlisted(),
                senderDao.getBlocklisted(),
                ruleDao.getAll(),
                userPreferenceDataStore.userPreference
            ) { allowlistedApps, blocklistedApps, allowlistedSenders, blocklistedSenders, allRules, userPref ->
                // Filter keyword rules
                val mutedRules = allRules.filter {
                    it.type == RuleType.KEYWORD.name &&
                    (it.action == RuleAction.PENALIZE.name || it.action == RuleAction.BLOCK.name)
                }
                val protectedRules = allRules.filter {
                    it.type == RuleType.KEYWORD.name && it.action == RuleAction.PROTECT.name
                }

                RulesUiState(
                    isLoading = false,
                    allowlistedApps = allowlistedApps,
                    blocklistedApps = blocklistedApps,
                    allowlistedSenders = allowlistedSenders,
                    blocklistedSenders = blocklistedSenders,
                    mutedKeywords = mutedRules,
                    protectedKeywords = protectedRules,
                    quietHoursEnabled = userPref.quietHoursEnabled,
                    quietHoursStart = userPref.quietHoursStart,
                    quietHoursEnd = userPref.quietHoursEnd,
                    focusModeEnabled = userPref.focusModeEnabled,
                    searchQuery = _searchQuery.value
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAddAllowlistApp(packageName: String, appName: String) {
        viewModelScope.launch {
            val existing = appPreferenceDao.getByPackage(packageName)
            if (existing != null) {
                appPreferenceDao.insert(
                    existing.copy(
                        isAllowlisted = true,
                        isBlocklisted = false,
                        trustScore = 0.8,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                appPreferenceDao.insert(
                    AppPreferenceEntity(
                        id = UUID.randomUUID().toString(),
                        packageName = packageName,
                        appName = appName,
                        isAllowlisted = true,
                        isBlocklisted = false,
                        trustScore = 0.8,
                        totalNotifications = 0,
                        importantCount = 0,
                        spamCount = 0,
                        isMuted = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun onAddBlocklistApp(packageName: String, appName: String) {
        viewModelScope.launch {
            val existing = appPreferenceDao.getByPackage(packageName)
            if (existing != null) {
                appPreferenceDao.insert(
                    existing.copy(
                        isAllowlisted = false,
                        isBlocklisted = true,
                        trustScore = 0.1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                appPreferenceDao.insert(
                    AppPreferenceEntity(
                        id = UUID.randomUUID().toString(),
                        packageName = packageName,
                        appName = appName,
                        isAllowlisted = false,
                        isBlocklisted = true,
                        trustScore = 0.1,
                        totalNotifications = 0,
                        importantCount = 0,
                        spamCount = 0,
                        isMuted = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun onRemoveAppRule(packageName: String) {
        viewModelScope.launch {
            val existing = appPreferenceDao.getByPackage(packageName) ?: return@launch
            appPreferenceDao.insert(
                existing.copy(
                    isAllowlisted = false,
                    isBlocklisted = false,
                    trustScore = 0.5,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun onAddAllowlistSender(displayName: String) {
        viewModelScope.launch {
            senderDao.insert(
                SenderEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    identifier = displayName.lowercase(),
                    isAllowlisted = true,
                    isBlocklisted = false,
                    trustScore = 0.8,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun onAddBlocklistSender(displayName: String) {
        viewModelScope.launch {
            senderDao.insert(
                SenderEntity(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    identifier = displayName.lowercase(),
                    isAllowlisted = false,
                    isBlocklisted = true,
                    trustScore = 0.1,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun onRemoveSenderRule(id: String) {
        viewModelScope.launch {
            senderDao.delete(id)
        }
    }

    fun onAddMutedKeyword(keyword: String) {
        viewModelScope.launch {
            ruleDao.insert(
                RuleEntity(
                    id = UUID.randomUUID().toString(),
                    type = RuleType.KEYWORD.name,
                    conditionValue = keyword.lowercase(),
                    action = RuleAction.PENALIZE.name,
                    weight = -0.15,
                    priority = 50,
                    isUserCreated = true,
                    isActive = true,
                    hitCount = 0,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun onAddProtectedKeyword(keyword: String) {
        viewModelScope.launch {
            ruleDao.insert(
                RuleEntity(
                    id = UUID.randomUUID().toString(),
                    type = RuleType.KEYWORD.name,
                    conditionValue = keyword.lowercase(),
                    action = RuleAction.PROTECT.name,
                    weight = 0.2,
                    priority = 100,
                    isUserCreated = true,
                    isActive = true,
                    hitCount = 0,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun onRemoveKeywordRule(id: String) {
        viewModelScope.launch {
            ruleDao.delete(id)
        }
    }

    fun onToggleQuietHours(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceDataStore.setQuietHoursEnabled(enabled)
        }
    }

    fun onToggleFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferenceDataStore.setFocusModeEnabled(enabled)
        }
    }

    fun onUpdateQuietHours(start: Int, end: Int) {
        viewModelScope.launch {
            userPreferenceDataStore.setQuietHoursStart(start)
            userPreferenceDataStore.setQuietHoursEnd(end)
        }
    }
}