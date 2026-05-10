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
import androidx.navigation.NavController
import com.example.deepworkai.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GameState {
    START, SHOWING_SEQUENCE, WAITING_FOR_INPUT, LEVEL_COMPLETE, GAME_OVER
}

@Composable
fun CognitiveChallengeScreen(
    navController: NavController,
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user
    var gameState by remember { mutableStateOf(GameState.START) }
    var level by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var userSequence by remember { mutableStateOf(listOf<Int>()) }
    var activeTile by remember { mutableIntStateOf(-1) }
    
    val gridItems = (0..8).toList()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    fun startLevel() {
        val newSequence = mutableListOf<Int>()
        repeat(level + 2) {
            newSequence.add((0..8).random())
        }
        sequence = newSequence
        userSequence = listOf()
        gameState = GameState.SHOWING_SEQUENCE
        
        scope.launch {
            delay(800)
            sequence.forEach { index ->
                activeTile = index
                delay(if (level > 5) 300 else 500)
                activeTile = -1
                delay(200)
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
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Streak Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${user?.cognitiveStreak ?: 0}",
                        color = Color(0xFFF97316),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (level.toFloat() / 15f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = DeepWorkBlue,
                trackColor = DeepWorkSurface
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text("Level $level", color = DeepWorkBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            AnimatedContent(targetState = gameState, label = "text") { state ->
                Text(
                    text = when (state) {
                        GameState.START -> "Warm up your working memory!"
                        GameState.SHOWING_SEQUENCE -> "Watch the pattern carefully..."
                        GameState.WAITING_FOR_INPUT -> "Repeat the sequence!"
                        GameState.LEVEL_COMPLETE -> "Perfect Focus! Level Up."
                        GameState.GAME_OVER -> "Final Score: $score"
                    },
                    color = if (state == GameState.GAME_OVER) Color(0xFFEF4444) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3x3 Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                gridItems.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { index ->
                            val isHighlighted = activeTile == index
                            
                            val glowAlpha by animateFloatAsState(
                                targetValue = if (isHighlighted) 0.8f else 0f,
                                animationSpec = tween(150), label = "glow"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isHighlighted) DeepWorkBlue else DeepWorkSurface
                                    )
                                    .then(
                                        if (isHighlighted) Modifier.background(
                                            androidx.compose.ui.graphics.Brush.radialGradient(
                                                colors = listOf(DeepWorkBlue, Color.Transparent),
                                                radius = 150f
                                            )
                                        ) else Modifier
                                    )
                                    .clickable(enabled = gameState == GameState.WAITING_FOR_INPUT) {
                                        if (gameState == GameState.WAITING_FOR_INPUT) {
                                            val newUserSequence = userSequence + index
                                            userSequence = newUserSequence
                                            
                                            // Check correctness
                                            if (sequence[userSequence.size - 1] != index) {
                                                gameState = GameState.GAME_OVER
                                                // Save Result
                                                viewModel.recordCognitiveResult(level, score)
                                            } else {
                                                score += (10 * level)
                                                if (newUserSequence.size == sequence.size) {
                                                    gameState = GameState.LEVEL_COMPLETE
                                                    scope.launch {
                                                        delay(800)
                                                        level++
                                                        startLevel()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isHighlighted) {
                                    Icon(
                                        Icons.Default.Refresh, 
                                        null, 
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(30.dp)
                                    )
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
                        if (gameState == GameState.GAME_OVER) {
                            level = 1
                            score = 0
                        }
                        startLevel() 
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(if (gameState == GameState.START) Icons.Default.Refresh else Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (gameState == GameState.START) "START DAILY CHALLENGE" else "RETRY CHALLENGE", 
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            } else if (gameState == GameState.WAITING_FOR_INPUT) {
                Surface(
                    color = DeepWorkBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepWorkBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        "${userSequence.size} / ${sequence.size} Steps",
                        color = DeepWorkBlue,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

