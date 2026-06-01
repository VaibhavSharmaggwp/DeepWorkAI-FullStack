package com.example.deepworkai.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepworkai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ReactionGameState {
    START, WAITING, READY, GAME_OVER
}

@Composable
fun ReactionSpeedGame(
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user
    var gameState by remember { mutableStateOf(ReactionGameState.START) }
    var score by remember { mutableIntStateOf(0) }
    var lastReactionTime by remember { mutableLongStateOf(0L) }
    
    var greenTime by remember { mutableLongStateOf(0L) }
    
    fun nextRound() {
        gameState = ReactionGameState.WAITING
        scope.launch {
            val waitTimeMs = (1500..4000).random().toLong()
            delay(waitTimeMs)
            if (gameState == ReactionGameState.WAITING) {
                gameState = ReactionGameState.READY
                greenTime = System.currentTimeMillis()
            }
        }
    }

    fun startGame() {
        score = 0
        nextRound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Reaction Speed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = "Streak", tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${user?.cognitiveStreak ?: 0}", color = Color(0xFFF97316), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (gameState == ReactionGameState.WAITING || gameState == ReactionGameState.READY) {
            Text("Score: $score", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            
            // The Target Area
            val boxColor = if (gameState == ReactionGameState.READY) Color(0xFF10B981) else Color(0xFFEF4444)
            val boxText = if (gameState == ReactionGameState.READY) "TAP NOW!" else "WAIT..."
            
            Surface(
                color = boxColor,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable {
                        if (gameState == ReactionGameState.WAITING) {
                            // Tapped too early
                            gameState = ReactionGameState.GAME_OVER
                            viewModel.recordCognitiveResult(score / 500, score)
                        } else if (gameState == ReactionGameState.READY) {
                            val reactTime = System.currentTimeMillis() - greenTime
                            lastReactionTime = reactTime
                            
                            if (reactTime > 1500) {
                                // Too slow
                                gameState = ReactionGameState.GAME_OVER
                                viewModel.recordCognitiveResult(score / 500, score)
                            } else {
                                val points = (1500 - reactTime).toInt().coerceAtLeast(0)
                                score += points
                                nextRound()
                            }
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(boxText, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            if (lastReactionTime > 0) {
                Text("Last Reaction: ${lastReactionTime}ms", color = Color.Gray, fontSize = 16.sp)
            }
            
        } else {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                if (gameState == ReactionGameState.GAME_OVER) "Game Over! Score: $score" else "Wait for GREEN, then tap as fast as possible.",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { startGame() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (gameState == ReactionGameState.START) "START GAME" else "TRY AGAIN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
