package com.notifiq.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiq.core.designsystem.component.LoadingState
import com.notifiq.core.designsystem.component.NotificationCard
import com.notifiq.core.designsystem.component.OemInstructionsBanner
import com.notifiq.core.designsystem.component.PermissionHealthBanner
import com.notifiq.core.designsystem.component.StatCard
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.core.designsystem.theme.EyebrowStyle
import com.notifiq.core.designsystem.theme.HeroNumberStyle
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.model.ClassificationLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInboxWithFilter: ((String?) -> Unit)? = null,
    userPreferenceDataStore: UserPreferenceDataStore? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        LoadingState()
        return
    }

    val filteredCount = uiState.lowValueCount + uiState.spamCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
    ) {
        item { HomeHeader(onNavigateToSettings) }
        item { Spacer(Modifier.height(24.dp)) }

        item { PermissionHealthBanner(Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
        if (userPreferenceDataStore != null) {
            item {
                OemInstructionsBanner(
                    onDismiss = { viewModel.dismissOemBanner() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }

        item {
            StaggerIn(0) {
                NoiseHero(
                    percent = uiState.noiseReductionPercent.toInt(),
                    kept = uiState.importantCount + uiState.usefulCount,
                    filtered = filteredCount,
                )
            }
        }

        item { Spacer(Modifier.height(30.dp)) }

        item {
            StaggerIn(1) {
                SecondaryStats(
                    importantCount = uiState.importantCount,
                    usefulCount = uiState.usefulCount,
                    filteredCount = filteredCount,
                    spamCount = uiState.spamCount,
                    onFilter = onNavigateToInboxWithFilter,
                )
            }
        }

        if (uiState.recentImportantNotifications.isNotEmpty()) {
            item { Spacer(Modifier.height(32.dp)) }
            item { StaggerIn(2) { SectionLabel("What mattered") } }
            item { Spacer(Modifier.height(14.dp)) }
            items(uiState.recentImportantNotifications) { notification ->
                NotificationCard(
                    appName = notification.appName,
                    title = notification.title,
                    text = notification.text,
                    timestamp = notification.postTime,
                    label = notification.classificationLabel,
                    confidence = notification.classificationScore,
                    isSuppressed = notification.isSuppressed,
                    isRead = notification.isRead,
                    onClick = { onNavigateToDetail(notification.id) },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }

        if (uiState.topNoisyApps.isNotEmpty()) {
            item { Spacer(Modifier.height(20.dp)) }
            item { SectionLabel("Noisiest apps") }
            item { Spacer(Modifier.height(6.dp)) }
            items(uiState.topNoisyApps) { app ->
                val maxCount = uiState.topNoisyApps.maxOfOrNull { it.count } ?: 1
                NoisyAppRow(appName = app.appName, count = app.count, maxCount = maxCount)
            }
        }
    }
}

@Composable
private fun HomeHeader(onNavigateToSettings: () -> Unit) {
    val today = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = today.uppercase(),
                style = EyebrowStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "NotifIQ",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onNavigateToSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoiseHero(percent: Int, kept: Int, filtered: Int) {
    // Count-up: start at 0 on first composition, then animate to the real value.
    var target by remember { mutableIntStateOf(0) }
    LaunchedEffect(percent) { target = percent }
    val shown by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "noisePercent"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "NOISE FILTERED TODAY",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$shown",
                style = HeroNumberStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "%",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "$kept kept · $filtered filtered from your stream",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecondaryStats(
    importantCount: Int,
    usefulCount: Int,
    filteredCount: Int,
    spamCount: Int,
    onFilter: ((String?) -> Unit)?,
) {
    val ext = AppTheme.ext
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Important",
                value = importantCount.toString(),
                icon = Icons.Outlined.Settings,
                iconTint = ext.labelImportant,
                modifier = Modifier.weight(1f),
                onClick = { onFilter?.invoke(ClassificationLabel.IMPORTANT.name) },
            )
            StatCard(
                title = "Useful",
                value = usefulCount.toString(),
                icon = Icons.Outlined.Settings,
                iconTint = ext.labelUseful,
                modifier = Modifier.weight(1f),
                onClick = { onFilter?.invoke(ClassificationLabel.USEFUL.name) },
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Filtered",
                value = filteredCount.toString(),
                icon = Icons.Outlined.Settings,
                iconTint = ext.warning,
                modifier = Modifier.weight(1f),
                onClick = { onFilter?.invoke(null) },
            )
            StatCard(
                title = "Spam",
                value = spamCount.toString(),
                icon = Icons.Outlined.Settings,
                iconTint = ext.labelSpam,
                modifier = Modifier.weight(1f),
                onClick = { onFilter?.invoke(ClassificationLabel.SPAM.name) },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AppTheme.ext.hairline)
        )
    }
}

@Composable
private fun NoisyAppRow(appName: String, count: Int, maxCount: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppTheme.ext.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(count.toFloat() / maxCount.toFloat())
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppTheme.ext.warning)
            )
        }
    }
}

/** Staggered entrance: fade + slight rise, delayed by [index]. */
@Composable
private fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 6 },
    ) {
        content()
    }
}
