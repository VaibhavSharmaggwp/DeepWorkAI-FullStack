package com.example.deepworkai.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepworkai.models.CreateTaskRequest
import com.example.deepworkai.models.Task
import com.example.deepworkai.network.NetworkPreferences
import com.example.deepworkai.network.TaskService
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val taskService = TaskService()

    private val _tasks = mutableStateOf<List<Task>>(emptyList())
    val tasks: State<List<Task>> = _tasks

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        val userId = NetworkPreferences.userId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = taskService.getUserTasks(userId)
            _tasks.value = result
            _isLoading.value = false
        }
    }

    fun addTask(title: String, description: String? = null, estimatedMinutes: Int = 30) {
        val userId = NetworkPreferences.userId ?: return
        viewModelScope.launch {
            val task = taskService.createTask(userId, CreateTaskRequest(title, description, estimatedMinutes))
            if (task != null) {
                fetchTasks() // Refresh list
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: String) {
        viewModelScope.launch {
            val success = taskService.updateTaskStatus(taskId, status)
            if (success) {
                fetchTasks() // Refresh list
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val success = taskService.deleteTask(taskId)
            if (success) {
                fetchTasks() // Refresh list
            }
        }
    }
}
