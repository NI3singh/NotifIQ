package com.notifiq.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notifiq.core.datastore.UserPreferenceDataStore
import com.notifiq.core.designsystem.theme.AppTheme
import com.notifiq.feature.analytics.AnalyticsScreen
import com.notifiq.feature.detail.DetailScreen
import com.notifiq.feature.home.HomeScreen
import com.notifiq.feature.inbox.InboxScreen
import com.notifiq.feature.onboarding.OnboardingScreen
import com.notifiq.feature.rules.RulesScreen
import com.notifiq.feature.settings.SettingsScreen
import com.notifiq.feature.summary.SummaryScreen

private val bottomTabRoutes = setOf(
    Routes.Home.route,
    Routes.Inbox.route,
    Routes.Analytics.route,
    Routes.Rules.route,
    Routes.Settings.route
)

@Composable
fun NotifIQNavHost(
    hasCompletedOnboarding: Boolean,
    userPreferenceDataStore: UserPreferenceDataStore,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomTabRoutes
    val startDestination =
        if (hasCompletedOnboarding) Routes.Home.route else Routes.Onboarding.route

    val ext = AppTheme.ext
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ext.gradientTop, ext.gradientBottom)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(navController = navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                // Consume the insets the outer Scaffold already applied so nested
                // per-screen Scaffolds (Analytics/Settings/Rules/Summary) don't add
                // the status-bar inset a second time — which caused a large empty gap.
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
            composable(Routes.Onboarding.route) {
                // FIX 6A: OnboardingScreen declares its callback as onComplete, not onNavigateToHome.
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Home.route) {
                HomeScreen(
                    // FIX 6B: HomeScreen's parameter is onNavigateToInboxWithFilter, not onNavigateToInbox.
                    onNavigateToInboxWithFilter = { filter ->
                        navController.navigate(Routes.Inbox.route)
                    },
                    // FIX 5C: id is now String; createRoute accepts String.
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.Detail.createRoute(id))
                    },
                    onNavigateToSettings = {
                        // Route through the shared tab helper so reaching Settings from
                        // here is identical to selecting the Settings tab — otherwise the
                        // Home tab can't be re-selected afterwards.
                        navController.navigateToTab(Routes.Settings.route)
                    },
                    userPreferenceDataStore = userPreferenceDataStore
                )
            }

            composable(Routes.Inbox.route) {
                // FIX 5C: id is String from notification UUID.
                InboxScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.Detail.createRoute(id))
                    }
                )
            }

            composable(Routes.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Routes.Rules.route) {
                RulesScreen()
            }

            composable(Routes.Settings.route) {
                SettingsScreen(
                    onNavigateToSummary = {
                        navController.navigate(Routes.Summary.route)
                    }
                )
            }

            // FIX 5C: Use NavType.StringType and getString instead of LongType/getLong.
            composable(
                route = Routes.Detail.route,
                arguments = listOf(
                    navArgument("notificationId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val notificationId =
                    backStackEntry.arguments?.getString("notificationId") ?: ""
                DetailScreen(
                    notificationId = notificationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Summary.route) {
                SummaryScreen(onNavigateBack = { navController.popBackStack() })
            }
            }
        }
    }
}