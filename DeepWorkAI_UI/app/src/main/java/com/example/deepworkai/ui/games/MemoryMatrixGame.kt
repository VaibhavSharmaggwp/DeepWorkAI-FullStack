package com.example.deepworkai.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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

enum class MatrixGameState {
    START, SHOWING_PATTERN, WAITING_FOR_INPUT, LEVEL_COMPLETE, GAME_OVER
}

@Composable
fun MemoryMatrixGame(
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user
    var gameState by remember { mutableStateOf(MatrixGameState.START) }
    var level by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    
    var pattern by remember { mutableStateOf(setOf<Int>()) }
    var userPattern by remember { mutableStateOf(setOf<Int>()) }
    var incorrectTaps by remember { mutableStateOf(setOf<Int>()) }
    
    val gridSize = 16 // 4x4
    val gridItems = (0 until gridSize).toList()

    fun startLevel() {
        val numTiles = (level + 2).coerceAtMost(10)
        val newPattern = mutableSetOf<Int>()
        while (newPattern.size < numTiles) {
            newPattern.add((0 until gridSize).random())
        }
        pattern = newPattern
        userPattern = setOf()
        incorrectTaps = setOf()
        gameState = MatrixGameState.SHOWING_PATTERN
        
        scope.launch {
            delay(2000) // show pattern for 2 seconds
            gameState = MatrixGameState.WAITING_FOR_INPUT
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
            Text("Memory Matrix", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
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

        if (gameState != MatrixGameState.START && gameState != MatrixGameState.GAME_OVER) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Level $level", color = DeepWorkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Lives: $lives", color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                when (gameState) {
                    MatrixGameState.SHOWING_PATTERN -> "Memorize the pattern!"
                    MatrixGameState.WAITING_FOR_INPUT -> "Tap the illuminated tiles"
                    MatrixGameState.LEVEL_COMPLETE -> "Matrix cleared!"
                    else -> ""
                },
                color = Color.White, fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            // 4x4 Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                gridItems.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { index ->
                            val isPatternTile = pattern.contains(index)
                            val isUserTapped = userPattern.contains(index)
                            val isIncorrect = incorrectTaps.contains(index)
                            
                            val tileColor = when {
                                gameState == MatrixGameState.SHOWING_PATTERN && isPatternTile -> DeepWorkBlue
                                gameState == MatrixGameState.WAITING_FOR_INPUT && isUserTapped -> DeepWorkBlue
                                gameState == MatrixGameState.WAITING_FOR_INPUT && isIncorrect -> Color.Red
                                else -> DeepWorkSurface
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tileColor)
                                    .clickable(enabled = gameState == MatrixGameState.WAITING_FOR_INPUT && !isUserTapped && !isIncorrect) {
                                        if (isPatternTile) {
                                            userPattern = userPattern + index
                                            if (userPattern.size == pattern.size) {
                                                score += (level * 20)
                                                gameState = MatrixGameState.LEVEL_COMPLETE
                                                scope.launch {
                                                    delay(1000)
                                                    level++
                                                    startLevel()
                                                }
                                            }
                                        } else {
                                            incorrectTaps = incorrectTaps + index
                                            lives--
                                            if (lives <= 0) {
                                                gameState = MatrixGameState.GAME_OVER
                                                viewModel.recordCognitiveResult(level, score)
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                if (gameState == MatrixGameState.GAME_OVER) "Game Over! Score: $score" else "Memorize the grid matrix.",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { 
                    level = 1
                    score = 0
                    lives = 3
                    startLevel() 
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (gameState == MatrixGameState.START) "START GAME" else "TRY AGAIN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
