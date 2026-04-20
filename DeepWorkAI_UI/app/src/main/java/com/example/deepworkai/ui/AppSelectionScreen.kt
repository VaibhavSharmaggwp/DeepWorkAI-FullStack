package com.example.deepworkai.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.example.deepworkai.ui.theme.DeepWorkBackground
import com.example.deepworkai.ui.theme.DeepWorkBlue
import com.example.deepworkai.ui.theme.DeepWorkSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(val packageName: String, val appName: String, val icon: android.graphics.Bitmap?)

@Composable
fun AppSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    val prefs = context.getSharedPreferences("AppTrackingPrefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        val saved = prefs.getStringSet("tracked_apps", emptySet()) ?: emptySet()
        selectedPackages = saved

        // Load apps in background
        val apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val result = mutableListOf<InstalledApp>()

            for (appInfo in packages) {
                // Filter out system apps
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && appInfo.packageName != context.packageName) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val icon = try {
                        pm.getApplicationIcon(appInfo).toBitmap(100, 100)
                    } catch (e: Exception) { null }
                    result.add(InstalledApp(appInfo.packageName, appName, icon))
                }
            }
            result.sortedBy { it.appName.lowercase() } // Alphabetical order
        }
        installedApps = apps
        isLoading = false
    }

    Scaffold(containerColor = DeepWorkBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Select Apps to Track", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Disclaimer Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Privacy", tint = Color(0xFF60A5FA))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Privacy First: We only monitor the apps you choose during focus sessions. No personal data, messages, or content is accessed. All data is encrypted and used only to generate productivity insights.",
                        color = Color(0xFFDBEAFE),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepWorkBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    items(installedApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepWorkSurface),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                val newSet = selectedPackages.toMutableSet()
                                if (isSelected) newSet.remove(app.packageName) else newSet.add(app.packageName)
                                selectedPackages = newSet
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (app.icon != null) {
                                    Image(bitmap = app.icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
                                } else {
                                    Box(modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.Gray, RoundedCornerShape(8.dp)))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(app.appName, color = Color.White, modifier = Modifier.weight(1f), fontSize = 16.sp)
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = DeepWorkBlue)
                                )
                            }
                        }
                    }
                }
                
                // Save Button
                Button(
                    onClick = {
                        prefs.edit().putStringSet("tracked_apps", selectedPackages).putBoolean("is_tracking_setup_complete", true).apply()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save & Continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
