package com.example.deepworkai.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepworkai.models.SessionSummaryResponse
import com.example.deepworkai.viewmodel.SessionViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionSummaryScreen(
    navController: NavController,
    viewModel: SessionViewModel = viewModel(),
    onSave: () -> Unit = {},
    onViewDetailed: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val currentSession = viewModel.currentSession.collectAsState().value
    val stability = currentSession?.session?.focusScore ?: 0
    val duration = (currentSession?.session as? com.example.deepworkai.models.FocusSession)?.let { 
        val start = java.time.LocalDateTime.parse(it.startTime)
        val end = it.endTime?.let { e -> java.time.LocalDateTime.parse(e) } ?: start
        java.time.Duration.between(start, end).toMinutes()
    } ?: 0
    val distractionsCount = currentSession?.session?.distractions ?: 0
    val distractionLevel = if (distractionsCount > 5) "High" else if (distractionsCount > 2) "Medium" else "Low"
    val avgBlock = if (distractionsCount == 0 && duration > 0) duration else if (duration > 0) duration / (distractionsCount + 1) else 0
    val cogLoad = currentSession?.session?.cognitiveLoad ?: "Optimal"

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { innerPadding -> // Changed 'padding' to 'innerPadding' to avoid conflicts
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Session Summary",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = onClose,
                    color = Color(0xFF1E293B),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 🔵 PART 1: The Big Stability Ring
            FocusStabilityCard(stability = stability)

            Spacer(modifier = Modifier.height(24.dp))

            // 🟢 PART 2: The 2x2 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    iconColor = Color(0xFF3B82F6),
                    label = "Total Duration",
                    value = duration.toString(),
                    unit = "min"
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.NotificationsPaused,
                    iconColor = Color(0xFFF59E0B),
                    label = "Distractions",
                    value = distractionsCount.toString(),
                    status = distractionLevel
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Layers,
                    iconColor = Color(0xFF10B981),
                    label = "Avg. Deep Block",
                    value = avgBlock.toString(),
                    unit = "min"
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Psychology,
                    iconColor = Color(0xFF8B5CF6),
                    label = "Cognitive Load",
                    value = cogLoad
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 🔵 PART 3: Save Button
            Button(
                onClick = { 
                    onSave() // Triggers the navigation back to home or the closure of this sheet from the caller
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("history_screen") }) { // 🚀 Navigate to history
                Text(
                    "View Detailed Insights",
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    unit: String? = null,
    status: String? = null
) {
    Surface(
        modifier = modifier.height(130.dp),
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (unit != null) Text(" $unit", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                if (status != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(status, color = Color(0xFF475569), fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}

@Composable
fun FocusStabilityCard(stability: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF161B22).copy(alpha = 0.4f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("FOCUS STABILITY", color = Color(0xFF3B82F6), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(140.dp).blur(30.dp).background(Color(0xFF2563EB).copy(0.1f), CircleShape))
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawArc(color = Color(0xFF1E293B), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color = Color(0xFF2563EB), startAngle = -90f, sweepAngle = (stability / 100f) * 360f, useCenter = false, style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                }
                Row(verticalAlignment = Alignment.Top) {
                    Text("$stability", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                    Text("%", color = Color(0xFF94A3B8), fontSize = 28.sp, modifier = Modifier.padding(top = 16.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Excellent work! You maintained high focus depth.", color = Color(0xFF94A3B8), fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun SessionSummaryPreview() {
    // Mocking the data we expect from Ktor
    val mockSummary = SessionSummaryResponse(
        stabilityScore = 87,
        durationMin = 45,
        distractions = 2,
        distractionLevel = "Low",
        avgDeepBlock = 21,
        cognitiveLoad = "Optimal"
    )

    SessionSummaryScreen(
        navController = rememberNavController()
    )
}