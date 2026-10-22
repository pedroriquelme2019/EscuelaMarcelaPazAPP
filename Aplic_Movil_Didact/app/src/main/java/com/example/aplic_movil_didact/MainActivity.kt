package com.example.aplic_movil_didact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplic_movil_didact.navigation.NavigationEvent
import com.example.aplic_movil_didact.navigation.Screen
import com.example.aplic_movil_didact.ui.screens.*
import com.example.aplic_movil_didact.ui.theme.Aplic_Movil_DidactTheme
import com.example.aplic_movil_didact.viewmodels.LoginViewModel
import com.example.aplic_movil_didact.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private val estudianteNavItems = listOf(
    BottomNavItem(Screen.Home, "Inicio", Icons.Filled.Home),
    BottomNavItem(Screen.Profile, "Perfil", Icons.Filled.AccountCircle),
    BottomNavItem(Screen.Settings, "Ajustes", Icons.Filled.Settings)
)

private val docenteNavItems = listOf(
    BottomNavItem(Screen.DocentePanel, "Mi Clase", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Screen.Profile, "Perfil", Icons.Filled.AccountCircle),
    BottomNavItem(Screen.Settings, "Ajustes", Icons.Filled.Settings)
)

private val utpNavItems = listOf(
    BottomNavItem(Screen.UTPPanel, "Panel UTP", Icons.Filled.AdminPanelSettings),
    BottomNavItem(Screen.DocentePanel, "Informes", Icons.Filled.Assessment),
    BottomNavItem(Screen.Settings, "Ajustes", Icons.Filled.Settings)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Aplic_Movil_DidactTheme {
                val rootNavController = rememberNavController()

                NavHost(
                    navController = rootNavController,
                    startDestination = Screen.Login.route,
                    enterTransition = { slideInVertically { it } },
                    exitTransition = { slideOutVertically { -it } },
                    popEnterTransition = { slideInVertically { -it } },
                    popExitTransition = { slideOutVertically { it } }
                ) {
                    composable(Screen.Login.route) {
                        val loginViewModel: LoginViewModel = viewModel()
                        LoginScreen(
                            onLoginSuccess = { role ->
                                rootNavController.navigate(Screen.Main.withRole(role)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            loginViewModel = loginViewModel
                        )
                    }

                    composable(
                        route = Screen.Main.route,
                        arguments = listOf(
                            navArgument(Screen.Main.ARG_ROLE) { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val role = backStackEntry.arguments?.getString(Screen.Main.ARG_ROLE) ?: "Estudiante"
                        MainContent(role = role)
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(role: String) {
    val mainViewModel: MainViewModel = viewModel()
    val bottomNavController = rememberNavController()

    val navItems = when (role) {
        "UTP / Administrador" -> utpNavItems
        "Docente" -> docenteNavItems
        else -> estudianteNavItems
    }

    val startDestination = when (role) {
        "UTP / Administrador" -> Screen.UTPPanel.route
        "Docente" -> Screen.DocentePanel.route
        else -> Screen.Home.route
    }

    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> bottomNavController.navigate(event.route.route)
                is NavigationEvent.PopBackStack -> bottomNavController.popBackStack()
                is NavigationEvent.NavigateUp -> bottomNavController.navigateUp()
            }
        }
    }

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(item.screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
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
            navController = bottomNavController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it } },
            popEnterTransition = { slideInHorizontally { -it } },
            popExitTransition = { slideOutHorizontally { it } }
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel = mainViewModel) }
            composable(Screen.Profile.route) { ProfileScreen(role = role) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.UTPPanel.route) { UTPScreen() }
            composable(Screen.DocentePanel.route) { DocenteScreen() }
        }
    }
}
