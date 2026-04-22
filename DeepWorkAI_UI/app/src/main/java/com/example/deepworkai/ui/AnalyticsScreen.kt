package com.example.deepworkai.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deepworkai.R
import com.example.deepworkai.models.AnalyticsDashboard
import com.example.deepworkai.viewmodel.AnalyticsViewModel
import com.example.deepworkai.viewmodel.SessionViewModel

@Composable
fun AnalyticsScreen(
    navController: NavController? = null,
    viewModel: AnalyticsViewModel = viewModel(),
    sessionViewModel: SessionViewModel = viewModel()
) {
    val data by viewModel.uiState
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        sessionViewModel.updateCognitiveLoad() // Refresh dynamic load on entry
    }

    val uId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"

    AnalyticsContent(
        data = data,
        isLoading = isLoading,
        selectedPeriod = viewModel.selectedPeriod.value,
        navController = navController,
        onTogglePeriod = { viewModel.togglePeriod() },
        onRetry = { viewModel.fetchAnalytics(uId, viewModel.selectedPeriod.value.lowercase()) }
    )
}

@Composable
fun AnalyticsContent(
    data: AnalyticsDashboard?,
    isLoading: Boolean,
    selectedPeriod: String = "Weekly",
    navController: NavController? = null,
    onTogglePeriod: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color(0xFF0D1117),
        bottomBar = { AnalyticsBottomNavigationBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val hasData = data != null && data.weeklyScores.isNotEmpty()
            if (isLoading && data == null && !hasData) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3B82F6)
                )
            } else if (hasData) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    AnalyticsHeader()

                    Spacer(modifier = Modifier.height(28.dp))

                    TimeToggleSection(selectedPeriod = selectedPeriod, onToggle = onTogglePeriod)

                    Spacer(modifier = Modifier.height(32.dp))

                    FocusScoreCard(
                        score = data.todayScore,
                        scoreTrend = data.trend,
                        weeklyScores = data.weeklyScores,
                        isMonthly = selectedPeriod == "Monthly"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val hours = String.format("%.1fh", data.totalDeepMinutes / 60.0)
                        CognitiveLoadCard(modifier = Modifier.weight(1f), hours = hours)
                        DistractionsCard(modifier = Modifier.weight(1.1f), heatmap = data.heatmap)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Insights",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    InsightItem(
                        icon = Icons.Default.Psychology,
                        iconColor = Color(0xFF3B82F6),
                        title = "Cognitive Peak",
                        badge = "HIGH CONFIDENCE",
                        description = data.cognitivePeakInsight, // Crazy kiya re
                        action = "Schedule Focus Block"
                    )

                    InsightItem(
                        icon = Icons.Default.Bolt,
                        iconColor = Color(0xFFF59E0B),
                        title = "Consistency",
                        description = data.consistencyInsight // ohoo mza aagya
                    )

                    InsightItem(
                        icon = Icons.Default.NotificationsOff,
                        iconColor = Color(0xFF8B5CF6),
                        title = "Context Switching",
                        description = data.switchesInsight // ye bni na baat
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else if (!isLoading) {
                // Empty state or Error state
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No Analytics Data",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "We couldn't find any session data for you. Start your first deep work session to see your insights!",
                        color = Color(0xFF94A3B8),
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
                    ) {
                        Text("Retry Fetch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Analytics",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Deep Work Performance",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color(0xFFFFCC99)
        ) {}
    }
}

