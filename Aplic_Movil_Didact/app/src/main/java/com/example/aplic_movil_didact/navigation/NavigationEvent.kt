package com.example.aplic_movil_didact.navigation

sealed class NavigationEvent {
    data class NavigateTo(val route: Screen) : NavigationEvent()
    data object PopBackStack : NavigationEvent()
    data object NavigateUp : NavigationEvent()
}
