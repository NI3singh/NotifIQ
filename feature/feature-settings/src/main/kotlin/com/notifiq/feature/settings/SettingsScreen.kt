package com.notifiq.feature.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.LoadingState
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSummary: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSuppressionDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRetentionSheet by remember { mutableStateOf(false) }
    var showSummaryFrequencySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.DataDeleted -> {
                    snackbarHostState.showSnackbar("All data has been deleted")
                }
                is SettingsEvent.DataExported -> {
                    shareJson(context, event.json)
                }
                is SettingsEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Today's Intelligence Report Card
                item {
                    TodayIntelligenceCard(
                        importantCount = uiState.todayImportant,
                        usefulCount = uiState.todayUseful,
                        suppressedCount = uiState.todaySuppressed,
                        spamCount = uiState.todaySpam,
                        usefulPercentage = uiState.usefulPercentage,
                        onViewSummary = onNavigateToSummary
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Notification Behavior Section
                item {
                    SectionTitle("Notification Behavior")
                }

                item {
                    SettingRowWithSwitch(
                        title = "Suppression Mode",
                        subtitle = if (uiState.userPreference.suppressionEnabled) {
                            "Low-value notifications are silenced"
                        } else {
                            "All notifications appear normally"
                        },
                        icon = Icons.Default.Shield,
                        iconTint = MaterialTheme.colorScheme.primary,
                        checked = uiState.userPreference.suppressionEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showSuppressionDialog = true
                            } else {
                                viewModel.onSetSuppressionEnabled(false)
                            }
                        }
                    )
                }

                item {
                    SettingRowWithSwitch(
                        title = "Learning Mode",
                        subtitle = if (uiState.userPreference.learningEnabled) {
                            "Improving from your feedback"
                        } else {
                            "Classification uses fixed rules only"
                        },
                        icon = Icons.Default.Psychology,
                        iconTint = MaterialTheme.colorScheme.primary,
                        checked = uiState.userPreference.learningEnabled,
                        onCheckedChange = viewModel::onSetLearningEnabled
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Summaries Section
                item {
                    SectionTitle("Summaries")
                }

                item {
                    SettingRowWithAction(
                        title = "Summary Schedule",
                        subtitle = getSummaryFrequencyLabel(uiState.userPreference.summaryFrequency),
                        icon = Icons.Default.Notifications,
                        iconTint = Color(0xFF34D399),
                        onClick = { showSummaryFrequencySheet = true }
                    )
                }

                item {
                    SettingRowWithAction(
                        title = "View Daily Summary",
                        subtitle = "See your notification summary",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onNavigateToSummary
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Appearance Section
                item {
                    SectionTitle("Appearance")
                }

                item {
                    SettingRowWithSwitch(
                        title = "Dark Mode",
                        subtitle = if (uiState.userPreference.darkModeEnabled) "Dark theme enabled" else "Light theme",
                        icon = if (uiState.userPreference.darkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                        iconTint = Color(0xFFFACC15),
                        checked = uiState.userPreference.darkModeEnabled,
                        onCheckedChange = viewModel::onSetDarkMode
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Data & Privacy Section
                item {
                    SectionTitle("Data & Privacy")
                }

                item {
                    SettingRowWithAction(
                        title = "Data Retention",
                        subtitle = getRetentionLabel(uiState.userPreference.dataRetentionDays),
                        icon = Icons.Default.Lock,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { showRetentionSheet = true }
                    )
                }

                item {
                    SettingRowWithAction(
                        title = "Privacy Policy",
                        subtitle = "All data stays on your device",
                        icon = Icons.Default.Lock,
                        iconTint = Color(0xFF34D399),
                        onClick = { openPrivacyPolicy(context) }
                    )
                }

                item {
                    SettingRowWithAction(
                        title = "Export Data",
                        subtitle = "Download your notification history as JSON",
                        icon = Icons.Default.FileDownload,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = viewModel::onExportData
                    )
                }

                item {
                    SettingRowDanger(
                        title = "Delete All Data",
                        subtitle = "Permanently erase all notifications, rules, and preferences",
                        onClick = { showDeleteDialog = true }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // About Section
                item {
                    SectionTitle("About")
                }

                item {
                    SettingRowStatic(
                        title = "Version",
                        subtitle = "1.0.0",
                        icon = null,
                        iconTint = Color.Transparent
                    )
                }

                item {
                    Text(
                        text = "Built locally — No data ever leaves your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Suppression Dialog
    if (showSuppressionDialog) {
        AlertDialog(
            onDismissRequest = { showSuppressionDialog = false },
            title = { Text("Enable Suppression?") },
            text = {
                Text(
                    "Low-value notifications will be hidden from your notification shade.\n\n" +
                    "Important: This is best-effort and never hides:\n" +
                    "• OTPs and banking alerts\n" +
                    "• Emergency notifications\n" +
                    "• Calls and messages\n\n" +
                    "All hidden notifications remain visible in your inbox."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onSetSuppressionEnabled(true)
                        showSuppressionDialog = false
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuppressionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data?") },
            text = {
                Text(
                    "This will permanently erase all notifications, rules, preferences, and analytics.\n\n" +
                    "This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteAllData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Retention Bottom Sheet
    if (showRetentionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRetentionSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Data Retention",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(7 to "7 days", 30 to "30 days", 90 to "90 days", 365 to "1 year").forEach { (days, label) ->
                    RetentionOption(
                        label = label,
                        isSelected = uiState.userPreference.dataRetentionDays == days,
                        onClick = {
                            viewModel.onSetDataRetentionDays(days)
                            showRetentionSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Summary Frequency Bottom Sheet
    if (showSummaryFrequencySheet) {
        ModalBottomSheet(
            onDismissRequest = { showSummaryFrequencySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Summary Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "DAILY" to "Daily digest",
                    "WEEKLY" to "Weekly digest (Sundays)",
                    "DAILY_WEEKLY" to "Daily + Weekly",
                    "OFF" to "Off"
                ).forEach { (value, label) ->
                    FrequencyOption(
                        label = label,
                        isSelected = uiState.userPreference.summaryFrequency == value,
                        onClick = {
                            viewModel.onSetSummaryFrequency(value)
                            showSummaryFrequencySheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TodayIntelligenceCard(
    importantCount: Int,
    usefulCount: Int,
    suppressedCount: Int,
    spamCount: Int,
    usefulPercentage: Float,
    onViewSummary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Intelligence Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${suppressedCount} notifications suppressed • ${usefulPercentage.toInt()}% useful",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(
                    count = importantCount,
                    label = "Important",
                    color = Color(0xFFFF8A4C)
                )
                StatBadge(
                    count = usefulCount,
                    label = "Useful",
                    color = Color(0xFF34D399)
                )
                StatBadge(
                    count = suppressedCount,
                    label = "Suppressed",
                    color = Color(0xFFFACC15)
                )
                StatBadge(
                    count = spamCount,
                    label = "Spam",
                    color = Color(0xFFF87171)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewSummary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Full Daily Summary")
            }
        }
    }
}

@Composable
private fun StatBadge(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingRowWithSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingRowWithAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingRowStatic(
    title: String,
    subtitle: String,
    icon: ImageVector?,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingRowDanger(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun RetentionOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FrequencyOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getRetentionLabel(days: Int): String {
    return when (days) {
        7 -> "7 days"
        30 -> "30 days"
        90 -> "90 days"
        365 -> "1 year"
        else -> "$days days"
    }
}

private fun getSummaryFrequencyLabel(frequency: String): String {
    return when (frequency) {
        "DAILY" -> "Daily digest"
        "WEEKLY" -> "Weekly digest (Sundays)"
        "DAILY_WEEKLY" -> "Daily + Weekly"
        "OFF" -> "Off"
        else -> frequency
    }
}

private fun shareJson(context: Context, json: String) {
    try {
        val fileName = "notifiq_export_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(json)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Export Data"))
    } catch (e: Exception) {
        // Handle error
    }
}

private fun openPrivacyPolicy(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("file:///android_asset/privacy_policy.html")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: open in system browser if WebView is not available
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://notifiq.app/privacy")
        }
        context.startActivity(intent)
    }
}