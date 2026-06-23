package com.gainsmaxxing.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gainsmaxxing.ui.calendar.CalendarScreen
import com.gainsmaxxing.ui.home.HomeScreen
import com.gainsmaxxing.ui.workout.WorkoutScreen

// TODO: swap for Lucide when artifact available
private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Calendar : Tab("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object Home : Tab("home", "Home", Icons.Default.Home)
    data object Workout : Tab("workout", "Workout", Icons.Default.FitnessCenter)
}

private val tabs = listOf(Tab.Calendar, Tab.Home, Tab.Workout)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(Tab.Calendar.route) { CalendarScreen() }
            composable(Tab.Home.route) { HomeScreen() }
            composable(Tab.Workout.route) { WorkoutScreen() }
        }
    }
}
