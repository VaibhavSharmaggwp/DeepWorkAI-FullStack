package com.example.deepworkai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deepworkai.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val user by viewModel.user
    
    var enabled by remember { mutableStateOf(user?.notificationsEnabled ?: true) }
    var selectedType by remember { mutableStateOf(user?.notificationType ?: "notification") }
    var selectedTime by remember { mutableStateOf(user?.notificationTime ?: "09:00") }

    LaunchedEffect(user) {
        user?.let {
            enabled = it.notificationsEnabled
            selectedType = it.notificationType
            selectedTime = it.notificationTime
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Reminders", color = Color.White, fontSize = 18.sp)
                Switch(
                    checked = enabled,
                    onCheckedChange = { 
                        enabled = it
                        viewModel.updatePreferences(notificationsEnabled = it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Reminder Type", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TypeCard(
                    title = "App Notification",
                    icon = Icons.Default.Notifications,
                    isSelected = selectedType == "notification",
                    onClick = { 
                        selectedType = "notification"
                        viewModel.updatePreferences(notificationType = "notification")
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TypeCard(
                    title = "Email Alert",
                    icon = Icons.Default.Email,
                    isSelected = selectedType == "email",
                    onClick = { 
                        selectedType = "email"
                        viewModel.updatePreferences(notificationType = "email")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Preferred Time", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Simplified time picker (could be expanded)
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { 
                    selectedTime = it
                    viewModel.updatePreferences(notificationTime = it)
                },
                label = { Text("Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color(0xFF161B22),
                    focusedBorderColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun TypeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color(0xFF161B22),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) Color(0xFF3B82F6) else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) Color(0xFF3B82F6) else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title, 
                color = if (isSelected) Color.White else Color.Gray, 
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
