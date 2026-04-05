package com.coe.mobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coe.mobile.ui.components.AppBottomNavigation
import com.coe.mobile.ui.components.AppTopBar
import com.coe.mobile.ui.navigation.AppNavHost
import com.coe.mobile.ui.navigation.AppRoute
import com.coe.mobile.ui.theme.CoEMobileTheme
import com.coe.mobile.util.AppConstants

@Composable
fun CoEApp() {
    CoEMobileTheme {
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route
        val currentTitle = when {
            currentRoute == AppRoute.Dashboard.route -> "Dashboard"
            currentRoute == AppRoute.Recorder.route -> "Recorder Console"
            currentRoute == AppRoute.History.route -> "Meeting History"
            currentRoute == AppRoute.Processing.route -> "Processing Status"
            currentRoute == AppRoute.Inbox.route -> "Approvals Inbox"
            currentRoute == AppRoute.SuggestionDetail.route -> "Suggestion Detail"
            currentRoute?.startsWith("history/meeting/") == true -> "Meeting Detail"
            else -> AppConstants.APP_TITLE
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AppTopBar(title = currentTitle)
            },
            bottomBar = {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
