package com.example.deepworkai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepworkai.models.FocusSession
import com.example.deepworkai.ui.theme.*


@Composable
fun AnalyticsScreen(sessions: List<FocusSession>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Focus History",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // This is the scrollable list of your ML-scored sessions
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions) { session ->
                SessionHistoryItem(session)
            }
        }
    }
}

@Composable
fun SessionHistoryItem(session: FocusSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepWorkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Deep Work Session", color = Color.White, fontWeight = FontWeight.Bold)
                // Basic date formatting from the ISO string
                Text(text = session.startTime.substring(0, 10), color = DeepWorkTextSecondary, fontSize = 12.sp)
            }
            Text(
                text = "${session.focusScore}%",
                color = if (session.focusScore > 70) Color(0xFF4ADE80) else Color(0xFFFACC15),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}