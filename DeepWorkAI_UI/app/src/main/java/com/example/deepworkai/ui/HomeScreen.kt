package com.example.deepworkai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepworkai.R
import com.example.deepworkai.network.FocusService
import com.example.deepworkai.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.deepworkai.viewmodel.SessionViewModel
import com.example.deepworkai.viewmodel.AnalyticsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableFloatStateOf

import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController? = null,
    onNavigateToActiveSession: () -> Unit = {},
    viewModel: SessionViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusService = remember{ FocusService() }
    var isSessionActive by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableStateOf<String?>(null) }
    var focusTimeDisplay by remember { mutableStateOf("0h 00m") }

    var secondsElapsed by remember { mutableIntStateOf(0) }

    val returnedRisk = navController?.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("burnout_risk", null)
        ?.collectAsState()

    var burnoutRisk by remember { mutableStateOf("Low") }
    var showWellDoneDialog by remember { mutableStateOf(false) }

    // Real-time Date & Time State
    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    
    // Cognitive Load from ViewModel
    val cognitiveLoad by viewModel.cognitiveLoad.collectAsState()

    // Update Date & Time every second
    LaunchedEffect(Unit) {
        viewModel.updateCognitiveLoad() // Refresh on entry
        while (true) {
            currentDateTime = LocalDateTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    // Update burnoutRisk and show dialog when a new value is returned
    LaunchedEffect(returnedRisk?.value) {
        val riskValue = returnedRisk?.value as? String
        if (riskValue != null) {
            burnoutRisk = riskValue
            showWellDoneDialog = true
            navController?.currentBackStackEntry?.savedStateHandle?.set("burnout_risk", null)
        }
    }

    var currentScore by remember { mutableIntStateOf(0) }

    // The Ticker: Runs only when isSessionActive is true
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            while (true) {
                kotlinx.coroutines.delay(1000) // Wait 1 second
                secondsElapsed++
            }
        } else {
            secondsElapsed = 0 // Reset when stopped
        }
    }

