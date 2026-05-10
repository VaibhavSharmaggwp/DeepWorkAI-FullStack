package com.example.deepworkai.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deepworkai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GameState {
    START, SHOWING_SEQUENCE, WAITING_FOR_INPUT, LEVEL_COMPLETE, GAME_OVER
}

@Composable
fun CognitiveChallengeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var gameState by remember { mutableStateOf(GameState.START) }
    var level by remember { mutableIntStateOf(1) }
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userSequence by remember { mutableStateOf(listOf<Int>()) }
    var activeTile by remember { mutableIntStateOf(-1) }
    
    val gridItems = (0..8).toList()

    fun startLevel() {
        val newSequence = mutableListOf<Int>()
        repeat(level + 2) {
            newSequence.add((0..8).random())
        }
        sequence = newSequence
        userSequence = listOf()
        gameState = GameState.SHOWING_SEQUENCE
        
        scope.launch {
            delay(1000)
            sequence.forEach { index ->
                activeTile = index
                delay(600)
                activeTile = -1
                delay(300)
            }
            gameState = GameState.WAITING_FOR_INPUT
        }
    }

    Scaffold(
        containerColor = DeepWorkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        .clickable { navController.popBackStack() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Cognitive Challenge", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text("Level $level", color = DeepWorkBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (gameState) {
                    GameState.START -> "Ready to train your memory?"
                    GameState.SHOWING_SEQUENCE -> "Watch the pattern carefully..."
                    GameState.WAITING_FOR_INPUT -> "Repeat the sequence!"
                    GameState.LEVEL_COMPLETE -> "Perfect Focus! Level Up."
                    GameState.GAME_OVER -> "Game Over. Your brain is warming up!"
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3x3 Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                gridItems.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { index ->
                            val isHighlighted = activeTile == index
                            val isCorrect = userSequence.contains(index) && sequence.take(userSequence.size).contains(index)
                            
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            isHighlighted -> DeepWorkBlue
                                            gameState == GameState.WAITING_FOR_INPUT && userSequence.contains(index) -> DeepWorkBlue.copy(alpha = 0.5f)
                                            else -> DeepWorkSurface
                                        }
                                    )
                                    .clickable(enabled = gameState == GameState.WAITING_FOR_INPUT) {
                                        if (gameState == GameState.WAITING_FOR_INPUT) {
                                            val newUserSequence = userSequence + index
                                            userSequence = newUserSequence
                                            
                                            // Check correctness
                                            if (sequence[userSequence.size - 1] != index) {
                                                gameState = GameState.GAME_OVER
                                            } else if (newUserSequence.size == sequence.size) {
                                                gameState = GameState.LEVEL_COMPLETE
                                                scope.launch {
                                                    delay(1000)
                                                    level++
                                                    startLevel()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isHighlighted) {
                                    Box(modifier = Modifier.fillMaxSize().background(DeepWorkBlue.copy(alpha = 0.3f)))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                Button(
                    onClick = { 
                        level = 1
                        startLevel() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
                ) {
                    Icon(if (gameState == GameState.START) Icons.Default.Refresh else Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (gameState == GameState.START) "Start Warmup" else "Try Again", fontWeight = FontWeight.Bold)
                }
            } else if (gameState == GameState.WAITING_FOR_INPUT) {
                Text(
                    "${userSequence.size} / ${sequence.size} found",
                    color = DeepWorkTextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
