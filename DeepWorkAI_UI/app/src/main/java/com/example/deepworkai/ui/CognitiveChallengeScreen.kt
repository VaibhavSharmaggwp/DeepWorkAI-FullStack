package com.example.deepworkai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deepworkai.ui.games.*
import com.example.deepworkai.ui.theme.*

data class GameInfo(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val icon: ImageVector,
    val color: Color
)

val gameList = listOf(
    GameInfo("pattern", "Pattern Sequence", "Repeat the flashing sequence of blocks.", "Easy", Icons.Default.GridOn, DeepWorkBlue),
    GameInfo("color", "Color Match", "Does the word's meaning match its color?", "Easy", Icons.Default.Palette, Color(0xFF10B981)),
    GameInfo("math", "Quick Math", "Solve basic arithmetic before time runs out.", "Medium", Icons.Default.Calculate, Color(0xFFF59E0B)),
    GameInfo("reaction", "Reaction Speed", "Tap as fast as possible when the screen turns green.", "Medium", Icons.Default.Speed, Color(0xFFEF4444)),
    GameInfo("matrix", "Memory Matrix", "Memorize the 4x4 grid pattern before it disappears.", "Hard", Icons.Default.Memory, Color(0xFFA855F7))
)

@Composable
fun CognitiveChallengeScreen(
    navController: NavController,
    viewModel: com.example.deepworkai.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedGame by remember { mutableStateOf<String?>(null) }
    val user by viewModel.user

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    Scaffold(
        containerColor = DeepWorkBackground
    ) { padding ->
        AnimatedContent(targetState = selectedGame, label = "game_transition") { game ->
            if (game == null) {
                // Game Selection Menu
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
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
                        Text("Flow Lab", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Streak Badge
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

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Train your focus and cognitive resilience with these targeted mini-games.",
                        color = Color.Gray, fontSize = 16.sp, lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(gameList) { info ->
                            GameCard(info) {
                                selectedGame = info.id
                            }
                        }
                    }
                }
            } else {
                // Render Selected Game
                when (game) {
                    "pattern" -> PatternSequenceGame(viewModel = viewModel, onBack = { selectedGame = null })
                    "color" -> ColorMatchGame(viewModel = viewModel, onBack = { selectedGame = null })
                    "math" -> QuickMathGame(viewModel = viewModel, onBack = { selectedGame = null })
                    "reaction" -> ReactionSpeedGame(viewModel = viewModel, onBack = { selectedGame = null })
                    "matrix" -> MemoryMatrixGame(viewModel = viewModel, onBack = { selectedGame = null })
                }
            }
        }
    }
}

@Composable
fun GameCard(info: GameInfo, onClick: () -> Unit) {
    Surface(
        color = DeepWorkSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = info.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(info.icon, null, tint = info.color, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(info.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(info.description, color = Color.Gray, fontSize = 14.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                val diffColor = when(info.difficulty) {
                    "Easy" -> Color(0xFF10B981)
                    "Medium" -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
                
                Text(info.difficulty, color = diffColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.ArrowForwardIos, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
