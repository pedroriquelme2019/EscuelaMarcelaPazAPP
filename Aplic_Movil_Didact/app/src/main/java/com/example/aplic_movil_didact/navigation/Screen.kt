package com.example.aplic_movil_didact.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Main : Screen("main_screen")
    data object Home : Screen("home_screen")
    data object Profile : Screen("profile_screen")
    data object Settings : Screen("settings_screen")
}
