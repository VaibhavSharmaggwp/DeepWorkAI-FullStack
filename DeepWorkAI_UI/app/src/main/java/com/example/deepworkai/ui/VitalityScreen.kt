package com.example.deepworkai.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deepworkai.ui.theme.*
import com.example.deepworkai.viewmodel.AnalyticsViewModel
import com.example.deepworkai.viewmodel.WellnessViewModel

@Composable
fun VitalityScreen(
    navController: NavController,
    wellnessViewModel: WellnessViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val wellnessLog by wellnessViewModel.wellnessLog
    val analyticsData by analyticsViewModel.uiState

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
                    text = "Vitality & Focus",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Daily Vitality Score Card
            val sleepScore = ((wellnessLog?.sleepHours ?: 0) * 12.5f).coerceIn(0f, 100f)
            val hydrationScore = ((wellnessLog?.hydrationLevel ?: 0) * 10f).coerceIn(0f, 100f)
            val vitalityScore = ((sleepScore + hydrationScore) / 2).toInt()
            
            VitalityScoreCard(score = vitalityScore)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Physical Resilience",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResilienceItem(
                    modifier = Modifier.weight(1f),
                    title = "Exercise",
                    isDone = wellnessLog?.exercise ?: false,
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFFF87171)
                )
                ResilienceItem(
                    modifier = Modifier.weight(1f),
                    title = "Meditation",
                    isDone = wellnessLog?.meditated ?: false,
                    icon = Icons.Default.SelfImprovement,
                    color = Color(0xFF818CF8)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Focus-Wellness Correlation Chart (Mock visualization)
            Text(
                "Focus Correlation",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CorrelationChart()

            Spacer(modifier = Modifier.height(32.dp))

            // AI Vitality Insight
            VitalityAIInsightCard(vitalityScore = vitalityScore, focusScore = analyticsData?.todayScore ?: 0)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VitalityScoreCard(score: Int) {
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("DAILY VITALITY SCORE", color = DeepWorkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = score / 100f,
                    color = DeepWorkBlue,
                    strokeWidth = 12.dp,
                    modifier = Modifier.size(160.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("OPTIMAL", color = Color(0xFF4ADE80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Your physical energy is the fuel for your deep work sessions.",
                color = DeepWorkTextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ResilienceItem(modifier: Modifier, title: String, isDone: Boolean, icon: ImageVector, color: Color) {
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (isDone) {
                Text("COMPLETED", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("PENDING", color = DeepWorkTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CorrelationChart() {
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.BottomCenter) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val mockData = listOf(0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 0.9f, 0.85f)
                mockData.forEachIndexed { index, height ->
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (index == 5) DeepWorkBlue else Color.White.copy(alpha = 0.1f)
                            )
                    )
                }
            }
            Text("Focus Score vs Sleep (Last 7 Days)", color = DeepWorkTextSecondary, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun VitalityAIInsightCard(vitalityScore: Int, focusScore: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFEAB308).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = "AI Insight", tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI VITALITY INSIGHT", color = Color(0xFFEAB308), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val insight = if (vitalityScore > 70) {
                "Your high vitality is directly correlated with your $focusScore focus score today. Keep this rhythm to avoid burnout."
            } else {
                "Increasing your hydration by 2 glasses could potentially boost your focus stability by 12% based on your history."
            }
            Text(
                text = insight,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
