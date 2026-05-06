package com.notifiq.feature.onboarding

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val subtitle: String,
    val bullets: List<String>
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Smart Notifications",
        icon = Icons.Default.LocalFireDepartment,
        iconColor = Color(0xFF4B7EF5),
        subtitle = "Intelligent notification classification",
        bullets = listOf(
            "Automatically classifies notifications by importance",
            "Learns from your feedback over time",
            "All processing happens on your device"
        )
    ),
    OnboardingPage(
        title = "Privacy First",
        icon = Icons.Default.Shield,
        iconColor = Color(0xFF34D399),
        subtitle = "Your data stays on your device",
        bullets = listOf(
            "No cloud uploads or external servers",
            "All classification runs locally",
            "Your notification data is encrypted"
        )
    ),
    OnboardingPage(
        title = "Allow Access",
        icon = Icons.Outlined.Notifications,
        iconColor = Color(0xFFFF8A4C),
        subtitle = "Notification access is required",
        bullets = listOf(
            "NotifIQ needs notification access to work",
            "Grant access in system settings",
            "Your notifications never leave your phone"
        )
    ),
    OnboardingPage(
        title = "Quiet Mode",
        icon = Icons.Default.VisibilityOff,
        iconColor = Color(0xFFFACC15),
        subtitle = "Optional notification suppression",
        bullets = listOf(
            "Suppress low-value notifications quietly",
            "Never hides OTPs or banking alerts",
            "Find everything in your private inbox"
        )
    )
)

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Skip button at top right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                viewModel.completeOnboarding()
                onNavigateToHome()
            }) {
                Text("Skip")
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Bottom button
        Button(
            onClick = {
                if (pagerState.currentPage == onboardingPages.size - 1) {
                    viewModel.completeOnboarding()
                    onNavigateToHome()
                } else if (pagerState.currentPage == 2) {
                    // Page 3 is "Allow Access" - open notification listener settings
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    context.startActivity(intent)
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = when (pagerState.currentPage) {
                    2 -> "Grant Access"
                    onboardingPages.size - 1 -> "Get Started"
                    else -> "Continue"
                }
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon in colored circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(page.iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = page.iconColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            page.bullets.forEach { bullet ->
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}