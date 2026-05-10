package com.example.deepworkai.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deepworkai.models.DistractionApp
import com.example.deepworkai.models.DistractionInsightsResponse
import com.example.deepworkai.network.FocusService
import com.example.deepworkai.network.NetworkPreferences
import com.example.deepworkai.ui.theme.*
import com.example.deepworkai.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

@Composable
fun FlowInsightsScreen(
    navController: NavController,
    viewModel: SessionViewModel
) {
    val focusService = remember { FocusService() }
    val userId = NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"

    var insightsData by remember { mutableStateOf<DistractionInsightsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val sessionHistory by viewModel.history.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory(userId)
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
                .verticalScroll(rememberScrollState())
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
                Text(
                    text = "Flow State Lab",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepWorkBlue)
                }
            } else {
                // Feature 1: Focus Stability Report
                Text(
                    "Focus Stability Trend",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                FocusStabilityChart(scores = sessionHistory.takeLast(7).map { it.focusScore })

                Spacer(modifier = Modifier.height(32.dp))

                // Feature 2: Cognitive Resilience Metric
                val totalDistractions = insightsData?.sessions?.sumOf { it.apps.sumOf { it.usageTime } } ?: 0
                val resilienceScore = (100 - (totalDistractions * 2)).coerceIn(0, 100)
                CognitiveResilienceCard(score = resilienceScore)

                Spacer(modifier = Modifier.height(32.dp))

                // Feature 3: AI Flow Recommendation
                if (insightsData?.recommendation?.isNotEmpty() == true) {
                    AIInsightCard(insightsData?.recommendation!!)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Feature 4: Focus Leaks (Renamed from Distractions)
                if (insightsData?.sessions?.isNotEmpty() == true) {
                    Text(
                        "Focus Leaks",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Periods where your attention drifted to other apps.",
                        color = DeepWorkTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    insightsData?.sessions?.forEachIndexed { index, session ->
                        session.apps.forEach { app ->
                            FocusLeakRow(app = app)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                } else {
                    EmptyFlowState()
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FocusStabilityChart(scores: List<Int>) {
    val displayScores = if (scores.isEmpty()) listOf(70, 75, 72, 85, 80, 92, 88) else scores
    
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (displayScores.size - 1).coerceAtLeast(1)
                
                val points = displayScores.mapIndexed { index, score ->
                    androidx.compose.ui.geometry.Offset(
                        x = index * spacing,
                        y = height * (1f - (score / 100f))
                    )
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            cubicTo(
                                prev.x + (curr.x - prev.x) / 2, prev.y,
                                prev.x + (curr.x - prev.x) / 2, curr.y,
                                curr.x, curr.y
                            )
                        }
                    }
                }

                drawPath(
                    path = path,
                    color = DeepWorkBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Gradient fill
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepWorkBlue.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            }
        }
    }
}

@Composable
fun CognitiveResilienceCard(score: Int) {
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = score / 100f,
                    color = Color(0xFF2DD4BF),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    "$score%", 
                    color = Color.White, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    "Cognitive Resilience", 
                    color = Color.White, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "How well you maintained focus.", 
                    color = DeepWorkTextSecondary, 
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FocusLeakRow(app: DistractionApp) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(app.appName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(app.appName, color = Color.White, modifier = Modifier.weight(1f))
            Text("${app.usageTime}m", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyFlowState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✨", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Pristine Focus Detected", 
            color = Color(0xFF4ADE80), 
            fontSize = 18.sp, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your recent sessions have been free of digital distractions. Your neuro-performance is peaking!", 
            color = DeepWorkTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

@Composable
fun AIInsightCard(recommendation: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2DD4BF).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Insight", tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI FLOW RECOMMENDATION", color = Color(0xFF2DD4BF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recommendation,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
