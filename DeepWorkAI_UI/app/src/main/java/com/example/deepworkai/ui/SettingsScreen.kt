package com.example.deepworkai.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.deepworkai.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController, 
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.user
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        profileViewModel.fetchProfile()
    }

    Scaffold(
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Profile & Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Profile Card
            Surface(
                color = Color(0xFF161B22),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.Profile.route) }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(100.dp)) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D1117))
                                .border(2.dp, Color(0xFF3B82F6), CircleShape)
                        ) {
                            if (user?.imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(com.example.deepworkai.BuildConfig.BACKEND_URL + user?.imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(20.dp))
                            }
                        }
                        // Online Status
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(3.dp, Color(0xFF161B22), CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user?.fullName ?: "Loading...", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Focus Score: ${user?.focusScore ?: 0}/100", color = Color(0xFF3B82F6), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("SYSTEM PREFERENCES", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF161B22),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    PreferenceItem(
                        icon = Icons.Default.NightsStay,
                        title = "Dark Mode",
                        subtitle = "System override",
                        showSwitch = true,
                        checked = user?.darkMode ?: true,
                        onCheckedChange = { profileViewModel.updatePreferences(darkMode = it) }
                    )
                    Divider(color = Color(0xFF0D1117), thickness = 1.dp)
                    PreferenceItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Push & email alerts",
                        onClick = { navController.navigate(Screen.Notifications.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("RESEARCH DATA", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val token = com.example.deepworkai.network.NetworkPreferences.authToken ?: ""
                ExportCard(
                    title = "Export PDF",
                    subtitle = "Full Report",
                    icon = Icons.Default.PictureAsPdf,
                    iconColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl(context, com.example.deepworkai.BuildConfig.BACKEND_URL + "/api/export/pdf?token=$token") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                ExportCard(
                    title = "Export CSV",
                    subtitle = "Raw Data",
                    icon = Icons.Default.TableChart,
                    iconColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl(context, com.example.deepworkai.BuildConfig.BACKEND_URL + "/api/export/csv?token=$token") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = Color(0xFF161B22),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data last synced: 10:42 AM", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("AI Model Version: v4.2.0 (Stable)", color = Color(0xFF3B82F6), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("GENERAL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFF161B22),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = { navController.navigate(Screen.Security.route) }
                    )
                    Divider(color = Color(0xFF0D1117), thickness = 1.dp)
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About DeepWork AI",
                        onClick = { navController.navigate(Screen.About.route) }
                    )
                    Divider(color = Color(0xFF0D1117), thickness = 1.dp)
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help & Support",
                        onClick = { navController.navigate(Screen.Help.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    com.example.deepworkai.network.NetworkPreferences.authToken = null
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PreferenceItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showSwitch: Boolean = false,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!showSwitch) onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF0D1117),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        if (showSwitch) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3B82F6))
            )
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun ExportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161B22),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
    context.startActivity(intent)
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
