package com.example.deepworkai.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepworkai.models.FocusSession
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionHistoryItem(
    session: FocusSession,
    onClick: () -> Unit = {}
) {
    val durationMin = try {
        val start = java.time.OffsetDateTime.parse(session.startTime)
        val end = session.endTime?.let { java.time.OffsetDateTime.parse(it) } ?: start
        java.time.Duration.between(start, end).toMinutes()
    } catch (_: Exception) { 0 }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // Navigation trigger
        color = Color(0xFF161B22).copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini Stability Ring
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = session.focusScore / 100f,
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFF1E293B),
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${session.focusScore}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Focus Block", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$durationMin min • ${session.distractions} distractions",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    session.cognitiveLoad,
                    color = if (session.cognitiveLoad == "Optimal") Color(0xFF10B981) else Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun SessionHistoryItemPreview() {
    val mock = FocusSession("1", "userId", java.time.OffsetDateTime.now().toString(), null, 87, 2, 42, "Optimal")
    SessionHistoryItem(session = mock)
}