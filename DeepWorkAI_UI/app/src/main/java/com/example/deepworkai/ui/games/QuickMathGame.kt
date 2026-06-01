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

enum class MathGameState {
    START, PLAYING, GAME_OVER
}

@Composable
fun QuickMathGame(
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by viewModel.user
    var gameState by remember { mutableStateOf(MathGameState.START) }
    var score by remember { mutableIntStateOf(0) }
    
    var equation by remember { mutableStateOf("") }
    var answer by remember { mutableIntStateOf(0) }
    var options by remember { mutableStateOf(listOf<Int>()) }
    var timeLeft by remember { mutableIntStateOf(10) }
    
    fun generateEquation() {
        val ops = listOf("+", "-", "*")
        val op = ops.random()
        val num1 = if (op == "*") (2..12).random() else (10..99).random()
        val num2 = if (op == "*") (2..12).random() else (10..99).random()
        
        val actualAns = when(op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            else -> num1 * num2
        }
        
        val wrongAns = mutableSetOf<Int>()
        while (wrongAns.size < 3) {
            val wrong = actualAns + (-10..10).random()
            if (wrong != actualAns) wrongAns.add(wrong)
        }
        
        equation = "$num1 $op $num2 = ?"
        answer = actualAns
        options = (wrongAns.toList() + actualAns).shuffled()
        timeLeft = 10
    }

    fun startGame() {
        score = 0
        generateEquation()
        gameState = MathGameState.PLAYING
    }

    LaunchedEffect(gameState, timeLeft) {
        if (gameState == MathGameState.PLAYING && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) {
                gameState = MathGameState.GAME_OVER
                viewModel.recordCognitiveResult(score / 15, score)
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
            Text("Quick Math", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
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

        if (gameState == MathGameState.PLAYING) {
            LinearProgressIndicator(
                progress = { (timeLeft.toFloat() / 10f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (timeLeft > 3) DeepWorkBlue else Color.Red,
                trackColor = DeepWorkSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Score: $score", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(48.dp))
            
            // Equation
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(150.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(equation, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Options Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                options.chunked(2).forEach { rowOps ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowOps.forEach { opt ->
                            Button(
                                onClick = {
                                    if (opt == answer) {
                                        score += 15
                                        generateEquation()
                                    } else {
                                        gameState = MathGameState.GAME_OVER
                                        viewModel.recordCognitiveResult(score / 15, score)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepWorkSurface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("$opt", color = DeepWorkBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
            
        } else {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                if (gameState == MathGameState.GAME_OVER) "Game Over! Score: $score" else "Solve arithmetic equations as fast as you can.",
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
                Text(if (gameState == MathGameState.START) "START GAME" else "TRY AGAIN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
