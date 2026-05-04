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
        val data = insightsData
        if (data == null) {
            // Error State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    M3Text("Could not load insights", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    M3Text("Check your connection and try again", color = DeepWorkTextSecondary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            isLoading = true
                            navController.navigate(Screen.DistractionInsights.route) {
                                popUpTo(Screen.DistractionInsights.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
                    ) {
                        M3Text("Retry", color = Color.White)
                    }
                }
            }
        } else if (data.sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    M3Text(
                        text = "🎯",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    M3Text(
                        "Great job! No distractions recorded in your sessions.",
                        color = Color(0xFF4ADE80),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    M3Text(
                        "You've been staying focused! Keep it up to build your streak.",
                        color = DeepWorkTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
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
            if (data.recommendation.isNotEmpty()) {
                AIInsightCard(data.recommendation)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
}

@Composable
fun SessionGroupCard(session: SessionDistractions, index: Int) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L) // Slightly faster animation
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DeepWorkSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    M3Text(
                        text = session.sessionTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    M3Text(
                        text = session.date,
                        color = DeepWorkTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val totalUsage = session.apps.sumOf { it.usageTime }.toFloat().coerceAtLeast(1f)

                session.apps.forEachIndexed { appIndex, app ->
                    AppUsageRow(app = app, progress = app.usageTime / totalUsage, isTop = appIndex == 0)
                    if (appIndex < session.apps.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppUsageRow(app: DistractionApp, progress: Float, isTop: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .background(if (expanded) Color.White.copy(alpha=0.03f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                M3Text(
                    text = app.appName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                M3Text(
                    text = app.appName, 
                    color = Color.White, 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Progress bar representing % of total distraction
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha=0.05f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress).height(6.dp).background(
                        if (isTop) Color(0xFFF87171) else DeepWorkBlue
                    ))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            M3Text(
                text = "${app.usageTime}m", 
                color = if (isTop) Color(0xFFF87171) else Color.White, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.Bold
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Crazy Animation
                PulsingRadarAnimation(isTop = isTop)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                M3Text(
                    text = "You used ${app.appName} for ${app.usageTime} minutes during your focus session. Try reducing it.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PulsingRadarAnimation(isTop: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val coreColor = if (isTop) Color(0xFFEF4444) else DeepWorkBlue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                color = coreColor.copy(alpha = alpha * 0.5f),
                radius = size.width / 2 * scale
            )
            drawCircle(
                color = coreColor.copy(alpha = alpha),
                radius = size.width / 2 * (scale * 0.6f)
            )
            drawCircle(
                color = coreColor,
                radius = size.width / 2 * 0.3f
            )
        }
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
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
