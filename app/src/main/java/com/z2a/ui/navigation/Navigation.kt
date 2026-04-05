package com.z2a.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.z2a.R
import com.z2a.ui.screens.home.HomeScreen
import com.z2a.ui.screens.logs.LogsScreen
import com.z2a.ui.screens.profiles.ProfilesScreen
import com.z2a.ui.screens.settings.SettingsScreen
import com.z2a.ui.theme.Z2aBackground
import com.z2a.ui.theme.Z2aGreen
import com.z2a.ui.theme.Z2aOnSurfaceDim
import com.z2a.ui.theme.Z2aSurface

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)
    data object Profiles : Screen("profiles", R.string.nav_profiles, Icons.Filled.Shield, Icons.Outlined.Shield)
    data object Logs : Screen("logs", R.string.nav_logs, Icons.Filled.List, Icons.Outlined.List)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

val screens = listOf(Screen.Home, Screen.Profiles, Screen.Logs, Screen.Settings)

@Composable
fun Z2aNavHost(onToggleVpn: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Z2aBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Z2aSurface,
                contentColor = Z2aOnSurfaceDim
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.titleResId)
                            )
                        },
                        label = { Text(stringResource(screen.titleResId)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Z2aGreen,
                            selectedTextColor = Z2aGreen,
                            unselectedIconColor = Z2aOnSurfaceDim,
                            unselectedTextColor = Z2aOnSurfaceDim,
                            indicatorColor = Z2aSurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(onToggleVpn = onToggleVpn) }
            composable(Screen.Profiles.route) { ProfilesScreen() }
            composable(Screen.Logs.route) { LogsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
