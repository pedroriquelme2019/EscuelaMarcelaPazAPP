package com.example.aplic_movil_didact.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Main : Screen("main_screen/{role}") {
        fun withRole(role: String) = "main_screen/${Uri.encode(role)}"
        const val ARG_ROLE = "role"
    }
    data object Home : Screen("home_screen")
    data object Profile : Screen("profile_screen")
    data object Settings : Screen("settings_screen")
    data object UTPPanel : Screen("utp_screen")
    data object DocentePanel : Screen("docente_screen")
}
