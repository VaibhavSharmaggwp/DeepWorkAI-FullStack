package com.example.deepworkai.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Analytics : Screen("analytics")
    object History : Screen("history_screen")
    object Settings : Screen("settings")
    object ActiveSession : Screen("active_session")
    object Security : Screen("security")
    object DistractionInsights : Screen("distraction_insights")
    object AppSelection : Screen("app_selection")
}