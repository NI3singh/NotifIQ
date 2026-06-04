package com.notifiq.feature.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.AppIconResolver
import com.notifiq.core.designsystem.component.EditorialHeader
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: RulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddKeywordDialog by remember { mutableStateOf(false) }
    var showAddSenderDialog by remember { mutableStateOf(false) }
    var keywordType by remember { mutableStateOf("mute") } // "mute" or "protect"

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    EditorialHeader(title = "Rules", eyebrow = "Filters & protection")
                }

                // Search bar for filtering
                item {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        placeholder = "Search apps..."
                    )
                }

                // Settings Section - Quiet Hours & Focus Mode
                item {
                    SettingsSection(
                        quietHoursEnabled = uiState.quietHoursEnabled,
                        quietHoursStart = uiState.quietHoursStart,
                        quietHoursEnd = uiState.quietHoursEnd,
                        focusModeEnabled = uiState.focusModeEnabled,
                        onToggleQuietHours = viewModel::onToggleQuietHours,
                        onToggleFocusMode = viewModel::onToggleFocusMode
                    )
                }

                // Blocklisted Apps
                if (uiState.blocklistedApps.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Blocked Apps",
                            icon = Icons.Default.Block,
                            iconTint = Color(0xFFF87171)
                        )
                    }

                    val filteredApps = uiState.blocklistedApps.filter {
                        uiState.searchQuery.isBlank() ||
                        it.appName.contains(uiState.searchQuery, ignoreCase = true)
                    }

                    items(filteredApps, key = { it.packageName }) { app ->
                        AppRuleRow(
                            packageName = app.packageName,
                            appName = app.appName,
                            isBlocked = true,
                            onRemove = { viewModel.onRemoveAppRule(app.packageName) }
                        )
                    }
                }

                // Allowlisted Apps
                if (uiState.allowlistedApps.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Allowlisted Apps",
                            icon = Icons.Default.Shield,
                            iconTint = Color(0xFF34D399)
                        )
                    }

                    val filteredAllowlisted = uiState.allowlistedApps.filter {
                        uiState.searchQuery.isBlank() ||
                        it.appName.contains(uiState.searchQuery, ignoreCase = true)
                    }

                    items(filteredAllowlisted, key = { it.packageName }) { app ->
                        AppRuleRow(
                            packageName = app.packageName,
                            appName = app.appName,
                            isBlocked = false,
                            onRemove = { viewModel.onRemoveAppRule(app.packageName) }
                        )
                    }
                }

                // Muted Keywords
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Muted Keywords",
                        icon = Icons.Default.Tag,
                        iconTint = Color(0xFFFACC15),
                        onAdd = {
                            keywordType = "mute"
                            showAddKeywordDialog = true
                        }
                    )
                }

                if (uiState.mutedKeywords.isEmpty()) {
                    item {
                        Text(
                            text = "No muted keywords yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.mutedKeywords, key = { it.id }) { rule ->
                        KeywordRuleRow(
                            keyword = rule.conditionValue,
                            onRemove = { viewModel.onRemoveKeywordRule(rule.id) }
                        )
                    }
                }

                // Protected Keywords
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Protected Keywords",
                        icon = Icons.Default.Shield,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onAdd = {
                            keywordType = "protect"
                            showAddKeywordDialog = true
                        }
                    )
                }

                if (uiState.protectedKeywords.isEmpty()) {
                    item {
                        Text(
                            text = "No protected keywords yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.protectedKeywords, key = { it.id }) { rule ->
                        KeywordRuleRow(
                            keyword = rule.conditionValue,
                            onRemove = { viewModel.onRemoveKeywordRule(rule.id) }
                        )
                    }
                }

                // Blocklisted Senders
                if (uiState.blocklistedSenders.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Blocked Senders",
                            icon = Icons.Default.Block,
                            iconTint = Color(0xFFF87171)
                        )
                    }

                    items(uiState.blocklistedSenders, key = { it.id }) { sender ->
                        SenderRuleRow(
                            senderName = sender.displayName,
                            onRemove = { viewModel.onRemoveSenderRule(sender.id) }
                        )
                    }
                }

                // Allowlisted Senders
                if (uiState.allowlistedSenders.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Allowlisted Senders",
                            icon = Icons.Default.Shield,
                            iconTint = Color(0xFF34D399)
                        )
                    }

                    items(uiState.allowlistedSenders, key = { it.id }) { sender ->
                        SenderRuleRow(
                            senderName = sender.displayName,
                            onRemove = { viewModel.onRemoveSenderRule(sender.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Add Keyword Dialog
    if (showAddKeywordDialog) {
        var keywordInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddKeywordDialog = false },
            title = { Text(if (keywordType == "mute") "Add Muted Keyword" else "Add Protected Keyword") },
            text = {
                OutlinedTextField(
                    value = keywordInput,
                    onValueChange = { keywordInput = it },
                    label = { Text("Keyword") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (keywordInput.isNotBlank()) {
                            if (keywordType == "mute") {
                                viewModel.onAddMutedKeyword(keywordInput.trim())
                            } else {
                                viewModel.onAddProtectedKeyword(keywordInput.trim())
                            }
                            showAddKeywordDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddKeywordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    quietHoursEnabled: Boolean,
    quietHoursStart: Int,
    quietHoursEnd: Int,
    focusModeEnabled: Boolean,
    onToggleQuietHours: (Boolean) -> Unit,
    onToggleFocusMode: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quiet Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (quietHoursEnabled) "${quietHoursStart}:00 - ${quietHoursEnd}:00" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = quietHoursEnabled,
                    onCheckedChange = onToggleQuietHours
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Focus Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Mode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (focusModeEnabled) "Only show important notifications" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = focusModeEnabled,
                    onCheckedChange = onToggleFocusMode
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onAdd: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (onAdd != null) {
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
}

@Composable
private fun AppRuleRow(
    packageName: String,
    appName: String,
    isBlocked: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconResolver(packageName = packageName, size = 32.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun KeywordRuleRow(
    keyword: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\"$keyword\"",
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SenderRuleRow(
    senderName: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}