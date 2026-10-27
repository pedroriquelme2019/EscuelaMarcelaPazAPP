package com.example.aplic_movil_didact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.aplic_movil_didact.navigation.Screen
import com.example.aplic_movil_didact.ui.screens.*
import com.example.aplic_movil_didact.ui.theme.Aplic_Movil_DidactTheme
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.LoginViewModel

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

private val slideSpec = tween<IntOffset>(320)
private val fadeSpec = tween<Float>(320)

private val enterForward = slideInHorizontally(slideSpec) { it } + fadeIn(fadeSpec)
private val exitForward  = slideOutHorizontally(slideSpec) { -it / 3 } + fadeOut(fadeSpec)
private val enterBack    = slideInHorizontally(slideSpec) { -it / 3 } + fadeIn(fadeSpec)
private val exitBack     = slideOutHorizontally(slideSpec) { it } + fadeOut(fadeSpec)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Aplic_Movil_DidactTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(
        navController = rootNavController,
        startDestination = Screen.Login.route,
        enterTransition = { enterForward },
        exitTransition = { exitForward },
        popEnterTransition = { enterBack },
        popExitTransition = { exitBack }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = {
                    val rol = loginViewModel.uiState.value.selectedRole
                    rootNavController.navigate(Screen.Main.withRol(rol)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Main.route,
            arguments = listOf(navArgument(Screen.Main.ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString(Screen.Main.ARG) ?: "Estudiante"
            MainScreen(rol = rol)
        }
    }
}

@Composable
fun MainScreen(rol: String) {
    val bottomNavController = rememberNavController()
    val contentViewModel: ContentViewModel = viewModel()

    val bottomNavItems = buildList {
        add(BottomNavItem(Screen.Home, "Inicio", Icons.Filled.Home))
        if (rol == "Docente") add(BottomNavItem(Screen.Monitoreo, "Avances", Icons.Filled.Assessment))
        add(BottomNavItem(Screen.Settings, "Ajustes", Icons.Filled.Settings))
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(item.screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.97f) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.97f) },
            popExitTransition = { fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.97f) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    contentViewModel = contentViewModel,
                    rol = rol,
                    onSubjectClick = { id, nombre ->
                        bottomNavController.navigate(Screen.Subject.withAsignatura(id, nombre))
                    }
                )
            }
            composable(
                route = Screen.Subject.route,
                arguments = listOf(
                    navArgument(Screen.Subject.ARG_ID) { type = NavType.IntType },
                    navArgument(Screen.Subject.ARG_NOMBRE) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val asignaturaId = backStackEntry.arguments?.getInt(Screen.Subject.ARG_ID) ?: 0
                val nombre = backStackEntry.arguments?.getString(Screen.Subject.ARG_NOMBRE) ?: ""
                SubjectScreen(
                    asignaturaId = asignaturaId,
                    asignatura = nombre,
                    rol = rol,
                    contentViewModel = contentViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onIniciarTarea = { titulo, asig, unidad ->
                        bottomNavController.navigate(Screen.Task.withContext(titulo, asig, unidad))
                    }
                )
            }
            composable(
                route = Screen.Task.route,
                arguments = listOf(
                    navArgument(Screen.Task.ARG_TITLE) { type = NavType.StringType },
                    navArgument(Screen.Task.ARG_ASIGNATURA) { type = NavType.StringType },
                    navArgument(Screen.Task.ARG_UNIDAD) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val titulo = backStackEntry.arguments?.getString(Screen.Task.ARG_TITLE) ?: ""
                val asig = backStackEntry.arguments?.getString(Screen.Task.ARG_ASIGNATURA) ?: ""
                val unidad = backStackEntry.arguments?.getString(Screen.Task.ARG_UNIDAD) ?: ""
                TaskScreen(
                    tareaTitle = titulo,
                    asignatura = asig,
                    unidadTitulo = unidad,
                    contentViewModel = contentViewModel,
                    onBack = { bottomNavController.popBackStack() }
                )
            }
            composable(Screen.Monitoreo.route) {
                MonitoreoScreen(contentViewModel = contentViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