// Format seconds into "0h 00m 00s"
    val formattedTime = remember(secondsElapsed) {
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        "${hours}h ${String.format("%02dm", minutes)} ${String.format("%02ds", seconds)}"
    }

    val userId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"
    val userName = com.example.deepworkai.network.NetworkPreferences.userName ?: "User"

    val greeting = when (currentDateTime.hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var dailyGoalDisplay by remember { mutableStateOf("----") }

    Scaffold(
        containerColor = DeepWorkBackground,
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToActiveSession() },
                containerColor = DeepWorkBlue,
                shape = CircleShape,
                modifier = Modifier.offset(y = 50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        // We add verticalScroll so the user can see all the ML cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            HomeHeader(userName = userName, greeting = greeting, currentDateTime = currentDateTime)

            Spacer(modifier = Modifier.height(32.dp))

            val todayScore = analyticsViewModel.uiState.value?.todayScore ?: 0
            MainFocusCard(
                focusTime = if (isSessionActive) formattedTime else "0h 00m",
                progress = 12,
                score = todayScore,
                cognitiveLoad = cognitiveLoad,
                dailyGoal = dailyGoalDisplay,
                onDailyGoalClick = { showDailyGoalDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Button ---
            PrimaryButton(
                text = "Start Focus Session",
                containerColor = DeepWorkBlue
            ) {
                val prefs = context.getSharedPreferences("AppTrackingPrefs", android.content.Context.MODE_PRIVATE)
                val isSetupComplete = prefs.getBoolean("is_tracking_setup_complete", false)
                if (!isSetupComplete) {
                    navController?.navigate(Screen.AppSelection.route)
                } else {
                    onNavigateToActiveSession()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- New ML Ready UI Components ---
            val weeklyMinutes = analyticsViewModel.uiState.value?.weeklyDeepMinutes ?: emptyList()
            WeeklyFocusGraph(weeklyMinutes)

            Spacer(modifier = Modifier.height(16.dp))

            GridMetrics(onMetricsClick = {
                navController?.navigate(Screen.DistractionInsights.route)
            })

            Spacer(modifier = Modifier.height(16.dp))

            BurnoutRiskCard(
                riskLevel = burnoutRisk,
                // Change 'color' to 'statusColor' to match the function definition
                statusColor = when (burnoutRisk) {
                    "High" -> Color(0xFFEF4444)   // Red
                    "Medium" -> Color(0xFFFACC15) // Yellow
                    else -> Color(0xFF4ADE80)     // Green
                }
            )

            // Extra spacer so the FAB doesn't hide the last card
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showDailyGoalDialog) {
            AlertDialog(
                onDismissRequest = { showDailyGoalDialog = false },
                containerColor = Color(0xFF13171D),
                title = {
                    DialogTitle("Set Daily Goal")
                },
                text = {
                    DailyGoalContent(
                        currentGoal = dailyGoalDisplay,
                        onGoalSelected = {
                            dailyGoalDisplay = it
                            showDailyGoalDialog = false
                        }
                    )
                },
                confirmButton = {
                    CancelButton(onClick = { showDailyGoalDialog = false })
                }
            )
        }

        // --- Well Done Message ---
        if (showWellDoneDialog) {
            AlertDialog(
                onDismissRequest = { showWellDoneDialog = false },
                containerColor = Color(0xFF13171D),
                title = {
                    DialogTitle("Session Complete! 🎉")
                },
                text = {
                    WellDoneContent(burnoutRisk)
                },
                confirmButton = {
                    AwesomeButton(onClick = { showWellDoneDialog = false })
                }
            )
        }
    }
}

@Composable
fun WellDoneContent(riskValue: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        M3Text("Well done! Your session has been recorded.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        
        val rColor = when (riskValue) {
            "High" -> Color(0xFFEF4444)
            "Medium" -> Color(0xFFFACC15)
            else -> Color(0xFF4ADE80)
        }
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = rColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, rColor.copy(alpha=0.5f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(rColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                M3Text(
                    text = "Burnout Risk: $riskValue",
                    color = rColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyGoalContent(currentGoal: String, onGoalSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val goals = listOf("2h 00m", "4h 00m", "6h 00m", "8h 00m")
        goals.forEach { goal ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoalSelected(goal) },
                color = if (currentGoal == goal) DeepWorkBlue.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentGoal == goal) DeepWorkBlue else Color.White.copy(alpha=0.03f))
            ) {
                M3Text(
                    goal,
                    modifier = Modifier.padding(16.dp),
                    color = if (currentGoal == goal) DeepWorkBlue else Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DialogTitle(text: String) {
    M3Text(text, fontWeight = FontWeight.Bold)
}

@Composable
fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        M3Text("Cancel", color = Color.White)
    }
}

@Composable
fun AwesomeButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        M3Text("Awesome", color = DeepWorkBlue)
    }
}

@Composable
fun HomeHeader(userName: String, greeting: String, currentDateTime: LocalDateTime) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM dd", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            M3Text(
                text = currentDateTime.format(dateFormatter),
                color = DeepWorkTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            M3Text(
                text = currentDateTime.format(timeFormatter),
                color = DeepWorkBlue,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            M3Text(
                text = "$greeting, $userName",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        // Profile Image Placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFCC80)) // Matches your orange profile icon
        )
    }
}

@Composable
fun MainFocusCard(
    focusTime: String,
    progress: Int,
    score: Int,
    cognitiveLoad: String,
    dailyGoal: String,
    onDailyGoalClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DeepWorkSurface)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            Column {
                M3Text("Today's Focus Time", color = DeepWorkTextSecondary, style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.Bottom) {
                    M3Text(focusTime, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    M3Text("↑ $progress%", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        M3Text("Cognitive Load", color = DeepWorkTextSecondary, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Badge(containerColor = Color(0xFF2D2418), contentColor = Color(0xFFEAB308)) {
                            M3Text(cognitiveLoad, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onDailyGoalClick)
                            .padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            M3Text("Daily Goal", color = DeepWorkTextSecondary, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Tune, contentDescription = "Set Goal", tint = DeepWorkTextSecondary, modifier = Modifier.size(12.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        M3Text(dailyGoal, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Circular Score Indicator (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp)
                    .border(2.dp, DeepWorkBlue.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    M3Text(score.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    M3Text("SCORE", color = DeepWorkTextSecondary, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController? = null) {
    // We use a custom Surface to get that specific dark background and top border
    Surface(
        color = DeepWorkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = painterResource(R.drawable.ic_home),
                label = "Home",
                isSelected = true,
                onClick = {
                    navController?.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
            BottomNavItem(
                icon = painterResource(R.drawable.ic_analytics),
                label = "Analytics",
                onClick = {
                    navController?.navigate(Screen.Analytics.route) {
                        launchSingleTop = true
                    }
                }
            )

            // Spacer for the Floating Action Button (+) in the middle
            Spacer(modifier = Modifier.width(48.dp))

            BottomNavItem(
                icon = painterResource(R.drawable.ic_history),
                label = "History",
                onClick = {
                    navController?.navigate(Screen.History.route) {
                        launchSingleTop = true
                    }
                }
            )
            BottomNavItem(
                icon = painterResource(R.drawable.ic_settings),
                label = "Settings",
                onClick = {
                    navController?.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            tint = if (isSelected) DeepWorkBlue else DeepWorkTextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        M3Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) DeepWorkBlue else DeepWorkTextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun WeeklyFocusGraph(weeklyMinutes: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DeepWorkSurface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                M3Text("Weekly Focus (Mins)", color = Color.White, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = DeepWorkTextSecondary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (weeklyMinutes.isEmpty() || weeklyMinutes.all { it == 0 }) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    M3Text("No focus data for this week yet.", color = DeepWorkTextSecondary, fontSize = 14.sp)
                }
            } else {
                // Ensure we have 7 points for the week, padding with 0 if necessary
                val paddedData = weeklyMinutes.takeLast(7).toMutableList()
                while (paddedData.size < 7) paddedData.add(0, 0)
                
                // Animation for drawing the path
                var animationProgress by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(paddedData) {
                    androidx.compose.animation.core.animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = tween(1500, easing = LinearOutSlowInEasing)
                    ) { value, _ -> animationProgress = value }
                }

                val maxMinutes = paddedData.maxOrNull()?.coerceAtLeast(1) ?: 1
                
                val labels = listOf("M", "T", "W", "T", "F", "S", "S")

                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp)) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        val stepX = canvasWidth / (paddedData.size - 1)
                        
                        val points = paddedData.mapIndexed { index, value ->
                            androidx.compose.ui.geometry.Offset(
                                x = index * stepX,
                                y = canvasHeight - (value.toFloat() / maxMinutes * canvasHeight * animationProgress)
                            )
                        }

                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        // Gradient fill
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(canvasWidth, canvasHeight)
                            lineTo(0f, canvasHeight)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(DeepWorkBlue.copy(alpha = 0.5f), Color.Transparent),
                                startY = 0f,
                                endY = canvasHeight
                            )
                        )

                        // Line
                        drawPath(
                            path = path,
                            color = DeepWorkBlue,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 4.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )

                        // Draw dots on the points
                        points.forEachIndexed { index, offset ->
                            val color = if (paddedData[index] == maxMinutes) Color(0xFF4ADE80) else DeepWorkBlue
                            val radius = if (paddedData[index] == maxMinutes) 6.dp.toPx() else 4.dp.toPx()
                            
                            drawCircle(
                                color = Color.White,
                                radius = radius + 2.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = color,
                                radius = radius,
                                center = offset
                            )
                        }
                    }

                    // Labels mapping X axis dynamically
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEachIndexed { index, label ->
                            M3Text(
                                text = label, 
                                color = if (paddedData[index] == maxMinutes) Color(0xFF4ADE80) else DeepWorkTextSecondary, 
                                fontSize = 12.sp,
                                fontWeight = if (paddedData[index] == maxMinutes) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridMetrics(onMetricsClick: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Focus Stability Card
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Focus Stability",
            value = "85%",
            trend = "+4%",
            icon = Icons.Default.Lightbulb,
            iconColor = DeepWorkBlue
        )
        // Distractions Card
        MetricCard(
            modifier = Modifier.weight(1f).clickable {
                android.widget.Toast.makeText(context, "Opening distraction insights...", android.widget.Toast.LENGTH_SHORT).show()
                onMetricsClick()
            },
            title = "DISTRACTIONS",
            value = "12",
            subValue = "spikes",
            isWarning = true
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    trend: String = "",
    subValue: String = "",
    icon: ImageVector? = null,
    iconColor: Color = Color.White,
    isWarning: Boolean = false
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepWorkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(modifier = Modifier.size(32.dp).background(iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(16.dp), tint = iconColor)
                    }
                }
                if (trend.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Badge(containerColor = Color(0xFF143224), contentColor = Color(0xFF4ADE80)) {
                        M3Text(trend, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            M3Text(title, color = DeepWorkTextSecondary, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.Bottom) {
                M3Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (subValue.isNotEmpty()) {
                    M3Text(" $subValue", color = DeepWorkTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
@Composable
fun BurnoutRiskCard(
    riskLevel: String,
    statusColor: Color = Color(0xFF4ADE80) // Add this parameter
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DeepWorkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // This is the little colored dot or badge
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            M3Text(text = "Burnout Risk: ", color = Color.White)
            M3Text(
                text = riskLevel,
                color = statusColor, // Text also changes color
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Preview(
    showBackground = true,
    device = Devices.PIXEL_7,
    name = "Home Screen Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HomeScreenPreview() {
    DeepWorkAITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeepWorkBackground
        ) {
            HomeScreen()
        }
    }
}
