package com.notifiq.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notifiq.core.datastore.UserPreferenceDataStore
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

    val startDestination = if (hasCompletedOnboarding) Routes.Home.route else Routes.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Onboarding.route) {
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
                    onNavigateToInbox = { filter ->
                        navController.navigate(Routes.Inbox.route)
                    },
                    onNavigateToDetail = { id ->
                        navController.navigate(Routes.Detail.createRoute(id))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.Settings.route)
                    },
                    userPreferenceDataStore = userPreferenceDataStore
                )
            }

            composable(Routes.Inbox.route) {
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

            composable(
                route = Routes.Detail.route,
                arguments = listOf(navArgument("notificationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getLong("notificationId") ?: 0L
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