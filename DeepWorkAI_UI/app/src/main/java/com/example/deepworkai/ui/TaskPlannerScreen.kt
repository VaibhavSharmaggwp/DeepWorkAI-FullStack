package com.example.deepworkai.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deepworkai.models.Task
import com.example.deepworkai.ui.theme.*
import com.example.deepworkai.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPlannerScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val tasks by viewModel.tasks
    val isLoading by viewModel.isLoading

    Scaffold(
        containerColor = DeepWorkBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Deep Planner", color = Color.White, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = DeepWorkBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Organize your deep work tasks. AI will prioritize complex work for your peak hours.",
                color = DeepWorkTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepWorkBlue)
                }
            } else if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.List, contentDescription = null, tint = DeepWorkTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tasks yet", color = DeepWorkTextSecondary)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onStatusChange = { newStatus -> viewModel.updateTaskStatus(task.id, newStatus) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, desc, mins ->
                    viewModel.addTask(title, desc, mins)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "Completed"
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepWorkSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    onStatusChange(if (isCompleted) "Pending" else "Completed") 
                }
            ) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompleted) Color(0xFF4ADE80) else DeepWorkTextSecondary
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = task.title,
                    color = if (isCompleted) DeepWorkTextSecondary else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = if (task.category == "Deep") Color(0xFF3B82F6).copy(0.1f) else Color(0xFF94A3B8).copy(0.1f),
                        contentColor = if (task.category == "Deep") Color(0xFF60A5FA) else DeepWorkTextSecondary
                    ) {
                        Text(task.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${task.estimatedMinutes}m", color = DeepWorkTextSecondary, fontSize = 12.sp)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171).copy(0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var estimatedMins by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepWorkSurface,
        title = { Text("New Task", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What are you working on?") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = estimatedMins,
                    onValueChange = { if (it.all { char -> char.isDigit() }) estimatedMins = it },
                    label = { Text("Estimated minutes") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, null, estimatedMins.toIntOrNull() ?: 30) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepWorkBlue)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepWorkTextSecondary)
            }
        }
    )
}
