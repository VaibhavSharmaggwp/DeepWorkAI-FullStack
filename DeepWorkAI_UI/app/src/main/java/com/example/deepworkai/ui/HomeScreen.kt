package com.example.deepworkai.ui

import androidx.compose.ui.graphics.graphicsLayer
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
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.deepworkai.viewmodel.SessionViewModel
import com.example.deepworkai.viewmodel.AnalyticsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Send
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import com.example.deepworkai.viewmodel.WellnessViewModel

import kotlinx.coroutines.Dispatchers

@Composable
fun HomeScreen(
    navController: NavController? = null,
    onNavigateToActiveSession: () -> Unit = {},
    viewModel: SessionViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    wellnessViewModel: WellnessViewModel = viewModel(),
    profileViewModel: com.example.deepworkai.viewmodel.ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user
    
    LaunchedEffect(Unit) {
        profileViewModel.fetchProfile()
    }
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
    
    var showChatbot by remember { mutableStateOf(false) }

    // Real-time Date & Time State
    var currentDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Cognitive Load from ViewModel
    val cognitiveLoad by viewModel.cognitiveLoad.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.fetchProfile()
    }

    // Update Date & Time every second
    LaunchedEffect(Unit) {
        viewModel.updateCognitiveLoad() // Refresh on entry
        while (true) {
            currentDateTime = Calendar.getInstance()
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

    val greeting = when (currentDateTime.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var dailyGoalDisplay by remember { mutableStateOf("2h 00m") }

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

            val profileImageUrl = com.example.deepworkai.network.NetworkPreferences.backendUrl + (user?.imageUrl ?: "")
            val streakCount = analyticsViewModel.uiState.value?.currentStreak ?: 0
            HomeHeader(
                userName = userName, 
                greeting = greeting, 
                currentDateTime = currentDateTime,
                imageUrl = if (user?.imageUrl != null) profileImageUrl else null,
                onProfileClick = { navController?.navigate(Screen.Profile.route) },
                streakCount = streakCount
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            QuoteCard()

            Spacer(modifier = Modifier.height(24.dp))

            val todayScore = analyticsViewModel.uiState.value?.todayScore ?: 0
            val stabilityTrend = analyticsViewModel.uiState.value?.trend ?: "+0%"
            MainFocusCard(
                focusTime = if (isSessionActive) formattedTime else "0h 00m",
                trend = stabilityTrend,
                score = if (todayScore > 0) todayScore else (user?.focusScore ?: 0),
                cognitiveLoad = cognitiveLoad,
                dailyGoal = dailyGoalDisplay,
                onDailyGoalClick = { showDailyGoalDialog = true },
                onScoreClick = { navController?.navigate(Screen.Analytics.route) }
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

            val contextSwitches = analyticsViewModel.uiState.value?.contextSwitches ?: 0
            GridMetrics(
                distractionsCount = contextSwitches.toString(),
                focusStability = todayScore,
                trend = stabilityTrend,
                onMetricsClick = {
                    navController?.navigate(Screen.FlowInsights.route)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Focus Wellness
            WellnessChecklist(
                wellnessLog = wellnessViewModel.wellnessLog.value,
                onUpdate = { s, h, m, e -> wellnessViewModel.updateWellness(s, h, m, e) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            BurnoutRiskCard(
                riskLevel = burnoutRisk,
                // Change 'color' to 'statusColor' to match the function definition
                statusColor = when (burnoutRisk) {
                    "High" -> Color(0xFFEF4444)   // Red
                    "Medium" -> Color(0xFFFACC15) // Yellow
                    else -> Color(0xFF4ADE80)     // Green
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Animated Chat Banner
            AIChatBanner(onClick = { showChatbot = true })

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Planner Entry
            SmartPlannerCard(onClick = { navController?.navigate(Screen.TaskPlanner.route) })

            // Extra spacer so the FAB doesn't hide the last card
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showDailyGoalDialog) {
            AlertDialog(
                onDismissRequest = { showDailyGoalDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
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
                containerColor = MaterialTheme.colorScheme.surface,
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

        if (showChatbot) {
            AIChatbotBottomSheet(
                onDismiss = { showChatbot = false },
                focusService = focusService
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
fun QuoteCard() {
    val quotes = listOf(
        "\"Focus is a matter of deciding what things you're not going to do.\" - John Carmack",
        "\"The successful warrior is the average man, with laser-like focus.\" - Bruce Lee",
        "\"Starve your distractions, feed your focus.\" - Unknown",
        "\"Deep work is the superpower of the 21st century.\" - Cal Newport",
        "\"Always remember, your focus determines your reality.\" - George Lucas"
    )
    val randomQuote = remember { quotes.random() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepWorkBlue.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeepWorkBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DeepWorkBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            M3Text(
                text = randomQuote,
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HomeHeader(userName: String, greeting: String, currentDateTime: Calendar, imageUrl: String? = null, onProfileClick: () -> Unit = {}, streakCount: Int = 0) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
            M3Text(
                text = dateFormatter.format(currentDateTime.time),
                color = DeepWorkTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            M3Text(
                text = timeFormatter.format(currentDateTime.time),
                color = DeepWorkBlue,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                M3Text(
                    text = "$greeting, $userName",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        }
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, DeepWorkBlue.copy(alpha = 0.5f), CircleShape)
                .clickable { onProfileClick() }
        ) {
            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    tint = Color.Gray, 
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun MainFocusCard(
    focusTime: String,
    trend: String,
    score: Int,
    cognitiveLoad: String,
    dailyGoal: String,
    onDailyGoalClick: () -> Unit = {},
    onScoreClick: () -> Unit = {}
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
                    
                    val isPositive = trend.startsWith("+")
                    val trendColor = if (isPositive) Color(0xFF4ADE80) else Color(0xFFEF4444)
                    val arrow = if (isPositive) "↑" else "↓"
                    
                    M3Text("$arrow $trend", color = trendColor, fontWeight = FontWeight.Bold)
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
                    .border(2.dp, DeepWorkBlue.copy(alpha = 0.3f), CircleShape)
                    .clickable { onScoreClick() },
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
                
                val labels = remember {
                    val cal = java.util.Calendar.getInstance()
                    val sdf = java.text.SimpleDateFormat("E", java.util.Locale.getDefault())
                    (0..6).map { i ->
                        val tempCal = cal.clone() as java.util.Calendar
                        tempCal.add(java.util.Calendar.DAY_OF_YEAR, -(6 - i))
                        sdf.format(tempCal.time).take(1)
                    }
                }

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
                            val color = if (index == 6) Color(0xFFEAB308) else DeepWorkBlue
                            val radius = if (index == 6) 6.dp.toPx() else 4.dp.toPx()
                            
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
                                color = if (index == 6) Color(0xFFEAB308) else DeepWorkTextSecondary, 
                                fontSize = 12.sp,
                                fontWeight = if (index == 6) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridMetrics(distractionsCount: String = "0", focusStability: Int = 85, trend: String = "+0%", onMetricsClick: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Focus Stability Card
        val stabilityColor = if (focusStability >= 80) Color(0xFF4ADE80) else Color(0xFFEF4444)
        MetricCard(
            modifier = Modifier.weight(1f),
            title = "Focus Stability",
            value = "$focusStability%",
            trend = trend,
            icon = Icons.Default.Lightbulb,
            iconColor = stabilityColor,
            valueColor = stabilityColor
        )
        // Flow Labs Card
        MetricCard(
            modifier = Modifier.weight(1f).clickable {
                onMetricsClick()
            },
            title = "FLOW LABS",
            value = "DEEP INSIGHTS",
            subValue = "",
            icon = Icons.Default.AutoAwesome,
            iconColor = DeepWorkBlue,
            valueColor = Color.White
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
    valueColor: Color = Color.White,
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
                    val isPositive = trend.startsWith("+")
                    val badgeBg = if (isPositive) Color(0xFF143224) else Color(0xFF321414)
                    val badgeContent = if (isPositive) Color(0xFF4ADE80) else Color(0xFFEF4444)
                    
                    Badge(containerColor = badgeBg, contentColor = badgeContent) {
                        M3Text(trend, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            M3Text(title, color = DeepWorkTextSecondary, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.Bottom) {
            val adaptiveFontSize = if (value.length > 8) 18.sp else 28.sp
            M3Text(value, color = valueColor, fontSize = adaptiveFontSize, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatbotBottomSheet(
    onDismiss: () -> Unit,
    focusService: com.example.deepworkai.network.FocusService
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var inputSchedule by remember { mutableStateOf("9 AM - 5 PM Work") }
    
    val messages = remember { androidx.compose.runtime.mutableStateListOf<Pair<Boolean, String>>(
        Pair(false, "Hi! I am your AI Productivity Assistant. How can I help you today?")
    ) }
    
    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = DeepWorkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 400.dp, max = 600.dp)
        ) {
            M3Text("DeepWork AI Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    val isUser = message.first
                    val text = message.second
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUser) DeepWorkBlue else Color(0xFF2A2A2A),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            M3Text(
                                text = text,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepWorkBlue, strokeWidth = 2.dp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputSchedule,
                onValueChange = { inputSchedule = it },
                label = { M3Text("Your Daily Schedule context", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepWorkBlue,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { M3Text("Ask me something...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepWorkBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            val userQuery = inputQuery
                            val userSchedule = inputSchedule
                            messages.add(Pair(true, userQuery))
                            inputQuery = ""
                            isLoading = true
                            
                            scope.launch(Dispatchers.IO) {
                                val response = focusService.askAIAssistant(userQuery, userSchedule)
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    isLoading = false
                                    if (response != null) {
                                        messages.add(Pair(false, response.reply))
                                    } else {
                                        messages.add(Pair(false, "Sorry, I couldn't reach the server."))
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(DeepWorkBlue, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AIChatBanner(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Pulsating animation scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glowing border alpha
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeepWorkBlue.copy(alpha = alpha))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DeepWorkBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "AI", tint = DeepWorkBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                M3Text(
                    text = "Ask AI Assistant",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                M3Text(
                    text = "Analyze my focus score & schedule",
                    color = DeepWorkTextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.PlayArrow, contentDescription = "Go", tint = DeepWorkBlue)
        }
    }
}

@Composable
fun SmartPlannerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6366F1).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFF818CF8))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                M3Text(
                    text = "Smart Planner",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                M3Text(
                    text = "Sync tasks with your focus peaks",
                    color = DeepWorkTextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = DeepWorkTextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun WellnessChecklist(
    wellnessLog: com.example.deepworkai.models.WellnessLog?,
    onUpdate: (Int?, Int?, Boolean?, Boolean?) -> Unit
) {
    Column {
        M3Text(
            text = "Daily Focus Wellness",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepWorkSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                WellnessRow(
                    icon = Icons.Default.Bedtime,
                    title = "Sleep",
                    value = "${wellnessLog?.sleepHours ?: 0}h",
                    onIncrement = { onUpdate((wellnessLog?.sleepHours ?: 0) + 1, null, null, null) },
                    onDecrement = { onUpdate(((wellnessLog?.sleepHours ?: 0) - 1).coerceAtLeast(0), null, null, null) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                WellnessRow(
                    icon = Icons.Default.WaterDrop,
                    title = "Hydration",
                    value = "${wellnessLog?.hydrationLevel ?: 0} glasses",
                    onIncrement = { onUpdate(null, (wellnessLog?.hydrationLevel ?: 0) + 1, null, null) },
                    onDecrement = { onUpdate(null, ((wellnessLog?.hydrationLevel ?: 0) - 1).coerceAtLeast(0), null, null) }
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WellnessToggle(
                        icon = Icons.Default.SelfImprovement,
                        title = "Meditated",
                        isSelected = wellnessLog?.meditated ?: false,
                        onClick = { onUpdate(null, null, !(wellnessLog?.meditated ?: false), null) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    WellnessToggle(
                        icon = Icons.Default.FitnessCenter,
                        title = "Exercise",
                        isSelected = wellnessLog?.exercise ?: false,
                        onClick = { onUpdate(null, null, null, !(wellnessLog?.exercise ?: false)) }
                    )
                }
            }
        }
    }
}

@Composable
fun WellnessRow(
    icon: ImageVector,
    title: String,
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DeepWorkBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        M3Text(text = title, color = Color.White, modifier = Modifier.weight(1f))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = DeepWorkTextSecondary)
            }
            M3Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DeepWorkBlue)
            }
        }
    }
}

@Composable
fun RowScope.WellnessToggle(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.weight(1f).clickable { onClick() },
        color = if (isSelected) DeepWorkBlue.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DeepWorkBlue else Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) DeepWorkBlue else DeepWorkTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            M3Text(text = title, color = if (isSelected) Color.White else DeepWorkTextSecondary, fontSize = 12.sp)
        }
    }
}

