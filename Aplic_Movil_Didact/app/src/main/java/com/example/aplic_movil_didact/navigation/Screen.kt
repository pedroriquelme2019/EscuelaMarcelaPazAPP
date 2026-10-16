package com.example.aplic_movil_didact.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home_screen")
    data object Profile : Screen("profile_screen")
    data object Settings : Screen("settings_screen")
}
