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

enum class ColorGameState {
    START, PLAYING, GAME_OVER
}

data class StroopColor(val name: String, val color: Color)

val stroopColors = listOf(
    StroopColor("RED", Color(0xFFEF4444)),
    StroopColor("BLUE", Color(0xFF3B82F6)),
    StroopColor("GREEN", Color(0xFF10B981)),
    StroopColor("YELLOW", Color(0xFFEAB308)),
    StroopColor("PURPLE", Color(0xFFA855F7))
)

@Composable
fun ColorMatchGame(
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user
    var gameState by remember { mutableStateOf(ColorGameState.START) }
    var score by remember { mutableIntStateOf(0) }
    
    var word by remember { mutableStateOf(stroopColors[0]) }
    var displayColor by remember { mutableStateOf(stroopColors[0].color) }
    var timeLeft by remember { mutableIntStateOf(30) }
    
    fun nextRound() {
        val isMatch = (0..1).random() == 1
        word = stroopColors.random()
        displayColor = if (isMatch) word.color else stroopColors.filter { it != word }.random().color
    }

    fun startGame() {
        score = 0
        timeLeft = 30
        nextRound()
        gameState = ColorGameState.PLAYING
    }

    LaunchedEffect(gameState, timeLeft) {
        if (gameState == ColorGameState.PLAYING && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) {
                gameState = ColorGameState.GAME_OVER
                viewModel.recordCognitiveResult(score / 5, score) // approx level
            }
        }
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
            Text("Color Match", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
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

        if (gameState == ColorGameState.PLAYING) {
            LinearProgressIndicator(
                progress = { (timeLeft.toFloat() / 30f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (timeLeft > 10) DeepWorkBlue else Color.Red,
                trackColor = DeepWorkSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Score: $score", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            
            // The word to guess
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(word.name, color = displayColor, fontSize = 56.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Does the meaning match the color?", color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (word.color == displayColor) {
                            score += 10
                            nextRound()
                        } else {
                            gameState = ColorGameState.GAME_OVER
                            viewModel.recordCognitiveResult(score / 5, score)
                        }
                    },
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("YES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                Button(
                    onClick = {
                        if (word.color != displayColor) {
                            score += 10
                            nextRound()
                        } else {
                            gameState = ColorGameState.GAME_OVER
                            viewModel.recordCognitiveResult(score / 5, score)
                        }
                    },
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("NO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            
        } else {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                if (gameState == ColorGameState.GAME_OVER) "Game Over! Score: $score" else "Does the word match the text color?",
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
                Text(if (gameState == ColorGameState.START) "START GAME" else "TRY AGAIN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
