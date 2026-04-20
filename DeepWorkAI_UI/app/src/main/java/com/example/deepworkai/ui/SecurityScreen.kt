package com.example.deepworkai.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    
    // State for toggles
    var e2eEnabled by remember { mutableStateOf(true) }
    var localProcessing by remember { mutableStateOf(true) }
    var anonymousAnalytics by remember { mutableStateOf(false) }
    var biometricsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = { Text("Security & Privacy", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Hero Section: Security Status
            SecurityStatusHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "DATA PROTECTION",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            SecurityCard(
                icon = Icons.Default.Lock,
                title = "End-to-End Encryption",
                description = "Your session data and focus metrics are encrypted before being synced to the cloud.",
                enabled = e2eEnabled,
                onToggle = { e2eEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecurityCard(
                icon = Icons.Default.Dns,
                title = "Local AI Processing",
                description = "Cognitive load analysis is performed locally on your device. Your behavior never leaves your phone.",
                enabled = localProcessing,
                onToggle = { localProcessing = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "PRIVACY PREFERENCES",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            SecurityItem(
                icon = Icons.Default.Assessment,
                title = "Anonymous Analytics",
                description = "Share crash reports and usage patterns anonymously to help us improve.",
                checked = anonymousAnalytics,
                onCheckedChange = { anonymousAnalytics = it }
            )
            
            SecurityItem(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Lock",
                description = "Require fingerprint or face ID to access your session history.",
                checked = biometricsEnabled,
                onCheckedChange = { biometricsEnabled = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Transparency Section
            TransparencyBanner()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SecurityStatusHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E293B).copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2DD4BF), Color(0xFF3B82F6))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text("Your Data is Safe", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("DeepWorkAI uses military-grade encryption to protect your focus insights.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SecurityCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2DD4BF),
                        checkedTrackColor = Color(0xFF2DD4BF).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, color = Color(0xFF94A3B8), fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun SecurityItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(description, color = Color(0xFF64748B), fontSize = 12.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF3B82F6),
                uncheckedColor = Color(0xFF475569)
            )
        )
    }
}

@Composable
fun TransparencyBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF3B82F6).copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF3B82F6))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "We are GDPR & CCPA compliant. Your privacy is our priority.",
                color = Color(0xFFBFDBFE),
                fontSize = 13.sp
            )
        }
    }
}