@Composable
fun TimeToggleSection(selectedPeriod: String, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF161B22), RoundedCornerShape(28.dp))
            .padding(4.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggle() }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(if (selectedPeriod == "Weekly") Color(0xFF2563EB) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text("Weekly", color = if (selectedPeriod == "Weekly") Color.White else Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(if (selectedPeriod == "Monthly") Color(0xFF2563EB) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text("Monthly", color = if (selectedPeriod == "Monthly") Color.White else Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FocusScoreCard(
    score: Int = 84,
    scoreTrend: String = "+12%",
    weeklyScores: List<Int> = listOf(40, 60, 55, 70, 84, 80, 92),
    isMonthly: Boolean = false
) {
    Surface(
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Focus Score", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(score.toString(), color = Color(0xFF2563EB), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(" $scoreTrend", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val avg = if (weeklyScores.isNotEmpty()) weeklyScores.average().toInt() else 0
                    Text(
                        "Avg: $avg",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (weeklyScores.isEmpty()) {
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Text("NO DATA RECORDED OF LAST ${if(isMonthly) 30 else 7} DAYS", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                } else {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                      val width = size.width
                      val height = size.height
                      
                      // Draw horizontal grid lines (dotted)
                      val intervals = 4
                      for (i in 0..intervals) {
                          val y = height * i / intervals
                          drawLine(
                              color = Color.White.copy(alpha = 0.05f),
                              start = Offset(0f, y),
                              end = Offset(width, y),
                              strokeWidth = 1.dp.toPx()
                          )
                      }

                      // Calculate points based on dynamic data
                      val points = weeklyScores.mapIndexed { index, s ->
                          Offset(
                              x = width * (index.toFloat() / (weeklyScores.size - 1).coerceAtLeast(1)),
                              y = height * (1f - (s.toFloat() / 100f))
                          )
                      }

                      val path = Path().apply {
                          if (points.isNotEmpty()) {
                              moveTo(points[0].x, points[0].y)
                              for (i in 1 until points.size) {
                                  val prev = points[i - 1]
                                  val curr = points[i]
                                  val cp1 = Offset(prev.x + (curr.x - prev.x) / 2, prev.y)
                                  val cp2 = Offset(prev.x + (curr.x - prev.x) / 2, curr.y)
                                  cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                              }
                          }
                      }

                      // Draw Gradient Fill
                      val fillPath = Path().apply {
                          addPath(path)
                          lineTo(width, height)
                          lineTo(0f, height)
                          close()
                      }
                      drawPath(
                          path = fillPath,
                          brush = Brush.verticalGradient(
                              colors = listOf(Color(0xFF2563EB).copy(alpha = 0.3f), Color.Transparent)
                          )
                      )

                      // Draw Curve Stroke
                      drawPath(
                          path = path,
                          color = Color(0xFF3B82F6),
                          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                      )

                      // Draw points
                      points.forEachIndexed { index, point ->
                          if (index > 0 && index < points.size - 1) {
                              drawCircle(
                                  color = Color(0xFF3B82F6),
                                  radius = 4.dp.toPx(),
                                  center = point
                              )
                          }
                      }

                      // Highlight Lat Point
                      if (points.isNotEmpty()) {
                          val lastPoint = points.last()
                          drawCircle(
                              color = Color.White,
                              radius = 6.dp.toPx(),
                              center = lastPoint
                          )
                          drawCircle(
                              color = Color(0xFF3B82F6),
                              radius = 4.dp.toPx(),
                              center = lastPoint
                          )
                      }
                  }

                  // Tooltip for Current
                  Surface(
                      modifier = Modifier
                          .align(Alignment.TopEnd)
                          .padding(end = 10.dp, top = 10.dp),
                      color = Color(0xFF2563EB),
                      shape = RoundedCornerShape(8.dp)
                  ) {
                      Text(
                          "${score}",
                          color = Color.White,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          fontSize = 12.sp,
                          fontWeight = FontWeight.Bold
                      )
                  }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isMonthly) {
                    Text("30 Days Ago", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("Today", color = Color(0xFF94A3B8), fontSize = 11.sp)
                } else {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                        Text(it, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CognitiveLoadCard(modifier: Modifier = Modifier, hours: String = "5.2h") {
    Surface(
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Cognitive Load",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF2563EB),
                        startAngle = -90f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(hours, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("DEEP", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnalyticsLegendItem(Color(0xFF2563EB), "Deep")
                Spacer(modifier = Modifier.width(12.dp))
                AnalyticsLegendItem(Color(0xFF475569), "Shallow")
            }
        }
    }
}

@Composable
fun AnalyticsLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp)
    }
}

@Composable
fun DistractionsCard(modifier: Modifier = Modifier, heatmap: List<Int> = List(16) { 0 }) {
    Surface(
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Distractions", color = Color(0xFF94A3B8), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heatmap Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val grid = heatmap.chunked(4)
                val maxDistrations = heatmap.maxOrNull()?.coerceAtLeast(1) ?: 1
                
                grid.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { count ->
                            val intensity = (count.toFloat() / maxDistrations.toFloat()).coerceIn(0.1f, 1f)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Color(0xFF2563EB).copy(alpha = intensity),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "High distractions detected between 2 PM - 4 PM.",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun InsightItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    badge: String? = null,
    description: String,
    action: String? = null
) {
    Surface(
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (badge != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    badge,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(description, color = Color(0xFF94A3B8), fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "$action →",
                    color = Color(0xFF3B82F6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 60.dp)
                )
            }
        }
    }
}

@Composable
fun AnalyticsBottomNavigationBar(navController: NavController? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color(0xFF0D1117))
    ) {
        Surface(
            color = Color(0xFF161B22),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnalyticsNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = false,
                    onClick = {
                        navController?.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
                AnalyticsNavItem(
                    icon = Icons.Default.Assessment,
                    label = "Analytics",
                    isSelected = true,
                    onClick = {
                        navController?.navigate(Screen.Analytics.route) {
                            launchSingleTop = true
                        }
                    }
                )
                AnalyticsNavItem(
                    icon = Icons.Default.History,
                    label = "History",
                    isSelected = false,
                    onClick = {
                        navController?.navigate(Screen.History.route) {
                            launchSingleTop = true
                        }
                    }
                )
                AnalyticsNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = false,
                    onClick = {
                        navController?.navigate(Screen.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-5).dp)
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .blur(20.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.5f), CircleShape)
            )
            FloatingActionButton(
                onClick = {
                    navController?.navigate("active_session")
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AnalyticsNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF3B82F6) else Color(0xFF475569),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF475569),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun AnalyticsScreenPreview() {
    val mockData = com.example.deepworkai.models.AnalyticsDashboard(
        weeklyScores = listOf(40, 60, 55, 70, 84, 80, 92),
        totalDeepMinutes = 312, // 5.2h
        contextSwitches = 42,
        heatmap = listOf(1, 2, 4, 1, 0, 5, 2, 0, 2, 0, 3, 0, 0, 0, 2, 0),
        todayScore = 84,
        trend = "+12%"
    )
    AnalyticsContent(data = mockData, isLoading = false, selectedPeriod = "Weekly")
}