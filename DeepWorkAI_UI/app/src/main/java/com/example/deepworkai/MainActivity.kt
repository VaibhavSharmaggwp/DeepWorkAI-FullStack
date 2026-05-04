package com.example.deepworkai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import com.example.deepworkai.viewmodel.ProfileViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deepworkai.ui.HomeScreen
import com.example.deepworkai.ui.LoginScreen
import com.example.deepworkai.ui.RegisterScreen
import com.example.deepworkai.ui.SplashScreen
import com.example.deepworkai.ui.ActiveSessionScreen
import com.example.deepworkai.ui.AnalyticsScreen
import com.example.deepworkai.ui.Screen
import com.example.deepworkai.ui.DistractionInsightsScreen
import com.example.deepworkai.ui.AppSelectionScreen
import com.example.deepworkai.ui.HistoryScreen
import com.example.deepworkai.ui.SettingsScreen
import com.example.deepworkai.ui.SessionSummaryScreen
import com.example.deepworkai.ui.SecurityScreen
import com.example.deepworkai.models.SessionSummaryResponse
import com.example.deepworkai.ui.theme.DeepWorkAITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepworkai.viewmodel.SessionViewModel

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.deepworkai.ui.NotificationSettingsScreen
import com.example.deepworkai.ui.ProfileScreen
import com.example.deepworkai.ui.AboutScreen
import com.example.deepworkai.ui.HelpScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.deepworkai.network.NetworkPreferences.init(applicationContext)
        val profileViewModel: com.example.deepworkai.viewmodel.ProfileViewModel = ProfileViewModel()
        enableEdgeToEdge()
        setContent {
            val isDarkMode = profileViewModel.user.value?.darkMode ?: true
            DeepWorkAITheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (profileViewModel.user.value?.darkMode != false) Color(0xFF0D1117) else Color.White
                ) {
                    val navController = rememberNavController()
                    val sessionViewModel: SessionViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(
                                onAnimationFinished = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                },
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                onNavigateToActiveSession = {
                                    navController.navigate("active_session")
                                }
                            )
                        }
                        composable("active_session") {
                            ActiveSessionScreen(
                                onFinish = { finalResult ->
                                    if (finalResult != null) {
                                        sessionViewModel.setLatestSession(finalResult)
                                        navController.navigate("session_summary") {
                                            popUpTo("home") { inclusive = false }
                                        }
                                    } else {
                                        // Error fallback
                                        navController.popBackStack("home", inclusive = false)
                                    }
                                }
                            )
                        }
                        composable(Screen.Analytics.route) {
                            AnalyticsScreen(navController = navController)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(navController = navController, viewModel = sessionViewModel)
                        }
                        composable(Screen.DistractionInsights.route) {
                            DistractionInsightsScreen(navController = navController, viewModel = sessionViewModel)
                        }
                        composable(Screen.AppSelection.route) {
                            AppSelectionScreen(navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController)
                        }
                        composable(Screen.Security.route) {
                            SecurityScreen(navController = navController)
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(navController = navController)
                        }
                        composable(Screen.Notifications.route) {
                            NotificationSettingsScreen(navController = navController)
                        }
                        composable(Screen.About.route) {
                            AboutScreen(navController = navController)
                        }
                        composable(Screen.Help.route) {
                            HelpScreen(navController = navController)
                        }
                        composable("session_summary") {
                            SessionSummaryScreen(
                                navController = navController,
                                viewModel = sessionViewModel,
                                onSave = {
                                    // Removed back-end save duplication. Now it just pops back.
                                    val risk = sessionViewModel.currentSession.value?.burnoutRisk ?: "Low"
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("burnout_risk", risk)
                                    navController.popBackStack("home", inclusive = false)
                                },
                                onViewDetailed = { navController.navigate("history_screen") },
                                onClose = { 
                                    val risk = sessionViewModel.currentSession.value?.burnoutRisk ?: "Low"
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("burnout_risk", risk)
                                    navController.popBackStack("home", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

