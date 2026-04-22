package com.example.jetpackcomposeapp.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jetpackcomposeapp.Drawer.DrawerItem
import com.example.jetpackcomposeapp.Drawer.NavDrawerWithNavigation
import com.example.jetpackcomposeapp.ImageRecognition.AIScanScreen
import com.example.jetpackcomposeapp.View.HomeScreen
import com.example.jetpackcomposeapp.View.ProfileScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val navItems = listOf(Screen.Home, Screen.Search, Screen.Profile)

@Composable
fun MainAppContainer(outerNavController: NavHostController) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentTitle = navItems.firstOrNull { it.route == currentRoute }?.label ?: "Store"
    val drawerItems = navItems.map { DrawerItem(route = it.route, title = it.label) }

    NavDrawerWithNavigation(
        title = currentTitle,
        currentRoute = currentRoute,
        items = drawerItems,
        onNavigate = { route ->
            innerNavController.navigate(route) {
                popUpTo(innerNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { innerPadding ->
        androidx.compose.material3.Scaffold(
            bottomBar = {
                NavigationBar {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                innerNavController.navigate(screen.route) {
                                    popUpTo(innerNavController.graph.findStartDestination().id) {
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
        ) { bottomPadding ->
            NavHost(
                navController = innerNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottomPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen(outerNavController) }
                composable(Screen.Search.route) { AIScanScreen() }
                composable(Screen.Profile.route) { ProfileScreen(outerNavController) }
            }
        }
    }
}
