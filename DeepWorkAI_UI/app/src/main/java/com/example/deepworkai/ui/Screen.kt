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
    object FlowInsights : Screen("flow_insights")
    object AppSelection : Screen("app_selection")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Help : Screen("help")
    object About : Screen("about")
    object TaskPlanner : Screen("task_planner")
    object Vitality : Screen("vitality")
    object CognitiveChallenge : Screen("cognitive_challenge")
}