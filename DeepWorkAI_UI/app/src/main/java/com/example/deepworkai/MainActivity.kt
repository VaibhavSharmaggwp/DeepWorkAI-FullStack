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
import androidx.compose.ui.Modifier
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

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.deepworkai.network.NetworkPreferences.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            DeepWorkAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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

