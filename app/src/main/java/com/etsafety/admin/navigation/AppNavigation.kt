package com.etsafety.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.etsafety.admin.data.PremiumUserDao
import com.etsafety.admin.ui.screens.AddUserScreen
import com.etsafety.admin.ui.screens.DashboardScreen
import com.etsafety.admin.ui.screens.UserListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object AddUser : Screen("add_user", "Add User", Icons.Default.Add)
    data object UserList : Screen("user_list", "Users", Icons.Default.List)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.AddUser,
    Screen.UserList
)

@Composable
fun AppNavigation(dao: PremiumUserDao) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    dao = dao,
                    onAddUserClick = {
                        navController.navigate(Screen.AddUser.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.AddUser.route) {
                AddUserScreen(
                    dao = dao,
                    onUserSaved = {
                        navController.navigate(Screen.UserList.route) {
                            popUpTo(Screen.Dashboard.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.UserList.route) {
                UserListScreen(dao = dao)
            }
        }
    }
}
