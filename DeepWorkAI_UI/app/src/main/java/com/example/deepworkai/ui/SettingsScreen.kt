package com.example.deepworkai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var isManualVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFF0D1117),
            topBar = {
                TopAppBar(
                    title = { Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsSection(title = "Account") {
                    SettingsItem(icon = Icons.Default.Person, title = "Profile")
                    SettingsItem(
                        icon = Icons.Default.Lock, 
                        title = "Security",
                        onClick = { navController.navigate(Screen.Security.route) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SettingsSection(title = "Preferences") {
                    SettingsItem(icon = Icons.Default.Notifications, title = "Notifications")
                    SettingsItem(
                        icon = Icons.Default.Info, 
                        title = "About DeepWorkAI",
                        onClick = { isManualVisible = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = { 
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isManualVisible) {
            UserManualOverlay(onClose = { isManualVisible = false })
        }
    }
}

@Composable
fun UserManualOverlay(onClose: () -> Unit) {
    val manualText = """
        WELCOME TO DEEPWORK AI
        -----------------------
        1. START SESSION: Click the '+' button on Home.
        2. FOCUS: Keep the app open to detect flow.
        3. PAUSE: Use the Coffee icon to take a break.
        4. ANALYZE: Check Analytics for deep work trends.
        5. PERSIST: Your data is synced automatically.
        
        READY TO REACH PEAK PERFORMANCE?
    """.trimIndent()

    Surface(
        color = Color.Black.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TypewriterText(
                text = manualText,
                color = Color(0xFF2DD4BF),
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GOT IT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TypewriterText(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        text.forEachIndexed { index, _ ->
            displayedText = text.substring(0, index + 1)
            delay(40)
        }
    }

    Text(
        text = displayedText + if (System.currentTimeMillis() % 1000 < 500) "_" else " ",
        color = color,
        fontSize = fontSize,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        lineHeight = 28.sp,
        textAlign = TextAlign.Start
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Surface(
            color = Color(0xFF161B22),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
    }
}
