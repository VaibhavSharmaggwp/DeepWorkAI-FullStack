package com.example.deepworkai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deepworkai.ui.SessionHistoryItem
import com.example.deepworkai.models.SessionSummaryResponse
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepworkai.viewmodel.SessionViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import es.dmoral.toasty.Toasty
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: SessionViewModel = viewModel()
) {
    val context = LocalContext.current
    val userId = com.example.deepworkai.network.NetworkPreferences.userId ?: "4acbc632-9cb6-4d7c-8bcc-8c3bd226f9c0"
    
    val historyList by viewModel.history.collectAsState()
    val bgColor = MaterialTheme.colorScheme.background

    LaunchedEffect(Unit) {
        viewModel.fetchHistory(userId)
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Detailed Insights", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // ⬅️ Navigate Prev
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        val token = com.example.deepworkai.network.NetworkPreferences.authToken ?: ""
                        openUrl(context, com.example.deepworkai.network.NetworkPreferences.backendUrl + "/api/export/pdf?token=$token")
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Export PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History, 
                        contentDescription = null, 
                        tint = Color(0xFF475569), 
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No sessions recorded yet", color = Color(0xFF94A3B8))
                }
            }
        } else {
            var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Recent") } // "Recent", "Oldest", "Best", "Worst"

            val totalFocusMinutes = historyList.sumOf { 
                try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    val start = sdf.parse(it.startTime)
                    val end = it.endTime?.let { e -> sdf.parse(e) } ?: start
                    if (start != null && end != null) ((end.time - start.time) / (1000 * 60)).toInt() else 0
                } catch(e: Exception) { 0 }
            }
            val totalHours = totalFocusMinutes / 60
            val remainingMins = totalFocusMinutes % 60
            
            val avgScore = if (historyList.isNotEmpty()) historyList.sumOf { it.focusScore } / historyList.size else 0
            val topCategory = historyList.mapNotNull { it.tags }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"

            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Analytics Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Focus Time",
                        value = "${totalHours}h ${remainingMins}m",
                        icon = Icons.Default.Timer,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Avg Score",
                        value = "$avgScore%",
                        icon = Icons.Default.Star,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Top Tag",
                        value = topCategory,
                        icon = Icons.Default.Category,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search sessions or tags...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.History, contentDescription = null, tint = Color.Gray) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filter Chips (Custom Surface to avoid M3 version issues)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Recent", "Oldest", "Best", "Worst")
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier.clickable { selectedFilter = filter },
                            color = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color(0xFF3B82F6) else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filter & Search Logic
                val filteredList = historyList.filter { session ->
                    val displayTitle = session.sessionName ?: "General Block"
                    val tag = session.tags ?: "Focus"
                    
                    displayTitle.contains(searchQuery, ignoreCase = true) || 
                    tag.contains(searchQuery, ignoreCase = true)
                }.let { list ->
                    when (selectedFilter) {
                        "Recent" -> list.sortedByDescending { it.startTime }
                        "Oldest" -> list.sortedBy { it.startTime }
                        "Best" -> list.sortedByDescending { it.focusScore }
                        "Worst" -> list.sortedBy { it.focusScore }
                        else -> list
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList) { session ->
                        SessionHistoryItem(session = session) {
                            // Optional: Navigate to a specific session detail
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryScreenPreview() {
    val navController = rememberNavController()
    HistoryScreen(navController = navController)
}


private fun openUrl(context: android.content.Context, url: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun SummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
