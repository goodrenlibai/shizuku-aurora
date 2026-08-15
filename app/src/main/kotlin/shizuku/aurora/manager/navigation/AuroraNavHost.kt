package shizuku.aurora.manager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import shizuku.aurora.manager.ui.apps.AppsScreen
import shizuku.aurora.manager.ui.console.ConsoleScreen
import shizuku.aurora.manager.ui.hiddenapi.HiddenApiScreen
import shizuku.aurora.manager.ui.home.HomeScreen
import shizuku.aurora.manager.ui.logs.LogsScreen
import shizuku.aurora.manager.ui.monitor.MonitorScreen
import shizuku.aurora.manager.ui.onboarding.OnboardingScreen
import shizuku.aurora.manager.ui.pairing.PairingScreen
import shizuku.aurora.manager.ui.permissions.PermissionsScreen
import shizuku.aurora.manager.ui.settings.SettingsScreen

/**
 * 路由定义与导航图。
 * ------------------------------------------------------------------
 * 底部导航四大主页面 + 六个二级页面；二级页面不含底栏，由主页卡片进入。
 */
object Routes {
    const val HOME = "home"
    const val APPS = "apps"
    const val CONSOLE = "console"
    const val SETTINGS = "settings"

    const val PAIRING = "pairing"
    const val PERMISSIONS = "permissions"
    const val HIDDEN_API = "hidden_api"
    const val MONITOR = "monitor"
    const val LOGS = "logs"
    const val ONBOARDING = "onboarding"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val filled: ImageVector,
    val outlined: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomDestination(Routes.APPS, "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
    BottomDestination(Routes.CONSOLE, "Console", Icons.Filled.Terminal, Icons.Outlined.Terminal),
    BottomDestination(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun AuroraNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == dest.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.filled else dest.outlined,
                                    contentDescription = dest.label,
                                )
                            },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
        ) {
            composable(Routes.HOME) {
                HomeScreen(padding, onNavigate = { navController.navigate(it) })
            }
            composable(Routes.APPS) { AppsScreen(padding) }
            composable(Routes.CONSOLE) { ConsoleScreen(padding) }
            composable(Routes.SETTINGS) { SettingsScreen(padding) }

            composable(Routes.PAIRING) { PairingScreen(padding) }
            composable(Routes.PERMISSIONS) { PermissionsScreen(padding) }
            composable(Routes.HIDDEN_API) { HiddenApiScreen(padding) }
            composable(Routes.MONITOR) { MonitorScreen(padding) }
            composable(Routes.LOGS) { LogsScreen(padding) }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(padding, onFinish = { navController.popBackStack() })
            }
        }
    }
}
