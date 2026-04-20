package com.example.deepworkai.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.deepworkai.models.DistractionApp
import com.example.deepworkai.models.DistractionInsightsResponse
import com.example.deepworkai.models.SessionDistractions
import com.example.deepworkai.network.FocusService
import com.example.deepworkai.network.NetworkPreferences
import com.example.deepworkai.ui.theme.DeepWorkBackground
import com.example.deepworkai.ui.theme.DeepWorkBlue
import com.example.deepworkai.ui.theme.DeepWorkSurface
import com.example.deepworkai.ui.theme.DeepWorkTextSecondary
import com.example.deepworkai.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

@Composable
fun DistractionInsightsScreen(
    navController: NavController,
    viewModel: SessionViewModel
) {
    val focusService = remember { FocusService() }
    val userId = NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"

    var insightsData by remember { mutableStateOf<DistractionInsightsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("AppTrackingPrefs", android.content.Context.MODE_PRIVATE)
    val isTrackingSetupComplete = prefs.getBoolean("is_tracking_setup_complete", false)

    LaunchedEffect(Unit) {
        val result = focusService.getDistractionInsights(userId)
        if (result != null) {
            insightsData = result
        }
        isLoading = false
    }

    Scaffold(
        containerColor = DeepWorkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Back", 
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                M3Text(
                    text = "Distraction Insights",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepWorkBlue)
                }
            } else {
                if (!isTrackingSetupComplete) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        M3Text(
                            "Start a focus session and select apps to track distractions.",
                            color = DeepWorkTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    val data = insightsData
                    if (data == null || data.sessions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            M3Text(
                                "Great job! No distractions recorded in your sessions 🎯",
                                color = Color(0xFF4ADE80),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(data.sessions) { index, session ->
                                SessionGroupCard(session = session, index = index)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Insight Container
                        AIInsightCard(data.recommendation)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionGroupCard(session: SessionDistractions, index: Int) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 150L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepWorkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                M3Text(
                    text = session.sessionTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val totalUsage = session.apps.sumOf { it.usageTime }.toFloat().coerceAtLeast(1f)

                session.apps.forEachIndexed { appIndex, app ->
                    AppUsageRow(app = app, progress = app.usageTime / totalUsage, isTop = appIndex == 0)
                    if (appIndex < session.apps.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppUsageRow(app: DistractionApp, progress: Float, isTop: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Determine domain for Clearbit Logo API
        val domain = when (app.appName.lowercase().replace(" ", "")) {
            "instagram" -> "instagram.com"
            "youtube" -> "youtube.com"
            "facebook" -> "facebook.com"
            "tiktok" -> "tiktok.com"
            "twitter" -> "twitter.com"
            "whatsapp" -> "whatsapp.com"
            "chrome" -> "google.com/chrome"
            "gmail" -> "mail.google.com"
            "maps" -> "maps.google.com"
            else -> "${app.appName.lowercase().replace(" ", "")}.com"
        }

        // Logo
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://logo.clearbit.com/$domain")
                .crossfade(true)
                .build(),
            contentDescription = "${app.appName} logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            M3Text(
                text = app.appName, 
                color = Color.White, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress bar representing % of total distraction
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha=0.1f))) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(4.dp).background(
                    if (isTop) Color(0xFFEF4444) else DeepWorkBlue
                ))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        M3Text(
            text = "${app.usageTime}m", 
            color = if (isTop) Color(0xFFEF4444) else Color.White, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AIInsightCard(recommendation: String) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500))
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF2DD4BF).copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Insight", tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    M3Text("AI PRODUCTIVITY INSIGHT", color = Color(0xFF2DD4BF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                M3Text(
                    text = recommendation,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
