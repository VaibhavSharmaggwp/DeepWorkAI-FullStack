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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
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

val CyberNeonCyan = Color(0xFF00F0FF)
val CyberNeonPurple = Color(0xFFB026FF)
val CyberDarkBg = Color(0xFF0B0C10)
val CyberSurface = Color(0xFF1F2833)

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

    // Infinite transition for cyber scanning line
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_offset"
    )

    LaunchedEffect(Unit) {
        viewModel.fetchHistory(userId)
        val result = focusService.getDistractionInsights(userId)
        if (result != null) {
            insightsData = result
        }
        isLoading = false
    }

    Scaffold(
        containerColor = CyberDarkBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Cyber Background Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 60.dp.toPx()
                for (i in 0..size.width.toInt() step gridSpacing.toInt()) {
                    drawLine(Color.White.copy(alpha = 0.03f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), 1f)
                }
                for (i in 0..size.height.toInt() step gridSpacing.toInt()) {
                    drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), 1f)
                }
            }

            // Scanning Laser Line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * scanOffset
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, CyberNeonCyan.copy(alpha = 0.5f), Color.Transparent),
                        startY = y - 20f,
                        endY = y + 20f
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 4f
                )
            }

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
                        border = BorderStroke(1.dp, CyberNeonCyan.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            tint = CyberNeonCyan,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "FLOW.STATE.LAB",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text("SYS.OVERRIDE_ACTIVE", color = CyberNeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CyberNeonCyan)
                    }
                } else {
                    // Feature 1: Glowing Neon Trend Graph
                    Text(
                        "NEURAL STABILITY TREND",
                        color = CyberNeonPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val scores = sessionHistory.takeLast(7).map { it.focusScore }
                    if (scores.isEmpty()) {
                        CyberEmptyState("INSUFFICIENT DATA FOR TREND ANALYSIS")
                    } else {
                        GlowingCyberChart(scores = scores)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Feature 2: Rotating HUD Rings (Cognitive Resilience)
                    val totalDistractionMins = insightsData?.sessions?.sumOf { it.apps.sumOf { it.usageTime } } ?: 0
                    val totalFocusMinsAllTime = sessionHistory.sumOf { 
                        try {
                            val startStr = it.startTime.substringBefore(".")
                            val start = java.time.LocalDateTime.parse(startStr)
                            val endStr = it.endTime?.substringBefore(".") ?: startStr
                            val end = if (endStr != "null") java.time.LocalDateTime.parse(endStr) else start
                            java.time.Duration.between(start, end).toMinutes().toInt()
                        } catch(e: Exception) { 
                            0 
                        }
                    }
                    val hasData = sessionHistory.isNotEmpty()
                    val resilienceScore = if (hasData) {
                        if (totalFocusMinsAllTime == 0) {
                            if (totalDistractionMins > 0) 0 else 100
                        } else {
                            val ratio = totalFocusMinsAllTime.toFloat() / (totalFocusMinsAllTime + totalDistractionMins).toFloat()
                            (ratio * 100).toInt().coerceIn(0, 100)
                        }
                    } else null
                    
                    CyberResilienceHud(score = resilienceScore)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Feature 3: Neural Overload Predictor
                    val totalFocusMinsToday = sessionHistory.filter { 
                        it.startTime.startsWith(java.time.LocalDate.now().toString()) 
                    }.sumOf { 
                        try {
                            val startStr = it.startTime.substringBefore(".") // Remove milliseconds if any
                            val start = java.time.LocalDateTime.parse(startStr)
                            val endStr = it.endTime?.substringBefore(".") ?: startStr
                            val end = if (endStr != "null") java.time.LocalDateTime.parse(endStr) else start
                            java.time.Duration.between(start, end).toMinutes().toInt()
                        } catch(e: Exception) { 
                            0 
                        }
                    }
                    NeuralOverloadPredictor(totalFocusMinsToday)
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // Feature 4: AI Insight
                    if (insightsData?.recommendation?.isNotEmpty() == true) {
                        CyberAIInsightCard(insightsData?.recommendation!!)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Feature 5: Focus Leaks
                    Text(
                        "ATTENTION LEAKS DETECTED",
                        color = Color(0xFFF87171),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (insightsData?.sessions?.isNotEmpty() == true) {
                        insightsData?.sessions?.forEachIndexed { index, session ->
                            session.apps.forEach { app ->
                                CyberFocusLeakRow(app = app)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        if(hasData) {
                             Text("0 LEAKS. NEURO-PATHWAYS OPTIMAL.", color = CyberNeonCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        } else {
                             Text("NO DATA LOGGED.", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun CyberEmptyState(msg: String) {
    Surface(
        color = CyberSurface.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(msg, color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun GlowingCyberChart(scores: List<Int>) {
    Surface(
        color = CyberSurface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CyberNeonCyan.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (scores.size - 1).coerceAtLeast(1)
                
                // Draw internal grid
                val rows = 4
                for(i in 0..rows) {
                    val y = height * (i.toFloat() / rows)
                    drawLine(Color.White.copy(0.05f), Offset(0f, y), Offset(width, y), 1f)
                }

                val points = scores.mapIndexed { index, score ->
                    Offset(
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

                // Shadow/Glow layer
                drawPath(
                    path = path,
                    color = CyberNeonCyan.copy(alpha = 0.4f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Core line
                drawPath(
                    path = path,
                    color = CyberNeonCyan,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Points
                points.forEach { pt ->
                    drawCircle(CyberNeonPurple, radius = 6.dp.toPx(), center = pt)
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = pt)
                }
            }
        }
    }
}

@Composable
fun CyberResilienceHud(score: Int?) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud")
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)), label = "r1"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)), label = "r2"
    )

    Surface(
        color = CyberSurface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CyberNeonPurple.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                // Outer ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(rotation1) {
                        drawArc(
                            color = CyberNeonPurple, startAngle = 0f, sweepAngle = 280f,
                            useCenter = false, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Square)
                        )
                    }
                }
                // Inner ring
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    rotate(rotation2) {
                        drawArc(
                            color = CyberNeonCyan, startAngle = 45f, sweepAngle = 200f,
                            useCenter = false, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                
                Text(
                    text = if (score != null) "$score%" else "N/A", 
                    color = Color.White, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    "COGNITIVE RESILIENCE", 
                    color = Color.White, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (score != null) "Neurological resistance to digital distraction algorithms." else "Awaiting neural sync data.", 
                    color = Color.Gray, 
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun NeuralOverloadPredictor(totalFocusMins: Int) {
    // Let's say overload happens at 120 mins (2 hours) for demo purposes
    val maxMins = 120f
    val progress = (totalFocusMins / maxMins).coerceIn(0f, 1f)
    
    val barColor = when {
        progress < 0.5f -> CyberNeonCyan
        progress < 0.8f -> Color(0xFFF59E0B)
        else -> Color(0xFFF87171)
    }

    Column {
        Text("NEURAL BURNOUT PREDICTOR", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(CyberSurface, RoundedCornerShape(8.dp))) {
            Box(modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(barColor, RoundedCornerShape(8.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("OPTIMAL", color = CyberNeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("CRITICAL LIMIT", color = Color(0xFFF87171), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun CyberFocusLeakRow(app: DistractionApp) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CyberSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFF87171).copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("[ERR]", color = Color(0xFFF87171), fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(12.dp))
        Text(app.appName.uppercase(), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("${app.usageTime} MINS", color = Color(0xFFF87171), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CyberAIInsightCard(recommendation: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberNeonCyan.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, CyberNeonCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = CyberNeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CORTEX_AI_ANALYSIS", color = CyberNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "> $recommendation",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )
        }
    }
}
